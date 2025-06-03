package antsearth.com.imagegenerator.stability

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import antsearth.com.imagegenerator.Constants
import antsearth.com.imagegenerator.ImageGenViewModel
import antsearth.com.imagegenerator.data.ErrorResponse
import antsearth.com.imagegenerator.data.GenerationIdResponse
import antsearth.com.imagegenerator.data.VideoGenerationParameters
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


private const val TAG = "ImageToVideo"

class ImageToVideo {
    /* Image to Video post service */
    interface ImageToVideoPostService {
        @Multipart
        @POST("v2beta/image-to-video")
        fun imageToVideoPost(
            @Header("authorization") authHeader: String,
            @Part image: MultipartBody.Part,
            @Part("seed") seed: RequestBody,
            @Part("cfg_scale") cfgScale: RequestBody,
            @Part("motion_bucket_id") motionBucketId: RequestBody

        ): Call<GenerationIdResponse>
    }

    interface ImageToVideoGetService {
        @Headers("accept: video/*")

        @GET
        fun imageToVideoGet(
            @Header("authorization") authHeader: String,
            @Url url: String
        ): Call<ResponseBody>
    }

    object RetrofitClient {
        private const val BASE_URL = "https://api.stability.ai/"

        private val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .build()

        val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }

        val apiPost: ImageToVideoPostService by lazy {
            retrofit.create(ImageToVideoPostService::class.java)
        }

        val apiGet: ImageToVideoGetService by lazy {
            retrofit.create(ImageToVideoGetService::class.java)
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun generateImageToVideoPost(apiKey: String, inputUri: Uri,
                                 context: Context, model: ImageGenViewModel) {
        val api = RetrofitClient.apiPost
        val vgp: VideoGenerationParameters = model.videoGenerationParameters.value
        //val url = "v2beta/image-to-video"
        val seed = "0".toRequestBody("text/plain".toMediaTypeOrNull())
        Log.d(TAG, "seed: $seed")
        val cfgScale = vgp.cfgScale.toString()
            .toRequestBody("text/plain".toMediaTypeOrNull())
        Log.d(TAG, "cfg_scale: $cfgScale")
        val motionBucketId = vgp.motionBucketId.toString()
            .toRequestBody("text/plain".toMediaTypeOrNull())
        Log.d(TAG, "motionBucketId: $motionBucketId")

        var callPost:  Call<GenerationIdResponse>

        context.contentResolver.getType(inputUri)?.let {
            val original = BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(inputUri)
            )
            Log.d(TAG, "Read data from uri")
            val newWidth = 768
            val newHeight = 768
            val resizedImage =
                Bitmap.createScaledBitmap(original, newWidth, newHeight, false)
            Log.d(TAG, "Resized the bitmap data")
            val outputStream = ByteArrayOutputStream()
            resizedImage.compress(Bitmap.CompressFormat.JPEG,
                100, outputStream)
            val requestBody = outputStream.toByteArray()
                .toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, outputStream.size())
            Log.d(TAG, "RequestBody formed")
            val filePart = MultipartBody.Part.createFormData("image",
                "image", requestBody)
            GlobalScope.launch {
                callPost = api.imageToVideoPost("Bearer $apiKey", filePart,
                    seed, cfgScale, motionBucketId)

                callPost.enqueue(object : Callback<GenerationIdResponse> {
                    override fun onResponse(
                        call: Call<GenerationIdResponse>,
                        response: Response<GenerationIdResponse>
                    ) {
                        Log.d(TAG, "Post response status code: ${response.code()}")
                        Log.d(TAG, "Post Response header: ${response.headers()}")
                        if (response.isSuccessful) {
                            Log.d(TAG, "Post Response successful ${response.body()}")
                            val data = response.body()
                            if (data != null) {
                                val generationId = data.id
                                Log.d(TAG, "generation Id: $generationId")
                                model.setVideoGenerationId(generationId)
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val gson = MoshiConverterFactory.create().responseBodyConverter(
                                ErrorResponse::class.java,
                                arrayOf(),
                                RetrofitClient.retrofit
                            )
                            val errorResponse = errorBody?.toResponseBody()
                                ?.let { it1 -> gson?.convert(it1) }
                            Log.d(TAG, "Error: ${errorResponse?.toString()}")
                        }
                        model.setStatusCode(response.code())
                    }

                    override fun onFailure(call: Call<GenerationIdResponse>, t: Throwable) {
                        Log.d(TAG, "Request failed: ${t.message}")
                        model.setStatusCode(Constants.API_INTERNAL_ERROR)
                    }
                })
            }

        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun generateImageToVideoGet(apiKey: String,
                                context: Context, model: ImageGenViewModel) {
        val api = RetrofitClient.apiGet
        val generationId = model.videoGenerationId.value
        val url = "v2beta/image-to-video/result/${generationId}"

        GlobalScope.launch {
            val callGet = api.imageToVideoGet("Bearer $apiKey", url)
            callGet.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.d(TAG, "Get response code: ${response.code()}")
                    Log.d(TAG, "Get response header: ${response.headers()}")
                    if (response.isSuccessful) {
                        if (response.code() == Constants.API_VIDEO_OR_IMAGE_GENERATION_IN_PROGRESS) {
                            Log.d(TAG, "Response generation still in progress")
                        } else if (response.code() == Constants.API_SUCCESS) {
                            Log.d(TAG, "Response successful ${response.body()}")
                            val data = response.body()
                            if (data != null) {
                                val byteData = data.bytes()
                                val outputFile = File(context.cacheDir, Constants.VIDEO_FILENAME)
                                FileOutputStream(outputFile).use { outputStream ->
                                    outputStream.write(byteData)
                                }
                                model.setVideoFileExistence(true)
                            }
                        }
                    } else {
                        Log.d(TAG, "Non-200 response: ${response.message()}")
                    }
                    model.setStatusCode(response.code())
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d(TAG, "Request failed: ${t.message}")
                    model.setStatusCode(Constants.API_INTERNAL_ERROR)
                }
            })
        }
    }
}