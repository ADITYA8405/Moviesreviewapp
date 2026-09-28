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
import com.example.movieratings.ui.screens.LibraryScreen
import com.example.movieratings.ui.screens.LoginScreen
import com.example.movieratings.ui.screens.SearchScreen
import com.example.movieratings.ui.screens.SignupScreen
import com.example.movieratings.viewmodel.AuthViewModel
import com.example.movieratings.viewmodel.HomeViewModel
import com.example.movieratings.viewmodel.SearchViewModel
import com.example.movieratings.viewmodel.UserLibraryViewModel

/**
 * Navigation routes for authentication, home, search, library and details.
 */
object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val DETAILS = "details/{movieId}"

    fun detailsRoute(movieId: Int) = "details/$movieId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    libraryViewModel: UserLibraryViewModel = viewModel()
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

    // Initialize user library flows as soon as authenticated
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            libraryViewModel.initLibrary(currentUserId)
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
                onLibraryClick = {
                    if (currentUserId != null) {
                        libraryViewModel.initLibrary(currentUserId)
                    }
                    navController.navigate(Routes.LIBRARY)
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

        // Library / Profile screen
        composable(Routes.LIBRARY) {
            // Ensure library data is loaded for the current user
            LaunchedEffect(currentUserId) {
                if (currentUserId != null) {
                    libraryViewModel.initLibrary(currentUserId)
                }
            }

            LibraryScreen(
                viewModel     = libraryViewModel,
                userEmail     = currentUserEmail,
                onBackClick   = { navController.popBackStack() },
                onMovieClick  = { savedMovie ->
                    homeViewModel.saveItem(savedMovie.toMediaItem())
                    navController.navigate(Routes.detailsRoute(savedMovie.movieId))
                },
                onReviewClick = { movieId ->
                    navController.navigate(Routes.detailsRoute(movieId))
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
                currentUserEmail = currentUserEmail,
                libraryViewModel = libraryViewModel
            )
        }
    }
}
