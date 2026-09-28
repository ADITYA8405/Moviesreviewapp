package com.example.movieratings.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.movieratings.ui.screens.DetailsScreen
import com.example.movieratings.ui.screens.HomeScreen
import com.example.movieratings.viewmodel.HomeViewModel

/**
 * Navigation routes — equivalent to Flutter's Navigator.push() / MaterialPageRoute.
 *
 * Flutter navigated by pushing a new Description widget with all data as constructor args.
 * Compose Navigation uses routes (like URLs) and passes just the movie ID.
 * The DetailsScreen then looks up the full item from the ViewModel's cache.
 */
object Routes {
    const val HOME = "home"
    const val DETAILS = "details/{movieId}"

    fun detailsRoute(movieId: Int) = "details/$movieId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // Home screen
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onItemClick = { item ->
                    navController.navigate(Routes.detailsRoute(item.id))
                }
            )
        }

        // Details screen — receives movieId from the route
        composable(
            route = Routes.DETAILS,
            arguments = listOf(
                navArgument("movieId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: -1
            val item = viewModel.getItemById(movieId)

            DetailsScreen(
                item = item,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
