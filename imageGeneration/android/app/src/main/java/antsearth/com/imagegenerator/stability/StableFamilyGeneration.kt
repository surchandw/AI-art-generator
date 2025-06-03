package antsearth.com.imagegenerator.stability

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import antsearth.com.imagegenerator.Constants
import antsearth.com.imagegenerator.ImageGenViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
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
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


private const val TAG = "StableFamilyGeneration"
class StableFamilyGeneration {
    interface StabilityAIEndpoint {
        //("v2beta/stable-image/generate/ultra")
        @Headers(
            "accept: image/*"
        )
        @Multipart
        @POST
        fun generateImage(
            @Header("authorization") authorization: String,
            @Url url: String,
            @Part("prompt") prompt: RequestBody,
            @Part("output_format") outputFormat: RequestBody,
            //@Part image: MultipartBody.Part
        ): Call<ResponseBody> // Adjust the response type as per actual API response
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

        val api: StabilityAIEndpoint by lazy {
            retrofit.create(StabilityAIEndpoint::class.java)
        }
    }

    fun generateTextToImage(apiKey: String, prompt: String, engine: String,
                            context: Context, model: ImageGenViewModel) {
        val api = RetrofitClient.api
        val url = "v2beta/stable-image/generate/$engine"
        val outputFormat = if (engine == Constants.StableFamilySD3) {
            Constants.JPEG
        } else {
            Constants.WEBP
        }

        // Prepare the image file
        //val imageFile = File(context.cacheDir, "none.txt")
        //val imageRequestBody = imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        //val imagePart = MultipartBody.Part.createFormData("none", imageFile.name, imageRequestBody)

        // Prepare the prompt and output format as RequestBody
        val promptBody = prompt.toRequestBody("text/plain".toMediaTypeOrNull())
        val outputFormatBody = outputFormat.toRequestBody("text/plain".toMediaTypeOrNull())

        // Make the API call
        val call = api.generateImage("Bearer $apiKey", url,
            promptBody, outputFormatBody)
        call.enqueue(object: Callback<ResponseBody> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d(TAG, "Response status code: ${response.headers()}")
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        val byteData = data.bytes()
                        val bitmap = BitmapFactory.decodeByteArray(byteData, 0, byteData.size)
                        val list = mutableListOf<Bitmap>()
                        list.add(bitmap)
                        model.imageBitmap.value = list
                        val outputFile = File(context.cacheDir, "temp.${outputFormat}")
                        FileOutputStream(outputFile).use { outputStream ->
                            outputStream.write(byteData)
                        }

                        Log.d(TAG, "Image downloaded successfully to: ${outputFile.absolutePath}")
                    }
                } else {
                    throw Exception("Failed to download image: ${response.code()}: ${response.errorBody()?.string() ?: "Unknown error"}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "API call failed: ${t.message}")
            }
        })
    }
}










