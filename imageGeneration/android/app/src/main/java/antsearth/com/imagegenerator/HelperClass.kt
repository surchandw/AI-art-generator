package antsearth.com.imagegenerator

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.util.Log
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.FileProvider
import com.google.android.gms.tasks.Task
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import java.io.FileInputStream

private const val TAG = "HelperClass"


class HelperClass {
    private lateinit var functions: FirebaseFunctions
    fun saveImageToExternalStorage(context: Context, bitmap: Bitmap,
                                   filename: String, fType: String): File? {
        val imagesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            Constants.IMAGE_DIR)
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val imageFile = File(imagesDir, filename)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(imageFile)
            when (fType) {
                Constants.JPEG -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                Constants.PNG -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            Log.d(TAG, "File saved")
        } catch (e: IOException) {
            Log.d(TAG, "Exception occurred: ${e.printStackTrace()}")
            return null
        } finally {
            fos?.close()
        }
        return imageFile
    }

    fun shareImage(context: Context, imageFile: File) {
        Log.d(TAG, "Inside the shareImage function")
        val uri = FileProvider.getUriForFile(context,
            "${context.packageName}.provider", imageFile)
        Log.d(TAG, "Uri: $uri")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        Log.d(TAG, "Starting chooser intent")
        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
        Log.d(TAG, "After chooser intent")
    }

    fun shareVideo(context: Context) {
        Log.d(TAG, "Inside the shareVideo function")
        val videoFile = File(context.cacheDir, Constants.VIDEO_FILENAME)
        val uri = FileProvider.getUriForFile(context,
            "${context.packageName}.provider", videoFile)
        Log.d(TAG, "Uri: $uri")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        Log.d(TAG, "Starting chooser intent")
        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
        Log.d(TAG, "After chooser intent")
    }
    fun copyFileFromCacheToPhotos(context: Context) {
        val cacheFile = File(context.cacheDir, Constants.VIDEO_FILENAME)
        Log.d(TAG, "Cache file path ${cacheFile.absolutePath}")
        val photosDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val destinationFile = File(photosDir, Constants.VIDEO_FILENAME)
        Log.d(TAG, "Destination file path ${destinationFile.absolutePath}")

        try {
            FileInputStream(cacheFile).use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }
            Log.d(TAG, "File copied successfully.")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "File copy failed.")
        }
    }
    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }

    fun getSTAronbaSho(model: ImageGenViewModel): Task<String> {
        functions = Firebase.functions
        return functions.getHttpsCallable("getSTAronbaSho")
            .call()
            .addOnFailureListener {
                Log.d(TAG, "Get sho call failed")
            }
            .addOnSuccessListener {
                Log.d(TAG, "Function call success")
                model.setShoData(it.data.toString())
                return@addOnSuccessListener
            }.continueWith {
                it.result.data.toString()
            }
    }

}


fun saveImage(context: Context, bitmap: Bitmap,
              filename: String, fType: String) {
    val helper = HelperClass()
    helper.saveImageToExternalStorage(context, bitmap,
        filename, fType)
}

fun shareImage(context: Context, bitmap: Bitmap,
               filename: String, fType: String) {
    val helper = HelperClass()
    val file = helper.saveImageToExternalStorage(context, bitmap,
        filename, fType)
    Log.d(TAG, "Share image file: $file")
    file?.let {
        helper.shareImage(context, file)
    }
}


class LeftPartialImageClip(pc: Float): Shape {
    private val portion = pc
    override fun createOutline(size: Size,
                               layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val path = Path().apply {
            //Reduce 0.01 from the image portion to keep a gap
            lineTo(size.width * (portion - 0.006f), 0f)
            lineTo(size.width * (portion - 0.006f), size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

class RightPartialImageClip(pc: Float): Shape {
    private val portion = pc
    override fun createOutline(size: Size,
                               layoutDirection: LayoutDirection,
                               density: Density
    ): Outline {
        Log.d(TAG, "Slider value $portion")
        val path = Path().apply {
            moveTo(size.width * portion, 0f)
            Log.d(TAG,"Width ${size.width} Height ${size.height}")
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(size.width * portion, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}