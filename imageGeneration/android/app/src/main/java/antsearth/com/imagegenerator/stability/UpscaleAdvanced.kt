package antsearth.com.imagegenerator.stability

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import antsearth.com.imagegenerator.Constants
import antsearth.com.imagegenerator.ImageGenViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Headers
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


private const val TAG = "UpscaleAdvanced"
class UpscaleAdvanced {
    interface UpscaleApiService {
        @Headers("accept: image/*")

        @Multipart
        @POST
        fun upscaleImage(
            @Header("authorization") authHeader: String,
            @Url url: String,
            @Part image: MultipartBody.Part,
            @Part("prompt") prompt: RequestBody,
            @Part("output_format") outputFormat: RequestBody,
            @Part("negative_prompt") negativePrompt: RequestBody,
            @Part("creativity") creativity: RequestBody
        ): Call<ResponseBody>
    }

    object RetrofitClient {
        private const val BASE_URL = "https://api.stability.ai/"
        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .build()

        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        }

        val api: UpscaleApiService by lazy {
            retrofit.create(UpscaleApiService::class.java)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun upscaleImage(apiKey: String, context: Context, inputUri: Uri,
                     model: ImageGenViewModel) {
        val api = RetrofitClient.api
        val url = "v2beta/stable-image/upscale/conservative"
        val outputFormat = "png"
        var call:  Call<ResponseBody>

        context.contentResolver.getType(inputUri)?.let {
            val file = File(inputUri.path!!)
            Log.d(TAG, "File name: ${file.name}")
            val requestBody = InputStreamRequestBody(context.contentResolver, inputUri)
            val filePart = MultipartBody.Part.createFormData("image", "image", requestBody)
            Log.d(TAG, "File part: ${filePart.headers}")
            val param = model.upscaleParameters.value
            val promptBody = param.prompt.toRequestBody("text/plain".toMediaTypeOrNull())
            Log.d(TAG, param.prompt)
            Log.d(TAG, param.negativePrompt)
            Log.d(TAG, "${param.creativity}")
            val negativePrompt = param.negativePrompt.toRequestBody("text/plain".toMediaTypeOrNull())
            val creativity = param.creativity.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val outputFormatBody = outputFormat.toRequestBody("text/plain".toMediaTypeOrNull())

            GlobalScope.launch {
                call = api.upscaleImage(
                    "Bearer $apiKey", url,
                    filePart, promptBody, outputFormatBody, negativePrompt, creativity
                )
                Log.d(TAG, "Upscale service created before enqueue")
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        Log.d(TAG, "Response status code: ${response.code()}")
                        Log.d(TAG, "Response header: ${response.headers()}")
                        if (response.isSuccessful) {
                            Log.d(TAG, "Response successful")
                            val data = response.body()
                            if (data != null) {
                                val byteData = data.bytes()
                                Log.d(TAG, "File size: ${byteData.size}")
                                val bitmap = BitmapFactory.decodeByteArray(
                                    byteData, 0,
                                    byteData.size
                                )
                                val outputFile = File(
                                    context.cacheDir,
                                    Constants.UPSCALE_FILENAME
                                )
                                FileOutputStream(outputFile).use { outputStream ->
                                    outputStream.write(byteData)
                                }
                                Log.d(TAG, "Image downloaded to: ${outputFile.absolutePath}")
                                val list = mutableListOf<Bitmap>()
                                list.add(bitmap)
                                model.imageBitmap.value = list
                                Log.d(TAG, "Bitmap data stored in the model")
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
}