package com.mobile.sisarasa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobile.sisarasa.navigation.Routes
import com.mobile.sisarasa.ui.appViewModel
import com.mobile.sisarasa.ui.screens.auth.AuthViewModel
import com.mobile.sisarasa.ui.screens.auth.LoginScreen
import com.mobile.sisarasa.ui.screens.auth.RegisterScreen
import com.mobile.sisarasa.ui.screens.detail.PostDetailScreen
import com.mobile.sisarasa.ui.screens.edit.EditPostScreen
import com.mobile.sisarasa.ui.screens.home.HomeScreen
import com.mobile.sisarasa.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SisaRasaNavGraph()
            }
        }
    }
}

@Composable
fun SisaRasaNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = appViewModel { AuthViewModel(it) }

    val startDestination = remember {
        if (authViewModel.uiState.value.currentUser != null) Routes.HOME else Routes.LOGIN
    }

    // ponytail: logout clears user -> force back to login.
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(uiState.currentUser) {
        if (uiState.currentUser == null && currentRoute != Routes.LOGIN) navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoggedIn = { navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } } },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onLoggedIn = { navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } } },
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenPost = { postId -> navController.navigate(Routes.postDetail(postId)) },
                onCreatePost = { type -> navController.navigate(Routes.editPost(type = type.name)) },
            )
        }
        composable(
            route = Routes.POST_DETAIL,
            arguments = listOf(androidx.navigation.navArgument("postId") { type = androidx.navigation.NavType.StringType }),
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId").orEmpty()
            PostDetailScreen(
                postId = postId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.editPost(postId = postId, type = "")) },
                onDeleted = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.EDIT_POST,
            arguments = listOf(
                androidx.navigation.navArgument("postId") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                },
                androidx.navigation.navArgument("type") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { backStackEntry ->
            EditPostScreen(
                postId = backStackEntry.arguments?.getString("postId"),
                type = backStackEntry.arguments?.getString("type").orEmpty(),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}