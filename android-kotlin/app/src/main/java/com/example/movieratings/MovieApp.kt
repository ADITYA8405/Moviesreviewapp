package com.example.movieratings

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.movieratings.navigation.NavGraph
import com.example.movieratings.ui.theme.MovieRatingsTheme

/**
 * Root Composable — equivalent to Flutter's MyApp StatelessWidget.
 *
 * Flutter:
 *   MaterialApp(
 *     debugShowCheckedModeBanner: false,
 *     theme: ThemeData(brightness: Brightness.dark),
 *     home: Home()
 *   )
 *
 * Compose:
 *   MovieRatingsTheme { NavGraph(navController) }
 */
@Composable
fun MovieApp() {
    MovieRatingsTheme {
        val navController = rememberNavController()
        NavGraph(navController = navController)
    }
}
