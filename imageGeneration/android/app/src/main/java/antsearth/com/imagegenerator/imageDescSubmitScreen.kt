package antsearth.com.imagegenerator

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import antsearth.com.imagegenerator.ads.BannerAdView
import antsearth.com.imagegenerator.ads.RewardedAdManager
import antsearth.com.imagegenerator.data.Config
import antsearth.com.imagegenerator.data.ShoData
import antsearth.com.imagegenerator.stability.StabilityAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "ImageDescSubmitScreen"

@Composable
fun ImageDescSubmitScreen(model: ImageGenViewModel,
                          context: Context,
                          cancelExecution: () -> Unit,
                          successExecution: () -> Unit
) {
    Log.d(TAG, "ImageDescSubmitScreen called.....")
    var imageDescText by remember { mutableStateOf("") }
    var isTextFieldFocused by remember { mutableStateOf(false) }
    var isImageGenerating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showErrorDialog by remember { mutableStateOf(false) }
    var statusCode by remember { mutableIntStateOf(0) }
    val rewardedAdManager = remember { RewardedAdManager(context) }
    var isRewardedAdLoading by remember { mutableStateOf(false) }
    //var isRewardedAdLoaded by remember { mutableStateOf(false) }
    val stabilityAPI = remember { StabilityAPI() }
    val config = remember { Config() }
    val helper = remember { HelperClass() }

    // Obtain the insets for the IME (keyboard)
    //val imeBottomInset = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    //val keyboardHeight = remember { mutableStateOf(0.dp) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        //Log.d(TAG, "BottomInset: $imeBottomInset")
        //keyboardHeight.value = imeBottomInset
        Column(modifier = Modifier.fillMaxSize()) {
            TextField(
                value = imageDescText,
                onValueChange = { newText ->
                    imageDescText = newText },
                placeholder = { if (!isTextFieldFocused && imageDescText.isEmpty()) {
                    Text(text = stringResource(id = R.string.image_description))
                }},
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .onFocusChanged { focusState ->
                        isTextFieldFocused = focusState.isFocused
                    },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White,
                    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    cursorColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (isRewardedAdLoading || isImageGenerating) {
                Log.d(TAG, "Rewarded Ad loading")
                var msg: String = ""
                if (isRewardedAdLoading) {
                    msg = "Loading Ad"
                } else if (isImageGenerating) {
                    msg = "Generating image"
                }
                ShowCircularProgressBar(message = msg)
            } else {
                Row(modifier = Modifier
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            Log.d(TAG, "Button clicked")
                            coroutineScope.launch {
                                if (!helper.isInternetConnected(context)) {
                                    statusCode = Constants.NO_INTERNET_CONNECTION
                                    Log.d(TAG, "No internet connection")
                                    showErrorDialog = true
                                    return@launch
                                }
                                if (!model.getShoDataStatus()) {
                                    Log.d(TAG, "Getting sho")
                                    helper.getSTAronbaSho(model)
                                }
                                if (imageDescText.isEmpty() || imageDescText.trim().isEmpty()) {
                                    statusCode = Constants.NO_TEXT_TO_IMAGE_DESCRIPTION
                                    Log.d(TAG, "Image description text is empty")
                                    showErrorDialog = true
                                } else {
                                    if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED) {
                                        isRewardedAdLoading = true
                                        rewardedAdManager.loadRewardedAd(
                                            onAdLoaded = {
                                                isRewardedAdLoading = false
                                                //isRewardedAdLoaded = true
                                                Log.d(TAG, "Rewarded Ad loaded - callback")
                                                rewardedAdManager.showRewardedAd(context as Activity,
                                                    onAdShown = { })
                                            },
                                            onAdFailedToLoad = {
                                                isRewardedAdLoading = false
                                                //isRewardedAdLoaded = false
                                            }
                                        )
                                    }
                                    if (model.getShoDataStatus()) {
                                        Log.d(TAG, "Calling stability API")
                                        model.onDescTextSubmit(imageDescText)
                                        val apiKey = "Bearer ${model.getSho()}"
                                        isImageGenerating = true
                                        stabilityAPI.generateTextToImage(
                                            apiKey,
                                            imageDescText, context, model
                                        )
                                        model.statusCode.collectLatest { code ->
                                            delay(100)
                                            Log.d(TAG, "Status code: $code")
                                            statusCode = code
                                            if (statusCode == Constants.API_SUCCESS) {
                                                successExecution()
                                            } else {
                                                showErrorDialog = true
                                            }
                                        }


                                        /*val stableFamilyAPI = StableFamilyGeneration()
                                    val apiKey = viewModel.getSho()
                                    //engine - ultra/core/sd3
                                    stableFamilyAPI.generateTextToImage(apiKey, descText,
                                    Constants.StableFamilyUltra, mContext, viewModel)*/
                                    } else {
                                        Log.d(TAG, "Sho empty. Failed to call Stability API")
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        ButtonText(firstLine = "Generate Image",
                            secondLine = stringResource(id = R.string.watch_ad), config)
                    }
                    if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED) {
                        Button(
                            onClick = {},
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
            }
            if (showErrorDialog) {
                if (statusCode == Constants.NO_TEXT_TO_IMAGE_DESCRIPTION) {
                    ShowDialog(title = stringResource(id = R.string.no_image_description),
                        message = stringResource(id = R.string.no_image_description_msg)) {
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
            if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            //.padding(bottom = keyboardHeight.value),
                            .imePadding() //doesn't work properly below API level 30
                        //verticalArrangement = Arrangement.Bottom

                    ) {
                        //Log.d(TAG, "Keyboard height: ${keyboardHeight.value}")
                        Spacer(modifier = Modifier.weight(1f))
                        BannerAdView(adId = Constants.BANNER_AD_ID)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        BannerAdView(adId = Constants.BANNER_AD_ID)
                    }
                }
            }
        }
    }
}
