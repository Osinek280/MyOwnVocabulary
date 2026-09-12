package com.example.myownvocabulary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myownvocabulary.ui.screens.HomeScreen

@Composable
fun VocabularyNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.Words
    ) {
        composable(Routes.Words) {
            HomeScreen()
        }
    }
}
