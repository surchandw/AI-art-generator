package antsearth.com.imagegenerator

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import antsearth.com.imagegenerator.data.ShoData
import antsearth.com.imagegenerator.data.TextPrompt
import antsearth.com.imagegenerator.data.UpscaleParameters
import antsearth.com.imagegenerator.data.VideoGenerationParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "ImageGenViewModel"
class ImageGenViewModel: ViewModel() {
    //1. Text to image generation - text description
    private var _imageDesc = MutableLiveData("")
    val imageDesc: LiveData<String> = _imageDesc

    fun onDescTextSubmit(descText: String) {
        _imageDesc.value = descText
    }

    //2. Image to Video generation - video generation Id
    private var _videoGenerationId = MutableLiveData("")
    val videoGenerationId: LiveData<String> = _videoGenerationId

    fun setVideoGenerationId(id: String) {
        _videoGenerationId.value = id
    }

    //3. Image picker screen is shared between image to video generation
    // and image enhancer screen
    private var _imagePickerOrigin = MutableLiveData("")
    val imagePickerOrigin: LiveData<String> = _imagePickerOrigin

    fun setImagePickerOrigin(id: String) {
        _imagePickerOrigin.value = id
    }

    //4. Generated image Uri
    var imageUri: MutableLiveData<List<Uri?>>
            = MutableLiveData(mutableListOf())

    //5. API key
    private var _shoData = MutableLiveData(ShoData(false, ""))
    val shoData: LiveData<ShoData> = _shoData

    fun setShoData(sho: String) {
        _shoData.value?.aronbaSho = sho
        _shoData.value?.fetched = true
    }
    fun getShoDataStatus(): Boolean {
        return shoData.value?.fetched ?: false
    }
    fun getSho(): String {
        return shoData.value?.aronbaSho ?: ""
    }

    //6. Text to image generation bitmap and upscale image storage
    var imageBitmap: MutableLiveData<List<Bitmap>>
                        = MutableLiveData(mutableListOf())

    //7. Generated video file existence flag
    private val _videoFileExistence = MutableStateFlow(false)
    val videoFileExistence: StateFlow<Boolean> = _videoFileExistence.asStateFlow()

    fun setVideoFileExistence(newState: Boolean) {
        _videoFileExistence.value = newState
    }

    //8. Video generation parameters
    private val _videoGenerationParameters = MutableStateFlow(
        VideoGenerationParameters()
    )
    val videoGenerationParameters: StateFlow<VideoGenerationParameters> =
        _videoGenerationParameters.asStateFlow()

    fun updateCfgScale(cfgScaleValue: Float) {
        _videoGenerationParameters.update { currentValue ->
            currentValue.copy(
                cfgScale = cfgScaleValue
            )
        }
    }
    fun updateMotionBucketId(bucketIdValue: Int) {
        _videoGenerationParameters.update { currentValue ->
            currentValue.copy(
                motionBucketId = bucketIdValue
            )
        }
    }

    //9. Retrofit HTTP API call status
    private val _statusCode = MutableStateFlow(0)
    val statusCode: StateFlow<Int> = _statusCode.asStateFlow()

    fun setStatusCode(code: Int) {
        _statusCode.value = code
    }

    //10. Image upscale parameters
    private val _upscaleParameters = MutableStateFlow(
        UpscaleParameters()
    )
    val upscaleParameters: StateFlow<UpscaleParameters> =
        _upscaleParameters.asStateFlow()

    fun updateCreativityValue(value: Float) {
        _upscaleParameters.update { currentValue ->
            currentValue.copy (creativity = value)
        }
    }

    fun updateUpscaleParameters(prompt: String, negPrompt: String, creativity: Float) {
        _upscaleParameters.update { currentValue ->
            currentValue.copy(
                prompt = prompt,
                negativePrompt = negPrompt,
                creativity = creativity
            )
        }
    }

    //11. Ersgan model image upscale parameter
    private var _imageWidth = MutableLiveData(0)
    val imageWidth: LiveData<Int> = _imageWidth

    fun updateWidthValue(width: Int) {
        _imageWidth.value = width
    }

    //12. Image picker image Uri
    private var _images = MutableStateFlow<List<Uri>>(emptyList())
    val images: StateFlow<List<Uri>> = _images
    fun loadImages(contentResolver: ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "Inside loadImages")
            val imageUris = mutableListOf<Uri>()
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = "${MediaStore.Images.Media.MIME_TYPE} = ?"
            val selectionArgs = arrayOf("image/jpeg")
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            contentResolver.query(queryUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                Log.d(TAG, "Inside the query")
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    Log.d(TAG, "Image uri: ${cursor.count}")
                    val id = cursor.getLong(idColumn)
                    val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                    imageUris.add(contentUri)
                }
            }

            _images.value = imageUris
        }
    }

    //2. Image to Video generation - video generation Id
    private var _imageEnhanceId = MutableLiveData("")
    val imageEnhanceId: LiveData<String> = _imageEnhanceId

    fun setImageEnhanceId(id: String) {
        _imageEnhanceId.value = id
    }

    fun resetValues() {
        _imageDesc = MutableLiveData("")
        Log.d(TAG, "Description Text: ${_imageDesc.value}")
        _videoGenerationId = MutableLiveData("")
        Log.d(TAG, "Video generation Id: ${_videoGenerationId.value}")
        _imagePickerOrigin = MutableLiveData("")
        Log.d(TAG, "Image picker origin: ${_imagePickerOrigin.value}")
        imageUri = MutableLiveData(mutableListOf())
        Log.d(TAG, "Image uri empty: ${imageUri.value?.isEmpty()}")
        imageBitmap = MutableLiveData(mutableListOf())
        Log.d(TAG, "imageBitmap empty: ${imageBitmap.value?.isEmpty()}")
        _videoFileExistence.value = false
        Log.d(TAG, "Video file existence value ${_videoFileExistence.value}")
        _statusCode.value = 0
        Log.d(TAG, "Status code value: ${_statusCode.value}")
        _images = MutableStateFlow(emptyList())
        _videoGenerationParameters.update{
            it.copy(
                cfgScale = 1.8f,
                motionBucketId = 127
            )
        }
        _imageEnhanceId = MutableLiveData("")
        Log.d(TAG, "Image enhance Id: ${_imageEnhanceId.value}")
    }
}