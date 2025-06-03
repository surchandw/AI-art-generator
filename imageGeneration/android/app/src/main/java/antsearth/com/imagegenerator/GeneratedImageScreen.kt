package antsearth.com.imagegenerator

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import antsearth.com.imagegenerator.ads.BannerAd
import antsearth.com.imagegenerator.ads.RewardedAdManager
import antsearth.com.imagegenerator.data.Config
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "GeneratedImageScreen"

@Composable
fun GeneratedImageScreen(model: ImageGenViewModel, context: Context) {
    val text = model.imageDesc.value ?: ""
    var fileSaved by remember { mutableStateOf(false) }
    val config = remember { Config() }
    val rewardedAdManager = remember { RewardedAdManager(context) }
    var isRewardedAdLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isRewardedAdShown = remember { MutableStateFlow<Boolean>(false) }
    val imageData by model.imageBitmap.observeAsState()
    //val imageData = listOf(ImageBitmap.imageResource(id = R.drawable.text_to_image).asAndroidBitmap())

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

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Log.d(TAG, "ImageData size ${imageData?.size}")
            if (imageData != null && imageData!!.isNotEmpty()) {
                imageData?.get(0)?.let { image ->
                    Image(
                        bitmap = image.asImageBitmap(),
                        //painter = painterResource(id = R.drawable.text_to_image),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .size(350.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
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
                                                        saveImage(context, image,
                                                            Constants.TEXT_TO_IMAGE_FILENAME, Constants.PNG)
                                                        fileSaved = !fileSaved
                                                        Toast.makeText(context, "File saved", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "File already saved", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        } else {
                                            if (!fileSaved) {
                                                saveImage(
                                                    context, image,
                                                    Constants.TEXT_TO_IMAGE_FILENAME, Constants.PNG
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
                                            && !isRewardedAdShown.value) {
                                            isRewardedAdLoading = true
                                            rewardedAdManager.loadRewardedAd(
                                                onAdLoaded = {
                                                    isRewardedAdLoading = false
                                                    //isRewardedAdLoaded = true
                                                    Log.d(TAG, "Rewarded Ad loaded - callback")
                                                    rewardedAdManager.showRewardedAd(context as Activity,
                                                        onAdShown = { isRewardedAdShown.value = true })
                                                },
                                                onAdFailedToLoad = {
                                                    isRewardedAdLoading = false
                                                    //isRewardedAdLoaded = false
                                                }
                                            )
                                            isRewardedAdShown.collect {
                                                Log.d(TAG, "Collected state : $it")
                                                if (it) {
                                                    if (isPermissionGranted) {
                                                        shareImage(
                                                            context,
                                                            image,
                                                            Constants.TEXT_TO_IMAGE_FILENAME,
                                                            Constants.PNG
                                                        )
                                                    } else {
                                                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                                    }
                                                }
                                            }
                                            //delay(Constants.REWARDED_AD_WAIT_TIME)
                                        } else {
                                            if (isPermissionGranted) {
                                                shareImage(
                                                    context, image,
                                                    Constants.TEXT_TO_IMAGE_FILENAME, Constants.PNG
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
}