package com.example.movieratings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

/**
 * The single Activity for this app.
 *
 * In Flutter, main() calls runApp(MyApp()).
 * In Compose, MainActivity calls setContent { MovieApp() }.
 *
 * enableEdgeToEdge() makes the app draw behind the system bars,
 * giving a more immersive dark-themed look (matching the Flutter version).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieApp()
        }
    }
}
