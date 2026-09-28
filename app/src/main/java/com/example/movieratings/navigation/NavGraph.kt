package com.example.movieratings.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.movieratings.ui.screens.DetailsScreen
import com.example.movieratings.ui.screens.HomeScreen
import com.example.movieratings.ui.screens.LoginScreen
import com.example.movieratings.ui.screens.SearchScreen
import com.example.movieratings.ui.screens.SignupScreen
import com.example.movieratings.viewmodel.AuthViewModel
import com.example.movieratings.viewmodel.HomeViewModel
import com.example.movieratings.viewmodel.SearchViewModel

/**
 * Navigation routes for authentication, home, search, and details.
 */
object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val SEARCH = "search"
    const val DETAILS = "details/{movieId}"

    fun detailsRoute(movieId: Int) = "details/$movieId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId    = currentUser?.uid
    val currentUserEmail = currentUser?.email
    val startDestination = if (currentUser != null) Routes.HOME else Routes.LOGIN

    // Automatically navigate when authentication state changes (login / logout)
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != Routes.LOGIN && currentRoute != Routes.SIGNUP) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == Routes.LOGIN || currentRoute == Routes.SIGNUP || currentRoute == null) {
                navController.navigate(Routes.HOME) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login screen
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignup = {
                    authViewModel.clearError()
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }

        // Signup screen
        composable(Routes.SIGNUP) {
            SignupScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    authViewModel.clearError()
                    navController.popBackStack()
                }
            )
        }

        // Home screen
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onSearchClick = {
                    navController.navigate(Routes.SEARCH)
                },
                onLogoutClick = {
                    authViewModel.logout()
                },
                onItemClick = { item ->
                    navController.navigate(Routes.detailsRoute(item.id))
                }
            )
        }

        // Search screen
        composable(Routes.SEARCH) {
            val searchViewModel: SearchViewModel = viewModel()
            SearchScreen(
                viewModel = searchViewModel,
                onBackClick = { navController.popBackStack() },
                onItemClick = { item ->
                    homeViewModel.saveItem(item)
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
            val item = homeViewModel.getItemById(movieId)

            DetailsScreen(
                item             = item,
                onBackClick      = { navController.popBackStack() },
                currentUserId    = currentUserId,
                currentUserEmail = currentUserEmail
            )
        }
    }
}
