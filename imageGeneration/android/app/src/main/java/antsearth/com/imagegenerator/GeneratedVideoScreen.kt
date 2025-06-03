package antsearth.com.imagegenerator

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ByteArrayDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.ui.PlayerView
import antsearth.com.imagegenerator.ads.RewardedAdManager
import antsearth.com.imagegenerator.data.Config
import coil.compose.rememberImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

private const val TAG = "GeneratedVideoScreen"
@OptIn(UnstableApi::class)
@Composable
fun GeneratedVideoScreen(model: ImageGenViewModel, context: Context) {
    var isReadyToPlay by remember { mutableStateOf(false) }
    val config = remember { Config() }
    val rewardedAdManager = remember { RewardedAdManager(context) }
    var isRewardedAdLoading by remember { mutableStateOf(false) }
    val isRewardedAdShown = remember { MutableStateFlow<Boolean>(false) }
    val coroutineScope = rememberCoroutineScope()
    var fileSaved by remember { mutableStateOf(false) }
    val helper = remember { HelperClass() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (File(context.cacheDir, Constants.VIDEO_FILENAME).exists()) {
                helper.shareVideo(context)
            } else {
                Log.d(TAG, "Image file reading permission denied")
            }
        }
    }

    val isPermissionGranted = remember {
        ContextCompat.checkSelfPermission(
            context, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) 
    {
        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(300.dp)
                .fillMaxWidth()
        ) {
            model.imageUri.value?.let {
                if (it.isNotEmpty() && it[0] != null) {
                    Log.d(TAG, "File uri ${it[0]}")
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
        Box(
            modifier = Modifier
                .height(300.dp)
                .fillMaxSize()
                .padding(all = 12.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            val exoPlayer = remember { ExoPlayer.Builder(context).build() }
            val videoFileState = model.videoFileExistence.collectAsState()
            if (videoFileState.value) {
                val apiKey = model.getSho()
                val id = model.videoGenerationId.value
                val videoUrl = "https://api.stability.ai/v2beta/image-to-video/result/$id"
                //val videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                val headers = mapOf(
                    "accept" to "video/*",
                    "authorization" to "Bearer $apiKey"
                )

                val dataSourceFactory = DefaultHttpDataSource.Factory().apply {
                    setDefaultRequestProperties(headers)
                }

                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)))

                DisposableEffect(
                    AndroidView(factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            exoPlayer.setMediaSource(mediaSource)
                            exoPlayer.prepare()
                            exoPlayer.addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(playbackState: Int) {
                                    if (playbackState == Player.STATE_READY) {
                                        isReadyToPlay = true
                                    }
                                }
                            })
                        }
                    })
                ) {
                    onDispose {
                        exoPlayer.release()
                    }
                }
            }
            if (!isReadyToPlay || isRewardedAdLoading) {
                var msg = ""
                if (!isReadyToPlay) {
                    msg = "Loading Video"
                }
                if (isRewardedAdLoading) {
                    msg = "Loading Ad"
                }
                ShowCircularProgressBar(message = msg)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly)
        {
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
                                        helper.copyFileFromCacheToPhotos(context)
                                        fileSaved = !fileSaved
                                        Toast.makeText(context, "File saved", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "File already saved", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            if (!fileSaved) {
                                helper.copyFileFromCacheToPhotos(context)
                                fileSaved = !fileSaved
                                Toast.makeText(context, "File saved", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "File already saved", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                ButtonText(firstLine = stringResource(id = R.string.save_video),
                    secondLine = stringResource(id = R.string.watch_ad), config = config)
            }
            Button(onClick =
                {
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
                                    if (isPermissionGranted) {
                                        helper.shareVideo(context)
                                    } else {
                                        permissionLauncher.launch(READ_EXTERNAL_STORAGE)
                                    }
                                }
                            }
                        } else {
                            if (isPermissionGranted) {
                                helper.shareVideo(context)
                            } else {
                                permissionLauncher.launch(READ_EXTERNAL_STORAGE)
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                ButtonText(firstLine = stringResource(id = R.string.share_video),
                    secondLine = stringResource(id = R.string.watch_ad), config = config)
            }
        }
    }
}