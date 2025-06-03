package antsearth.com.imagegenerator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController

@Composable
fun MenuScreen(navController: NavController) {
    IconButton(onClick = {
        navController.navigate(ImageGenAppScreen.MenuScreen.name)
    }) {
        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = stringResource(id = R.string.subscribe)
        )
    }
    IconButton(onClick = { /*TODO*/ }) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "More",
        )
    }
}

@Composable
fun MoreMenu() {

}