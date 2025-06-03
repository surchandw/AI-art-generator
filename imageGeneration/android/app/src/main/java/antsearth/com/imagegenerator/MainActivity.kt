package antsearth.com.imagegenerator

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import antsearth.com.imagegenerator.data.Config
import antsearth.com.imagegenerator.ui.theme.ImageGeneratorTheme
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions

private const val TAG = "MainActivity"
class MainActivity : ComponentActivity() {
    private val model: ImageGenViewModel by viewModels()
    private val helper: HelperClass = HelperClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = Config()
        if (config.appSubscribed != Constants.APP_NOT_SUBSCRIBED) {
            MobileAds.initialize(this)
        }
        enableEdgeToEdge()
        setContent {
            ImageGeneratorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->

                    ImageGenApp(this)
                    //CloseableScreen ({})
                    //ImagePicker()
                    //EnhancedImageScreen(model = model, context = this)
                    //GeneratedVideoScreen(model, this)
                    if (helper.isInternetConnected(this)) {
                        if (!model.getShoDataStatus()) {
                            helper.getSTAronbaSho(model)
                        }
                    } else {
                        Log.d(TAG, "No internet connection")
                        ShowDialog(
                            stringResource(id = R.string.no_internet_connection),
                            stringResource(id = R.string.no_internet_connection_msg), {}
                        )
                    }
                }
            }
        }
    }
}
