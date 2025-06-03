package antsearth.com.imagegenerator

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import antsearth.com.imagegenerator.data.Config

private const val TAG = "UtilScreen"
@Composable
fun RoundedCornerDialog(title: String, message: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss) {
                    Text(text = "Dismiss")
                }
            }
        }
    }
}

@Composable
fun ErrorDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("OK")
            }
        },
        title = { Text(title) },
        text = { Text(message) }
    )
}

@Composable
fun ShowDialog(title: String, message: String, onDismiss: () -> Unit) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDismiss()
                               },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDismiss()
                }) {
                    Text("OK")
                }
            },
            title = { Text(title) },
            text = { Text(message) }
        )

    }
}

@Composable
fun ShowErrorDialog(code: Int, onDismiss: () -> Unit) {
    when (code) {
        //Stability API HTTP errors -
        Constants.API_INTERNAL_ERROR -> {
            ErrorDialog(title = stringResource(id = R.string.internal_error),
                message = stringResource(id = R.string.internal_error_msg),
                onDismiss)
        }
        Constants.API_SERVER_INTERNAL_ERROR -> {
            ErrorDialog(title = stringResource(id = R.string.internal_error),
                message = stringResource(id = R.string.internal_error_msg),
                onDismiss)
        }
        Constants.API_REQUEST_FLAGGED_BY_CONTENT_MODERATOR -> {
            ErrorDialog(title = stringResource(id = R.string.content_violation),
                message = stringResource(id = R.string.content_violation_msg),
                onDismiss)
        }
        Constants.API_FILE_SIZE_EXCEEDS_MAX_ALLOWED -> {
            ErrorDialog(title = stringResource(id = R.string.file_size_error),
                message = stringResource(id = R.string.file_size_error_msg),
                onDismiss)
        }
        Constants.API_UNSUPPORTED_LANGUAGE -> {
            ErrorDialog(title = stringResource(id = R.string.unsupported_language_error),
                message = stringResource(id = R.string.unsupported_language_error_msg),
                onDismiss)
        }
        Constants.API_MAX_ALLOWED_REQUEST_EXCEEDED -> {
            ErrorDialog(title = stringResource(id = R.string.max_call_error),
                message = stringResource(id = R.string.max_call_error_msg),
                onDismiss)
        }
        //Other errors -
        Constants.NO_TEXT_TO_IMAGE_DESCRIPTION -> {
            ErrorDialog(title = stringResource(id = R.string.no_image_description),
                message = stringResource(id = R.string.no_image_description_msg),
                onDismiss)
        }
        Constants.NO_INTERNET_CONNECTION -> {
            ErrorDialog(title = stringResource(id = R.string.no_internet_connection),
                message = stringResource(id = R.string.no_internet_connection_msg),
                onDismiss)
        }
    }
}

@Composable
fun ShowCircularProgressBar(message: String) {
    Log.d(TAG, "Inside ShowCircularProgressBar function")
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message,
            modifier = Modifier.padding(top = 16.dp),
            fontSize = 20.sp)
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(56.dp)
        )
    }
}

@Composable
fun ButtonText(firstLine: String, secondLine: String, config: Config) {
    val annotatedString = buildAnnotatedString {
        if (config.appSubscribed == Constants.APP_NOT_SUBSCRIBED) {
            withStyle(style = SpanStyle(fontSize = 16.sp)) {
                append("$firstLine\n")
            }
            withStyle(style = SpanStyle(fontSize = 10.sp)) {
                append(secondLine)
            }
        } else if (config.appSubscribed == Constants.APP_SUBSCRIBED) {
            withStyle(style = SpanStyle(fontSize = 18.sp)) {
                append(firstLine)
            }
        }
    }
    Text(text = annotatedString,
        textAlign = TextAlign.Center)
}