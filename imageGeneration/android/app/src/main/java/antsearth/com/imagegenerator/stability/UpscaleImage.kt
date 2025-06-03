package antsearth.com.imagegenerator.stability

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import antsearth.com.imagegenerator.Constants
import antsearth.com.imagegenerator.ImageGenViewModel
import antsearth.com.imagegenerator.data.ErrorResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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

private const val TAG = "UpscaleImage"

class UpscaleImage {
    interface StabilityApiService {
        @Headers("Accept: image/png")

        @Multipart
        @POST
        fun upscaleImage(
            @Header("Authorization") authHeader: String,
            @Url url: String,
            @Part image: MultipartBody.Part,
            @Part("width") width: RequestBody
        ): Call<ResponseBody>
    }


    object RetrofitClient {
        private const val BASE_URL = "https://api.stability.ai"

        private val httpClient = OkHttpClient.Builder()
            .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .build()

        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val apiService: StabilityApiService by lazy {
            retrofit.create(StabilityApiService::class.java)
        }
    }

    fun upscaleImage(apiKey: String, inputUri: Uri, context: Context, model: ImageGenViewModel) {
        val url = "/v1/generation/esrgan-v1-x2plus/image-to-image/upscale"
        Log.d(TAG, "Input Uri: $inputUri")
        val service = RetrofitClient.apiService
        var call: Call<ResponseBody>

        context.contentResolver.getType(inputUri)?.let {
            val file = File(inputUri.path!!)
            Log.d(TAG, "File name: ${file.name}")
            /*val requestBody = InputStreamRequestBody(context.contentResolver, inputUri)
            val filePart = MultipartBody.Part.createFormData("image", "image", requestBody)*/
            val width = model.imageWidth.value.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val filePart = convertJpegUriToPngMultiPartBody(context, inputUri)
            if (filePart != null) {
                Log.d(TAG, "File part: ${filePart.headers}")

                GlobalScope.launch {
                    call = service.upscaleImage("Bearer $apiKey", url, filePart, width)
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
                                    val bitmap =
                                        BitmapFactory.decodeByteArray(byteData, 0, byteData.size)
                                    val list = mutableListOf<Bitmap>()
                                    list.add(bitmap)
                                    model.imageBitmap.value = list
                                    val outputFile = File(context.cacheDir, "upscale_image.png")
                                    FileOutputStream(outputFile).use { outputStream ->
                                        outputStream.write(byteData)
                                    }
                                    Log.d(
                                        TAG,
                                        "Image downloaded successfully to: ${outputFile.absolutePath}"
                                    )
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val gson = MoshiConverterFactory.create().responseBodyConverter(
                                    ErrorResponse::class.java,
                                    arrayOf(),
                                    ImageToVideo.RetrofitClient.retrofit
                                )
                                val errorResponse = errorBody?.toResponseBody()
                                    ?.let { it1 -> gson?.convert(it1) }
                                Log.d(TAG, "Error: ${errorResponse?.toString()}")
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

    private fun convertJpegUriToPngMultiPartBody(context: Context,
                                                 imageUri: Uri): MultipartBody.Part? {
        // Load the JPEG image from the URI into a Bitmap
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Check if the bitmap was successfully loaded
        if (bitmap == null) {
            Log.d(TAG, "Failed to load the image from URI.")
            return null
        }

        // Compress the Bitmap into PNG format and write it to a ByteArrayOutputStream
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val pngByteArray = outputStream.toByteArray()
        outputStream.close()

        // Convert the byte array into a RequestBody
        val requestBody = pngByteArray.toRequestBody("image/png".toMediaTypeOrNull(),
            0, pngByteArray.size)
        return MultipartBody.Part.createFormData("image", "image.png", requestBody)
    }
}