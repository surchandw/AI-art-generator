package antsearth.com.imagegenerator

import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Date


private const val TAG = "ImagePickerScreen"

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ImagePickerScreen(model: ImageGenViewModel,
                      onImageSelected: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var imageRes by remember { mutableIntStateOf(0) }
    var buttonClicked by remember { mutableStateOf(false) }
    //Selected image uri for both camera and selection of local file
    var selectedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }
    val images by model.images.collectAsState()
    val context = LocalContext.current
    //This section is for image selection from the local files
    val permissionLauncherForImages = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            model.loadImages(context.contentResolver)
        } else { //Need to show the buttons
            buttonClicked = false
        }
    }
    val getContent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult =  { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                if (getFileSizeFromUri(context, uri) >
                    Constants.MAX_ALLOWED_IMAGE_SIZE) {
                    showDialog = true
                    return@rememberLauncherForActivityResult
                }
                updateUri(uri, model, onImageSelected)
            }
        }
    )
    //This section is for taking picture using camera
    val file = remember { context.createImageFile() }
    val uri = remember { FileProvider.getUriForFile(
        java.util.Objects.requireNonNull(context),
        context.packageName + ".provider", file
    )}
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                selectedImageUri = uri
                if (getFileSizeFromUri(context, uri) >
                    Constants.MAX_ALLOWED_IMAGE_SIZE) {
                    showDialog = true
                    return@rememberLauncherForActivityResult
                }
                Log.d(TAG, "Captured image Uri: $uri")
                updateUri(uri, model, onImageSelected)
            } else {
                buttonClicked = false
            }
        }
    )


    val permissionLauncherForCamera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permission Granted")
            cameraLauncher.launch(uri)
        } else {
            buttonClicked = false
            Log.d(TAG, "Permission Denied")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
        //verticalArrangement = Arrangement.SpaceBetween
    ) {
        imageRes = if (model.imagePickerOrigin.value ==
            Constants.IMAGE_TO_VIDEO_ORIGIN) {
            R.drawable.image_to_video_example
        } else {
            R.drawable.enhance_image_example
        }
        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            .align(Alignment.CenterHorizontally)
            .height(300.dp)
            .fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .size(300.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.CenterHorizontally)
            .padding(start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!buttonClicked) {
                Button(onClick = {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.CAMERA
                    )
                    buttonClicked = true
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(uri)
                    } else {
                        permissionLauncherForCamera.launch(android.Manifest.permission.CAMERA)
                    }
                }, shape = RoundedCornerShape(12.dp))
                {
                    Text(text = stringResource(id = R.string.take_picture))
                }
                Button(
                    modifier = Modifier.padding(start = 4.dp),
                    onClick = {
                        /*val mimeType = "image/jpeg"
                    getContent.launch(
                        PickVisualMediaRequest(ActivityResultContracts.
                            .PickVisualMedia.SingleMimeType(mimeType))
                    )*/
                        val permissionCheckResult = ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                            model.loadImages(context.contentResolver)
                        } else {
                            permissionLauncherForImages.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        buttonClicked = true
                        Log.d(TAG, "Image loaded ${model.images.value.size}")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.select_image))
                }
            }
            if (showDialog) {
                RoundedCornerDialog(
                    title = stringResource(id = R.string.file_size_error),
                    message = stringResource(id = R.string.file_size_error_msg),
                    onDismiss = { showDialog = false }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize()
        ) {
            items(images.size) { index ->
                val imageUri = images[index]
                ImageThumbnail(imageUri) {
                    selectedImageUri = imageUri
                    updateUri(imageUri, model, onImageSelected)
                }
            }
        }
    }
    /*LazyVerticalGrid(
    columns = GridCells.Fixed(4),
    modifier = Modifier.fillMaxSize()
    ) {
        items(images.size) { index ->
            val imageUri = images[index]
            ImageThumbnail(imageUri) {
                selectedImageUri = imageUri
                updateUri(imageUri, model, onImageSelected)
            }
        }
    }*/
}

@Composable
fun ImageThumbnail(uri: Uri, onClick: () -> Unit) {
    val painter = rememberImagePainter(uri)
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .padding(4.dp)
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
            .clickable { onClick() }
    )
}

private fun updateUri(uri: Uri, model: ImageGenViewModel,
                      onImageSelected: () -> Unit) {
    val list = mutableListOf<Uri?>()
    list.add(uri)
    model.imageUri.value = list
    Log.d(TAG, "Selected Uri $uri")
    onImageSelected()
}
fun Context.createImageFile(): File {
    val timestamp = SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(Date())
    val imageFileName = "JPEG_" + timestamp + "_"
    val image = File.createTempFile(imageFileName,".jpg", externalCacheDir)
    return image
}

private fun getFileSizeFromUri(context: Context, uri: Uri): Long {
    var fileSize: Long = -1
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1) {
                fileSize = it.getLong(sizeIndex)
            }
        }
    }
    Log.d(TAG, "Image file size: $fileSize")
    return fileSize
}