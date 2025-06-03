package antsearth.com.imagegenerator

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import antsearth.com.imagegenerator.ads.BannerAd
import antsearth.com.imagegenerator.ads.RewardedAdManager
import antsearth.com.imagegenerator.data.Config
import antsearth.com.imagegenerator.stability.ImageToVideo
import coil.compose.rememberImagePainter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect

private const val TAG = "GenerateVideoScreen"
@Composable
fun GenerateVideoScreen(model: ImageGenViewModel,
                        context: Context,
                        cancelExecution: () -> Unit,
                        successExecution: () -> Unit) {
    //Hide the button and slider to go to the next button
    var submitImageButtonHide by remember { mutableStateOf(false) }
    var cfgScaleValue by remember { mutableFloatStateOf(1.8f) }
    var motionBucketValue by remember { mutableFloatStateOf( 127f) }
    val range = 1..255
    //Flag to show the circular progress bar
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    //Flag to hide fetch button when progress bar is showing
    var fetchVideoButtonHide by remember { mutableStateOf(false) }
    //Text message to show the video wait message
    var videoWaitMessage by remember { mutableStateOf("") }
    //Dialog variables
    var showErrorDialog by remember { mutableStateOf(false) }
    var statusCode by remember { mutableIntStateOf(0) }
    val config = remember { Config() }
    val helper = remember { HelperClass() }
    val rewardedAdManager = remember { RewardedAdManager(context) }
    var isRewardedAdLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(300.dp)
                .fillMaxWidth()
        ) {
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
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (!submitImageButtonHide) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "CFG Scale", textAlign = TextAlign.Start)
                    Text(
                        text = cfgScaleValue.toString(), textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 64.dp)
                    )
                }
                Slider(value = cfgScaleValue, onValueChange = { cfgScaleValue = it },
                    valueRange = 0f..10f, onValueChangeFinished = {
                        model.updateCfgScale(cfgScaleValue)
                    }
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Motion Scale", textAlign = TextAlign.Start)
                    Text(
                        text = motionBucketValue.toInt().toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 64.dp)
                    )
                }
                Slider(value = motionBucketValue, onValueChange = { motionBucketValue = it },
                    valueRange = range.first.toFloat()..range.last.toFloat(),
                    steps = range.last - range.first - 1, onValueChangeFinished = {
                        model.updateMotionBucketId(motionBucketValue.toInt())
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val imgToVideo = ImageToVideo()
            val apiKey = model.getSho()
            if (!submitImageButtonHide) {
                Button(
                    onClick = {
                        if (!helper.isInternetConnected(context)) {
                            statusCode = Constants.NO_INTERNET_CONNECTION
                            Log.d(TAG, "No internet connection")
                            showErrorDialog = true
                            return@Button
                        }
                        if (!model.getShoDataStatus()) {
                            Log.d(TAG, "Getting sho")
                            helper.getSTAronbaSho(model)
                        }
                        submitImageButtonHide = !submitImageButtonHide
                        if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED) {
                            isRewardedAdLoading = true
                            //Load the second rewarded Ad
                            rewardedAdManager.loadSecondRewardedAd()
                            rewardedAdManager.loadRewardedAd(
                                onAdLoaded = {
                                    isRewardedAdLoading = false
                                    Log.d(TAG, "Rewarded Ad loaded - callback")
                                    rewardedAdManager.showRewardedAd(context as Activity,
                                        onAdClosed = {
                                            if (rewardedAdManager.isSecondRewardedAdLoaded()) {
                                                rewardedAdManager.showSecondRewardedAd(context)
                                            }
                                        })
                                },
                                onAdFailedToLoad = {
                                    isRewardedAdLoading = false
                                }
                            )
                        }
                        model.imageUri.value?.let { it ->
                            if (it.isNotEmpty() && it[0] != null) {
                                imgToVideo.generateImageToVideoPost(
                                    apiKey, it[0]!!, context, model
                                )
                                coroutineScope.launch {
                                    model.statusCode.collect { code ->
                                        statusCode = code
                                        if (statusCode != Constants.API_SUCCESS) {
                                            showErrorDialog = true
                                        }
                                    }
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    ButtonText(firstLine = "Generate Video",
                        secondLine = stringResource(id = R.string.watch_two_ads), config)
                }
                if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED) {
                    Button(
                        onClick = { /*TODO*/ },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        ButtonText(
                            firstLine = stringResource(id = R.string.subscribe),
                            secondLine = stringResource(id = R.string.more_feature),
                            config
                        )
                    }
                }
            } else {
                if (isLoading || isRewardedAdLoading) {
                    var msg = ""
                    if (isRewardedAdLoading) {
                        msg = "Loading Ad"
                    } else if (isLoading) {
                        msg = videoWaitMessage
                    }
                    ShowCircularProgressBar(message = msg)
                } else {
                    if (!fetchVideoButtonHide) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    Log.d(TAG, "Status code set to 0")
                                    isLoading = true
                                    fetchVideoButtonHide = true
                                    videoWaitMessage = "Generating video"
                                    delay(Constants.VIDEO_OR_IMAGE_GENERATION_WAIT_TIME)
                                    //Overwrite the previous ImageToVideo.generateImageToVideoPost()
                                    //success statusCode
                                    model.setStatusCode(0)
                                    videoWaitMessage = "Checking video completion"
                                    delay(Constants.VIDEO_OR_IMAGE_GENERATION_WAIT_TIME)
                                    imgToVideo.generateImageToVideoGet(apiKey, context, model)
                                    model.statusCode.collect {
                                        Log.d(TAG, "Status code: $it")
                                        statusCode = it
                                        when (statusCode) {
                                            Constants.API_SUCCESS -> {
                                                Log.d(TAG, "Navigating to Generated video screen")
                                                successExecution()
                                            }

                                            Constants.API_VIDEO_OR_IMAGE_GENERATION_IN_PROGRESS -> {
                                                Log.d(TAG, "Video generation in progress ...")
                                                var escapeLoop = false
                                                videoWaitMessage = "Checking again ..."
                                                while (!escapeLoop) {
                                                    Log.d(TAG, "Inside the while loop ...")
                                                    delay(Constants.VIDEO_OR_IMAGE_GENERATION_WAIT_TIME)
                                                    imgToVideo.generateImageToVideoGet(
                                                        apiKey, context, model
                                                    )
                                                    model.statusCode.collect { code ->
                                                        Log.d(
                                                            TAG,
                                                            "Return code inside the while loop $code"
                                                        )
                                                        if (code == Constants.API_SUCCESS) {
                                                            escapeLoop = true
                                                            Log.d(
                                                                TAG,
                                                                "Navigating to generated video screen"
                                                            )
                                                            successExecution()
                                                        }
                                                    }
                                                }
                                            }

                                            else -> {
                                                showErrorDialog = true
                                            }
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = stringResource(id = R.string.fetch_video))
                        }
                    }
                }
            }
        }
        if (showErrorDialog) {
            if (statusCode == Constants.NO_INTERNET_CONNECTION) {
                ShowDialog(
                    stringResource(id = R.string.no_internet_connection),
                    stringResource(id = R.string.no_internet_connection_msg)) {
                    showErrorDialog = false
                }
            } else {
                ShowErrorDialog(statusCode, cancelExecution)
            }
        }
        if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED) {
            BannerAd(adId = Constants.BANNER_AD_ID)
        }
    }
}
