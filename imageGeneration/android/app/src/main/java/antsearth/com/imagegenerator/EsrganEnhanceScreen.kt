package antsearth.com.imagegenerator

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import antsearth.com.imagegenerator.stability.UpscaleAdvanced
import antsearth.com.imagegenerator.stability.UpscaleImage
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import kotlinx.coroutines.launch


private const val TAG = "EsrganEnhanceScreen"
@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EsrganEnhanceImageScreen(model: ImageGenViewModel, context: Context,
                       cancelExecution: () -> Unit, successExecution: () -> Unit) {
    var width by remember { mutableFloatStateOf(512.0f) }
    //Flag to show the circular progress bar
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    //Dialog variables
    var showErrorDialog by remember { mutableStateOf(false) }
    var statusCode by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Column(modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .height(300.dp)
            .fillMaxWidth()) {
            model.imageUri.value?.let {
                if (it.isNotEmpty() && it[0] != null) {
                    Image(
                        painter = rememberImagePainter(it[0]),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .size(300.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                ShowCircularProgressBar(message = "Enhancing image")
            } else { //Hide all while progress bar is showing
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Width/Height", textAlign = TextAlign.Start,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = width.toInt().toString(), textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 70.dp)
                    )
                }
                Slider(value = width, onValueChange = { width = it },
                    valueRange = 512.0f..2048.0f, onValueChangeFinished = {
                        model.updateWidthValue(width.toInt())
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = {
                Log.d(TAG, "Image enhance screen invoked")
                val upscaleImage = UpscaleImage()
                Log.d(TAG, "Upscale instance created")
                val apiKey = model.getSho()
                Log.d(TAG, "Got secret key")
                model.imageUri.value?.let {
                    if (it.isNotEmpty() && it[0] != null) {
                        upscaleImage.upscaleImage(apiKey, it[0]!!, context, model)
                        coroutineScope.launch {
                            //Show the progress bar
                            isLoading = true
                            Log.d(TAG, "Progress bar showing...")
                            model.statusCode.collect { code ->
                                Log.d(TAG, "Status code: $code")
                                statusCode = code
                                if (statusCode == Constants.API_SUCCESS) {
                                    successExecution()
                                } else {
                                    showErrorDialog = true
                                }
                            }
                        }
                    }
                }
            }) {
                Text(text = stringResource(id = R.string.watch_ad))
            }
            Button(onClick = { /*TODO*/ }) {
                Text(text = stringResource(id = R.string.subscribe))
            }
        }
        if (showErrorDialog) {
            ShowErrorDialog(statusCode, cancelExecution)
        }
    }
}
