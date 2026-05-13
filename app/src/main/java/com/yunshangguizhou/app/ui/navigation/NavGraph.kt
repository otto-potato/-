package com.yunshangguizhou.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yunshangguizhou.app.ui.detail.ClothingDetailScreen
import com.yunshangguizhou.app.ui.home.HomeScreen
import com.yunshangguizhou.app.ui.recommendation.RecommendationHistoryScreen
import com.yunshangguizhou.app.ui.settings.SettingsScreen
import com.yunshangguizhou.app.ui.wardrobe.AddClothingScreen
import com.yunshangguizhou.app.ui.wardrobe.WardrobeScreen

object Routes {
    const val HOME = "home"
    const val WARDROBE = "wardrobe"
    const val ADD_CLOTHING = "add_clothing"
    const val CLOTHING_DETAIL = "clothing_detail/{clothingId}"
    const val SETTINGS = "settings"
    const val RECOMMENDATION_HISTORY = "recommendation_history"

    fun clothingDetail(id: Long) = "clothing_detail/$id"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToWardrobe = {
                    navController.navigate(Routes.WARDROBE)
                },
                onNavigateToAddClothing = {
                    navController.navigate(Routes.ADD_CLOTHING)
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.RECOMMENDATION_HISTORY)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToDetail = { id ->
                    navController.navigate(Routes.clothingDetail(id))
                }
            )
        }

        composable(Routes.WARDROBE) {
            WardrobeScreen(
                onNavigateToAdd = {
                    navController.navigate(Routes.ADD_CLOTHING)
                },
                onNavigateToDetail = { id ->
                    navController.navigate(Routes.clothingDetail(id))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ADD_CLOTHING) {
            AddClothingScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.CLOTHING_DETAIL,
            arguments = listOf(
                navArgument("clothingId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val clothingId = backStackEntry.arguments?.getLong("clothingId") ?: return@composable
            ClothingDetailScreen(
                clothingId = clothingId,
                onBack = {
                    navController.popBackStack()
                },
                onDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.RECOMMENDATION_HISTORY) {
            RecommendationHistoryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
