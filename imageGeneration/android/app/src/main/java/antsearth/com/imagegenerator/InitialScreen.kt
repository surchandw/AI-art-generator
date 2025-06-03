package antsearth.com.imagegenerator

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Preview
@Composable
fun InitialScreenPreview() {
    //InitialScreen()
}

private const val TAG = "InitialScreen"

@Composable
fun InitialScreen(onClickImageGeneration: () -> Unit,
                  onClickImageEnhancer: () -> Unit,
                  onClickVideoGenerator: () -> Unit) {

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        ImageAsButton(
            imageId = R.drawable.gen_text_to_image,
            onClick = onClickImageGeneration
        )
        Spacer(modifier = Modifier.height(16.dp))

        //ButtonTemplate(stringResource(id = R.string.upscale_image),
        ImageAsButton(
            imageId = R.drawable.enhance_image,
            onClick = onClickImageEnhancer
        )
        Spacer(modifier = Modifier.height(16.dp))

        //ButtonTemplate(stringResource(id = R.string.image_to_video),
        ImageAsButton(
            imageId = R.drawable.gen_image_to_video,
            onClick = onClickVideoGenerator
        )

    }
}

@Composable
fun ButtonTemplate(text: String, onClick: () -> Unit) {
    Button(modifier = Modifier
        .height(86.dp)
        .padding(8.dp)
        .fillMaxWidth(),
        //colors = ButtonDefaults.buttonColors(Color.LightGray, Color.White),
        onClick = { onClick() }) {
        Text(text = text,
            fontSize = 18.sp)
    }
}

@Composable
fun ImageAsButton(imageId: Int, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Image(
        painter = painterResource(id = imageId),
        contentDescription = null,
        alignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .size(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentScale = ContentScale.Fit
    )
}