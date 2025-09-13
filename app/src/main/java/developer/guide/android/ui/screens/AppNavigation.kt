package developer.guide.android.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list_screen") {
        composable("list_screen") {
            ListScreen(onNavigateToSection = { sectionId ->
                navController.navigate("details_screen/$sectionId")
            })
        }
        composable(
            route = "details_screen/{sectionId}",
            arguments = listOf(navArgument("sectionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getInt("sectionId")
            if (sectionId != null) {
                DetailsScreen(sectionId = sectionId, navController = navController)
            } else {
                Text("Something went wrong!")
            }
        }
    }
}