package antsearth.com.imagegenerator.stability

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import antsearth.com.imagegenerator.Constants
import antsearth.com.imagegenerator.ImageGenViewModel
import antsearth.com.imagegenerator.data.GenerateImageRequest
import antsearth.com.imagegenerator.data.GenerateImageResponse
import antsearth.com.imagegenerator.data.TextPrompt
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64
import java.util.concurrent.TimeUnit

private const val TAG = "StabilityAPI"
class StabilityAPI {
    interface StabilityApiService {
        @Headers(
            "Accept: application/json",
            "Content-Type: application/json"
        )
        @POST
        fun generateImage(
            @Header("Authorization") key: String,
            @Url url: String,
            @Body requestBody: GenerateImageRequest
        ): Call<GenerateImageResponse>
    }

    object StabilityApiClient {
        private const val BASE_URL = "https://api.stability.ai"
        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT, TimeUnit.SECONDS)
            .build()
        //TODO - Make retrofit private
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val service: StabilityApiService = retrofit.create(StabilityApiService::class.java)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun generateTextToImage(apiKey: String, promptText: String,
                            mContext: Context, model: ImageGenViewModel) {
        val engineId = "stable-diffusion-xl-1024-v1-0"
        //val engineId = "stable-diffusion-v1-6"
        val apiService = StabilityApiClient.service
        var call: Call<GenerateImageResponse>

        Log.d(TAG, "Request url: ${StabilityApiClient.retrofit}")
        if (apiKey.isBlank()) {
            throw Exception("Missing Stability API key.")
        }
        val textPrompts = listOf(TextPrompt(promptText))
        val requestBody = GenerateImageRequest(
            text_prompts = textPrompts,
            cfg_scale = 7,
            height = 1024,
            width = 1024,
            samples = 1,
            steps = 30
        )
        GlobalScope.launch {
            call = apiService.generateImage(
                apiKey,
                "/v1/generation/$engineId/text-to-image",
                requestBody
            )
            call.enqueue(object : Callback<GenerateImageResponse> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<GenerateImageResponse>,
                    response: Response<GenerateImageResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        Log.d(TAG, "Response successful")
                        if (data != null) {
                            for ((i, artifact) in data.artifacts.withIndex()) {
                                val byteData = Base64.getDecoder().decode(artifact.base64)
                                Log.d(TAG, "Byte data size: $byteData")
                                val bitmapData = BitmapFactory.decodeByteArray(
                                    byteData, 0, byteData.size)
                                val list = mutableListOf<Bitmap>()
                                list.add(bitmapData)
                                model.imageBitmap.value = list
                                saveImage(artifact.base64, i, mContext)
                                Log.d(TAG, "Image parts $i")
                            }
                        } else {
                            Log.d(TAG, "Data is null")
                        }
                    } else {
                        Log.d(TAG, "Non-200 response: ${response.errorBody()?.string()}")
                    }
                    model.setStatusCode(response.code())
                }

                override fun onFailure(call: Call<GenerateImageResponse>, t: Throwable) {
                    Log.d(TAG, "API call failed: ${t.message}")
                    model.setStatusCode(Constants.API_INTERNAL_ERROR)
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveImage(base64Data: String, index: Int, mContext: Context) {
        try {
            val decodedBytes = Base64.getDecoder().decode(base64Data)
            val fileName = Constants.TEXT_TO_IMAGE_FILENAME
            FileOutputStream(File(mContext.cacheDir.absolutePath, fileName)).use { fos ->
                fos.write(decodedBytes)
                fos.flush()
                fos.close()
                Log.d(TAG, "File $fileName saved")
            }
        } catch(e : Exception) {
            e.printStackTrace()
        }
    }
}