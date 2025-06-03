package antsearth.com.imagegenerator

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private const val TAG = "ImageGenApp"

enum class ImageGenAppScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    ImageGenerator(title = R.string.generate_image),
    GeneratedImage(title = R.string.generated_image),
    ImagePicker(title = R.string.image_picker),
    EnhanceImage(title = R.string.enhance_image),
    EnhancedImage(title = R.string.enhanced_image),
    VideoGenerator(title = R.string.generate_video),
    GeneratedVideo(title = R.string.generated_video),
    MenuScreen(title = R.string.settings),
    PurchaseScreen(title = R.string.subscribe)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageGenAppBar(
    currentScreen: ImageGenAppScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(id = currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },
        actions = { MenuScreen(navController) }
    )
}

@Composable
fun ImageGenApp(mContext: Context,
                viewModel: ImageGenViewModel = viewModel(),
                navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = ImageGenAppScreen.valueOf(
        backStackEntry?.destination?.route?: ImageGenAppScreen.Start.name
    )

    Scaffold(
        topBar = {
            ImageGenAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                navController
            )
        }
    ) { innerPadding ->

        NavHost(navController = navController,
            startDestination = ImageGenAppScreen.Start.name,
            modifier = Modifier.padding(innerPadding)) {
            composable(route = ImageGenAppScreen.Start.name) {
                InitialScreen(
                    onClickImageGeneration = {
                        navController.navigate(route = ImageGenAppScreen.ImageGenerator.name)
                    },
                    onClickImageEnhancer = {
                        viewModel.setImagePickerOrigin(Constants.UPSCALE_ORIGIN)
                        Log.d(TAG, "Setting origin to upscale: ${Constants.UPSCALE_ORIGIN}")
                        navController.navigate(route = ImageGenAppScreen.ImagePicker.name)
                    },
                    onClickVideoGenerator = {
                        viewModel.setImagePickerOrigin(Constants.IMAGE_TO_VIDEO_ORIGIN)
                        Log.d(TAG, "Setting origin to image-to-video: ${Constants.IMAGE_TO_VIDEO_ORIGIN}")
                        navController.navigate(route = ImageGenAppScreen.ImagePicker.name)
                    }
                )
            }

            composable(route = ImageGenAppScreen.ImageGenerator.name) {
                ImageDescSubmitScreen(viewModel, mContext,
                    cancelExecution = {
                        cancelFlowAndNavigateToStart(viewModel, navController)
                    },
                    successExecution = {
                        navController.navigate(route = ImageGenAppScreen.GeneratedImage.name)
                    }
                )
            }
            composable(route = ImageGenAppScreen.GeneratedImage.name) {
                //val bitmap by viewModel.imageBitmap.collectAsState()
                GeneratedImageScreen(viewModel, mContext)
            }
            composable(route = ImageGenAppScreen.ImagePicker.name) {
                //ImagePickerScreen(getContent, selectedImageUri)
                ImagePickerScreen(viewModel,
                    onImageSelected = {
                        if (viewModel.imagePickerOrigin.value == Constants.UPSCALE_ORIGIN) {
                            Log.d(TAG, "Path for upscale: ${viewModel.imagePickerOrigin.value}")
                            navController.navigate(
                                route = ImageGenAppScreen.EnhanceImage.name)
                        } else {
                            Log.d(TAG, "Path for image-to-video: ${viewModel.imagePickerOrigin.value}")
                            navController.navigate(
                                route = ImageGenAppScreen.VideoGenerator.name)
                        }
                    }
                )
            }
            composable(route = ImageGenAppScreen.EnhanceImage.name) {
                EnhanceImageScreen(viewModel, mContext,
                //EsrganEnhanceImageScreen(viewModel, mContext,
                    cancelExecution = {
                        cancelFlowAndNavigateToStart(viewModel, navController)
                    },
                    successExecution = {
                        navController.navigate(route = ImageGenAppScreen.EnhancedImage.name)
                    })

                    /*
                                       val upscaleImage = UpscaleImage()
                    val apiKey = viewModel.getSho()
                    Log.d(TAG, "Got secret key")
                    viewModel.imageUri.value?.let {
                        if (it.isNotEmpty() && it[0] != null) {
                            val uri = it[0]
                            upscaleImage.upscaleImage(apiKey, uri!!, mContext, viewModel)
                            navController.navigate(route = ImageGenAppScreen.EnhancedImage.name)
                        }
                    }
                     */

            }
            composable(route = ImageGenAppScreen.EnhancedImage.name) {
                EnhancedImageScreen(viewModel, mContext)
            }
            composable(route = ImageGenAppScreen.VideoGenerator.name) {
                GenerateVideoScreen(viewModel, mContext,
                    cancelExecution = {
                        cancelFlowAndNavigateToStart(viewModel, navController)
                    },
                    successExecution = {
                        navController.navigate(route = ImageGenAppScreen.GeneratedVideo.name)
                    })
            }
            composable(route = ImageGenAppScreen.GeneratedVideo.name) {
                GeneratedVideoScreen(viewModel, context = mContext)
            }
            composable(route = ImageGenAppScreen.MenuScreen.name) {
                CloseableScreen(onClose = { navController.popBackStack() })
            }
        }
    }
}

private fun cancelFlowAndNavigateToStart(viewModel: ImageGenViewModel,
                                          navController: NavHostController) {
    navController.popBackStack(ImageGenAppScreen.Start.name, inclusive = false)
    viewModel.resetValues()
}
