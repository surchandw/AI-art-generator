package antsearth.com.imagegenerator

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private const val TAG = "SliderTest"
/*
@Composable
fun ImageSlider() {
    // Load images (replace with your own images)
    val topImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.image_enhancer)
    val bottomImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.image_enhancer)

    // State to keep track of the slider position
    var sliderPosition by remember { mutableFloatStateOf(1f) }
    val maxWidth = LocalConfiguration.current.screenWidthDp

    // Box to stack the images
    Box(modifier = Modifier.fillMaxSize()) {
        // Right image, visible when slider is at the left
        Image(
            bitmap = bottomImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(300.dp)
                .fillMaxSize()
                //.offset { IntOffset((sliderPosition * -500).roundToInt(), 0) }
        )

        // Left image, moves to the right and covers the right image as slider moves
        Image(
            bitmap = topImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(300.dp)
                .fillMaxSize()
                .offset { IntOffset((sliderPosition * 1200).roundToInt(), 0) }
        )

        // Slider at the bottom of the screen
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = 0f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(16.dp)
        )
    }
}



@Composable
fun SlideInAnimationScreen() {
    // I'm using the same duration for all animations.
    val animationTime = 300

    // This state is controlling if the second screen is being displayed or not
    var showScreen2 by remember { mutableStateOf(false) }

    // This is just to give that dark effect when the first screen is closed...
    val color = animateColorAsState(
        targetValue = if (showScreen2) Color.DarkGray else Color.Red,
        animationSpec = tween(
            durationMillis = animationTime,
            easing = LinearEasing
        )
    )
    Box(Modifier.fillMaxSize()) {
        // Both Screen1 and Screen2 are declared here...
        // Screen 1
        AnimatedVisibility(!showScreen2, modifier = Modifier.fillMaxSize(),
            enter = slideInHorizontally(initialOffsetX = { -300 }, // small slide 300px
                animationSpec = tween(durationMillis = animationTime,
                    easing = LinearEasing // interpolator
                )
            ),
            exit = slideOutHorizontally(targetOffsetX = { -300 },
                    animationSpec = tween(durationMillis = animationTime,
                        easing = LinearEasing
                    )
            )
        ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(color.value)) {// animating the color
            Button(modifier = Modifier.align(Alignment.Center),
                onClick = {
                    showScreen2 = true
                }) {
                Text(text = "Ok")
            }
        }
    }
        // Screen 2
        AnimatedVisibility(showScreen2, modifier = Modifier.fillMaxSize(),
            enter = slideInHorizontally(initialOffsetX = { it }, // it == fullWidth
                animationSpec = tween(durationMillis = animationTime,
                    easing = LinearEasing
                )
            ),
            exit = slideOutHorizontally(targetOffsetX = { it },
                animationSpec = tween(durationMillis = animationTime,
                    easing = LinearEasing
                )
            )
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Blue)
            ) {
                Button(modifier = Modifier.align(Alignment.Center),
                    onClick = {
                        showScreen2 = false
                    }) {
                    Text(text = "Back")
                }
            }
        }

    }
}

@Composable
fun ImageSideBySide() {
    // Load images (replace with your own images)
    val leftImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.image_enhancer)
    val rightImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.image_to_video)

    // State to keep track of the slider position
    var sliderPosition by remember { mutableFloatStateOf(1f) }
    //val maxWidth = LocalConfiguration.current.screenWidthDp

    // Box to stack the images
    Box(modifier = Modifier.fillMaxSize()) {
        // Right image, visible when slider is at the left
        Image(
            bitmap = leftImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(300.dp)
                .width((sliderPosition * 100).dp)
                .align(Alignment.CenterStart)
            //.offset { IntOffset((sliderPosition * -500).roundToInt(), 0) }
        )

        // Left image, moves to the right and covers the right image as slider moves
        Image(
            bitmap = rightImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(300.dp)
                .fillMaxSize()
                .width(((1 - sliderPosition) * 100).dp)
                .align(Alignment.CenterEnd)
        )

        // Slider at the bottom of the screen
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = 0f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(16.dp)
        )
    }
}
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayPartialImage() {
    // Load images (replace with your own images)
    val leftImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.enhance_image_example)
    val rightImage: ImageBitmap = ImageBitmap.imageResource(id = R.drawable.image_to_video_example)

    // State to keep track of the slider position
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }
    Box(modifier = Modifier
        .height(320.dp)
        .fillMaxSize()
        .padding(start = 16.dp, top = 64.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter) {
        Image(bitmap = leftImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(1.0f)
                .clip(RoundedCornerShape(16.dp))
                .clip(LeftPartialImageClip(sliderPosition)))
        Text(
            text = stringResource(id = R.string.before),
            color = Color.Black, fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 56.dp, bottom = 16.dp)
                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                .background(Color.White)
        )
        Image(bitmap = rightImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(1.0f)
                .clip(RoundedCornerShape(16.dp))
                .clip(RightPartialImageClip(sliderPosition)))
        Text(
            text = stringResource(id = R.string.after),
            color = Color.Black, fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 56.dp, bottom = 16.dp)
                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                .background(Color.White)
        )
        // Slider at the bottom of the screen
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = 0f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            colors = SliderDefaults.colors(
                thumbColor = Color.White, // Customize thumb color
                activeTrackColor = Color.White, // Customize track color
                inactiveTrackColor = Color.White
            )
        )
    }
}



class DoubleTriangleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(size.width / 2, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }

    fun draw(drawScope: DrawScope, alpha: Float) {
        val path = Path().apply {
            moveTo(drawScope.size.width / 2, 0f)
            lineTo(drawScope.size.width, drawScope.size.height)
            lineTo(0f, drawScope.size.height)
            close()
        }
        drawScope.drawPath(path, color = Color.Green, alpha = alpha)
    }
}