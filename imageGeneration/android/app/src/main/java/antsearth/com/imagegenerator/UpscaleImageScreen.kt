package antsearth.com.imagegenerator

import android.app.Activity
import android.content.Context
import android.util.Log
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import antsearth.com.imagegenerator.ads.RewardedAdManager
import antsearth.com.imagegenerator.data.Config
import antsearth.com.imagegenerator.stability.UpscaleAdvanced
import coil.compose.rememberImagePainter
import kotlinx.coroutines.launch


private const val TAG = "EnhanceImageScreen"
@Composable
fun UpscaleImageScreen(model: ImageGenViewModel, context: Context,
                       cancelExecution: () -> Unit, successExecution: () -> Unit) {
    var creativityValue by remember { mutableFloatStateOf(0.35f) }
    var prompt by remember { mutableStateOf("") }
    var isPromptFocussed by remember { mutableStateOf(false) }
    var negativePrompt by remember { mutableStateOf("") }
    var isNegativePromptFocussed by remember { mutableStateOf(false) }
    //Flag to show the circular progress bar
    var isImageEnhancing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    //Dialog variables
    var showErrorDialog by remember { mutableStateOf(false) }
    var statusCode by remember { mutableIntStateOf(0) }
    val config = remember { Config() }
    val helper = remember { HelperClass() }
    val rewardedAdManager = remember { RewardedAdManager(context) }
    var isRewardedAdLoading by remember { mutableStateOf(false) }


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
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isImageEnhancing || isRewardedAdLoading) {
                var msg: String = ""
                if (isRewardedAdLoading) {
                    msg = "Loading Ad"
                } else if (isImageEnhancing) {
                    msg = "Enhancing image"
                }
                ShowCircularProgressBar(message = msg)
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Creativity", textAlign = TextAlign.Start,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = creativityValue.toString(), textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 70.dp)
                    )
                }
                Slider(value = creativityValue, onValueChange = { creativityValue = it },
                    valueRange = 0.2f..0.5f, onValueChangeFinished = {
                        model.updateCreativityValue(creativityValue)
                    }
                )
                TextField(
                    value = prompt, onValueChange = { newText -> prompt = newText },
                    placeholder = {
                        if (!isPromptFocussed && prompt.isEmpty()) {
                            Text(text = stringResource(id = R.string.prompt))
                        }},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                        .onFocusEvent { focusState ->
                            isPromptFocussed = focusState.isFocused
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        cursorColor = androidx.compose.ui.graphics.Color.Black,
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                TextField(
                    value = negativePrompt, onValueChange = { newText -> negativePrompt = newText },
                    placeholder = {
                        if (!isNegativePromptFocussed && negativePrompt.isEmpty()) {
                            Text(text = stringResource(id = R.string.negative_prompt))
                        }},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 4.dp, top = 4.dp)
                        .onFocusEvent { focusState ->
                            isNegativePromptFocussed = focusState.isFocused
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        cursorColor = androidx.compose.ui.graphics.Color.Black,
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = {
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
                if (prompt.isEmpty() || prompt.trim().isEmpty()) {
                    statusCode = Constants.NO_ENHANCE_IMAGE_DESCRIPTION
                    Log.d(TAG, "Prompt text is empty")
                    showErrorDialog = true
                } else {
                    coroutineScope.launch {
                        if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED) {
                            isRewardedAdLoading = true
                            rewardedAdManager.loadRewardedAd(
                                onAdLoaded = {
                                    isRewardedAdLoading = false
                                    Log.d(TAG, "Rewarded Ad loaded - callback")
                                    //Load the second rewarded Ad
                                    rewardedAdManager.loadSecondRewardedAd()
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
                        model.updateUpscaleParameters(prompt, negativePrompt, creativityValue)
                        Log.d(TAG, "Image enhance screen invoked")
                        val upscaleImage = UpscaleAdvanced()
                        Log.d(TAG, "Upscale instance created")
                        val apiKey = model.getSho()
                        Log.d(TAG, "Got secret key")
                        model.imageUri.value?.let {
                            if (it.isNotEmpty() && it[0] != null) {
                                upscaleImage.upscaleImage(apiKey, context, it[0]!!, model)
                                //Show the progress bar
                                isImageEnhancing = true
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
                }
            }, shape = RoundedCornerShape(12.dp)
            ) {
                ButtonText(firstLine = "Enhance Image",
                    secondLine = stringResource(id = R.string.watch_two_ads),
                    config)
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
        }
        if (showErrorDialog) {
            if (statusCode == Constants.NO_ENHANCE_IMAGE_DESCRIPTION) {
                ShowDialog(title = stringResource(id = R.string.no_enhance_image_description),
                    message = stringResource(id = R.string.no_enhance_image_description_msg)) {
                    showErrorDialog = false
                }
            } else if (statusCode == Constants.NO_INTERNET_CONNECTION) {
                ShowDialog(
                    stringResource(id = R.string.no_internet_connection),
                    stringResource(id = R.string.no_internet_connection_msg)) {
                    showErrorDialog = false
                }
            } else {
                ShowErrorDialog(statusCode, cancelExecution)
            }
        }
    }
}