package developer.guide.android.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Define your screen routes
sealed class Screen(val route: String) {
    object ListScreen : Screen("list_screen")
    object DetailsScreen : Screen("details_screen/{itemId}") {
        fun createRoute(itemId: Int) = "details_screen/$itemId"

        // Define argument key as a constant
        const val ARG_ITEM_ID = "itemId"

    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.ListScreen.route) {
        composable(Screen.ListScreen.route) {
            ListScreen(onItemClick = { itemId ->
                navController.navigate(Screen.DetailsScreen.createRoute(itemId))
            })
        }
        composable(
            route = Screen.DetailsScreen.route,
            arguments = listOf(navArgument(Screen.DetailsScreen.ARG_ITEM_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            // Consider making argument non-nullable if it's required
            val itemId = backStackEntry.arguments?.getInt(Screen.DetailsScreen.ARG_ITEM_ID)
            if (itemId != null) {
                DetailsScreen(itemId = itemId)
            } else {
                Text("🔄 Restart the app.")
            }
        }
    }
}


