package com.yunshangguizhou.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yunshangguizhou.app.ui.debug.DebugScreen
import com.yunshangguizhou.app.ui.detail.ClothingDetailScreen
import com.yunshangguizhou.app.ui.home.HomeScreen
import com.yunshangguizhou.app.ui.recommendation.RecommendationHistoryScreen
import com.yunshangguizhou.app.ui.settings.SettingsScreen
import com.yunshangguizhou.app.ui.wardrobe.AddClothingScreen
import com.yunshangguizhou.app.ui.wardrobe.WardrobeScreen

object Routes {
    const val HOME = "home"; const val WARDROBE = "wardrobe"; const val ADD = "add"
    const val DETAIL = "clothing_detail/{clothingId}"; const val SETTINGS = "settings"
    const val HISTORY = "history"; const val DEBUG = "debug"
    fun detail(id: Long) = "clothing_detail/$id"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onWardrobe = { navController.navigate(Routes.WARDROBE) },
                onAdd = { navController.navigate(Routes.ADD) },
                onHistory = { navController.navigate(Routes.HISTORY) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onDebug = { navController.navigate(Routes.DEBUG) },
                onDetail = { id -> navController.navigate(Routes.detail(id)) }
            )
        }
        composable(Routes.WARDROBE) {
            WardrobeScreen(onAdd = { navController.navigate(Routes.ADD) },
                onDetail = { id -> navController.navigate(Routes.detail(id)) },
                onBack = { navController.popBackStack() })
        }
        composable(Routes.ADD) {
            AddClothingScreen(onBack = { navController.popBackStack() }, onSuccess = { navController.popBackStack() })
        }
        composable(Routes.DETAIL, arguments = listOf(navArgument("clothingId") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("clothingId") ?: return@composable
            ClothingDetailScreen(clothingId = id, onBack = { navController.popBackStack() }, onDeleted = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) { SettingsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.HISTORY) { RecommendationHistoryScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.DEBUG) { DebugScreen(onBack = { navController.popBackStack() }) }
    }
}
