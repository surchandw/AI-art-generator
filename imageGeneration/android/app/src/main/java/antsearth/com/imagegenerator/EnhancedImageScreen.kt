package antsearth.com.imagegenerator

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import antsearth.com.imagegenerator.ads.BannerAd
import antsearth.com.imagegenerator.ads.RewardedAdManager
import antsearth.com.imagegenerator.data.Config
import coil.compose.rememberImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private const val TAG = "EnhancedImageScreen"
@Composable
fun EnhancedImageScreen(model: ImageGenViewModel, context: Context) {
    val config = remember { Config() }
    val rewardedAdManager = remember { RewardedAdManager(context) }
    var isRewardedAdLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isRewardedAdShown = remember { MutableStateFlow<Boolean>(false) }
    var fileSaved by remember { mutableStateOf(false) }
    val imageData by model.imageBitmap.observeAsState()
    //val imageData = listOf(ImageBitmap.imageResource(id = R.drawable.enhance_image).asAndroidBitmap())

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (imageData != null && imageData!!.isNotEmpty()) {
                imageData?.get(0)?.let { image ->
                    shareImage(
                        context, image,
                        Constants.TEXT_TO_IMAGE_FILENAME, Constants.PNG
                    )
                }
            } else {
                Log.d(TAG, "Image file reading permission denied")
            }
        }
    }

    val isPermissionGranted = remember {
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }

    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        var sliderPosition by remember { mutableFloatStateOf(0.5f) }

        Log.d(TAG, "ImageData size ${imageData?.size}")
        Box(modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .height(300.dp)
            .fillMaxWidth(),
            ) {
            if (imageData != null && imageData!!.isNotEmpty()) {
                imageData?.get(0)?.let { enhancedBitmap ->
                    Image(painter = rememberImagePainter(model.imageUri.value?.get(0)),
                    //Image(painter = rememberImagePainter(R.drawable.enhance_image),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(12.dp)
                            .height(300.dp)
                            .fillMaxWidth(1.0f)
                            .clip(RoundedCornerShape(16.dp))
                            .clip(LeftPartialImageClip(sliderPosition))
                    )
                    if (sliderPosition > 0.20f) {
                        Box(modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 56.dp, top = 16.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            .padding(start = 4.dp, top = 1.dp, bottom = 1.dp, end = 4.dp)) {
                            Text(
                                text = stringResource(id = R.string.before),
                                color = Color.Black, fontSize = 12.sp
                            )
                        }
                    }
                    Image(
                        bitmap = enhancedBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(12.dp)
                            .height(300.dp)
                            .fillMaxWidth(1.0f)
                            .clip(RoundedCornerShape(16.dp))
                            .clip(RightPartialImageClip(sliderPosition))
                    )
                    if (sliderPosition < 0.75f) {
                        Box(modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 56.dp, top = 16.dp)
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            .padding(start = 10.dp, top = 1.dp, bottom = 1.dp, end = 10.dp)) {
                            Text(
                                text = stringResource(id = R.string.after),
                                color = Color.Black, fontSize = 12.sp
                            )
                        }
                    }
                    // Slider at the bottom of the screen
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(start = 4.dp, end = 4.dp, bottom = 24.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (imageData != null && imageData!!.isNotEmpty()) {
            imageData?.get(0)?.let { enhancedBitmap ->
                if (isRewardedAdLoading) {
                    Log.d(TAG, "Rewarded Ad loading")
                    ShowCircularProgressBar(message = "Loading Ad")
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED
                                        && !isRewardedAdShown.value) {
                                        isRewardedAdLoading = true
                                        rewardedAdManager.loadRewardedAd(
                                            onAdLoaded = {
                                                isRewardedAdLoading = false
                                                Log.d(TAG, "Rewarded Ad loaded - callback")
                                                rewardedAdManager.showRewardedAd(context as Activity,
                                                    onAdShown = { isRewardedAdShown.value = true })
                                            },
                                            onAdFailedToLoad = {
                                                isRewardedAdLoading = false
                                            }
                                        )
                                        isRewardedAdShown.collect {
                                            if (it) {
                                                if (!fileSaved) {
                                                    saveImage(
                                                        context, enhancedBitmap,
                                                        Constants.ENHANCE_FILENAME, Constants.PNG
                                                    )
                                                    fileSaved = !fileSaved
                                                    Toast.makeText(
                                                        context,
                                                        "File saved",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "File already saved",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    } else {
                                        if (!fileSaved) {
                                            saveImage(
                                                context, enhancedBitmap,
                                                Constants.ENHANCE_FILENAME, Constants.PNG
                                            )
                                            fileSaved = !fileSaved
                                            Toast.makeText(
                                                context,
                                                "File saved",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "File already saved",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            ButtonText(
                                firstLine = stringResource(id = R.string.save_image),
                                secondLine = stringResource(id = R.string.watch_ad),
                                config = config
                            )
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED
                                        && !isRewardedAdShown.value
                                    ) {
                                        isRewardedAdLoading = true
                                        rewardedAdManager.loadRewardedAd(
                                            onAdLoaded = {
                                                isRewardedAdLoading = false
                                                Log.d(TAG, "Rewarded Ad loaded - callback")
                                                rewardedAdManager.showRewardedAd(context as Activity,
                                                    onAdShown = { isRewardedAdShown.value = true })
                                            },
                                            onAdFailedToLoad = {
                                                isRewardedAdLoading = false
                                            }
                                        )
                                        isRewardedAdShown.collect {
                                            Log.d(TAG, "Collected state : $it")
                                            if (it) {
                                                if (isPermissionGranted) {
                                                    shareImage(
                                                        context,
                                                        enhancedBitmap,
                                                        Constants.ENHANCE_FILENAME,
                                                        Constants.PNG
                                                    )
                                                } else {
                                                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                                }
                                            }
                                        }
                                    } else {
                                        if (isPermissionGranted) {
                                            shareImage(
                                                context, enhancedBitmap,
                                                Constants.ENHANCE_FILENAME, Constants.PNG
                                            )
                                        } else {
                                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            ButtonText(
                                firstLine = stringResource(id = R.string.share_image),
                                secondLine = stringResource(id = R.string.watch_ad),
                                config = config
                            )
                        }
                    }
                }
            }
        }
        if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED) {
            BannerAd(adId = Constants.BANNER_AD_ID)
        }
    }
}