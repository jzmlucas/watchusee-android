package br.com.watchusee.android.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import br.com.watchusee.android.ui.auth.LoginScreen
import br.com.watchusee.android.ui.auth.RegisterScreen
import br.com.watchusee.android.ui.detail.MovieDetailScreen
import br.com.watchusee.android.ui.home.HomeScreen
import br.com.watchusee.android.ui.search.SearchScreen
import br.com.watchusee.android.ui.shares.SharesScreen
import br.com.watchusee.android.ui.watchlist.ToWatchScreen
import br.com.watchusee.android.ui.watchlist.WatchedScreen
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.WatchlistViewModel

sealed class Screen(
    val route: String, 
    val title: String, 
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Login : Screen("login", "Entrar", Icons.Default.Lock, Icons.Default.Lock)
    data object Register : Screen("register", "Cadastrar", Icons.Default.PersonAdd, Icons.Default.PersonAdd)
    data object Home : Screen("home", "Início", Icons.Outlined.Home, Icons.Default.Home)
    data object Search : Screen("search", "Busca", Icons.Outlined.Search, Icons.Default.Search)
    data object Shares : Screen("shares", "Shares", Icons.Outlined.Share, Icons.Default.Share)
    data object Library : Screen("library", "Biblioteca", Icons.Outlined.VideoLibrary, Icons.Default.VideoLibrary)
    data object Profile : Screen("profile", "Perfil", Icons.Outlined.AccountCircle, Icons.Default.AccountCircle)
    data object About : Screen("about", "Sobre", Icons.Outlined.Info, Icons.Default.Info)
    data object ToWatch : Screen("to_watch", "Lista", Icons.Outlined.BookmarkBorder, Icons.Default.Bookmark)
    data object Watched : Screen("watched", "Vistos", Icons.Outlined.Visibility, Icons.Default.Visibility)
}

@Composable
fun WatchuSeeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val watchlistViewModel: WatchlistViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: br.com.watchusee.android.viewmodel.ProfileViewModel = hiltViewModel()

    val onRequireLogin: (() -> Unit) -> Unit = { action ->
        authViewModel.setPendingAction(action)
        navController.navigate(Screen.Login.route)
    }

    val onContinueAsGuest = {
        navController.navigate(Screen.Home.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    profileViewModel.loadProfile()
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onContinueAsGuest = onContinueAsGuest,
                viewModel = authViewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    profileViewModel.loadProfile()
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onBack = {
                    navController.popBackStack()
                },
                onContinueAsGuest = onContinueAsGuest,
                viewModel = authViewModel
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onRequireLogin = onRequireLogin,
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onRequireLogin = onRequireLogin,
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Shares.route) {
            SharesScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onRequireLogin = onRequireLogin,
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ToWatch.route) {
            ToWatchScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = watchlistViewModel,
                authViewModel = authViewModel,
                onRequireLogin = { onRequireLogin {} },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Watched.route) {
            WatchedScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = watchlistViewModel,
                authViewModel = authViewModel,
                onRequireLogin = { onRequireLogin {} },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "${Screen.Library.route}?tab={tab}",
            arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getInt("tab") ?: 0
            br.com.watchusee.android.ui.watchlist.LibraryScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = watchlistViewModel,
                authViewModel = authViewModel,
                onRequireLogin = { onRequireLogin {} },
                initialTab = tab
            )
        }
        composable(Screen.Profile.route) {
            br.com.watchusee.android.ui.profile.ProfileScreen(
                onWatchedClick = {
                    navController.navigate("${Screen.Library.route}?tab=1") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onToWatchClick = {
                    navController.navigate("${Screen.Library.route}?tab=0") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onRequireLogin = { onRequireLogin {} },
                viewModel = profileViewModel,
                authViewModel = authViewModel,
                onAboutClick = {
                    navController.navigate(Screen.About.route)
                }
            )
        }
        composable(Screen.About.route) {
            br.com.watchusee.android.ui.profile.AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "detail/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.LongType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getLong("movieId") ?: 0L
            MovieDetailScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() },
                onRequireLogin = onRequireLogin,
                authViewModel = authViewModel
            )
        }
    }
}

@Composable
fun WatchuSeeBottomBar(
    navController: NavHostController,
    shareViewModel: br.com.watchusee.android.viewmodel.ShareViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val pendingCount by shareViewModel.pendingCount.collectAsStateWithLifecycle()

    if (currentRoute == Screen.Login.route || currentRoute == Screen.Register.route) return

    val items = listOf(Screen.Home, Screen.Search, Screen.Library, Screen.Shares, Screen.Profile)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp, 
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.height(80.dp)
            ) {
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { dest ->
                        dest.route?.startsWith(screen.route) == true
                    } == true
                    NavigationBarItem(
                        icon = {
                             BadgedBox(
                                badge = {
                                    if (screen == Screen.Shares && pendingCount > 0) {
                                        Badge {
                                            Text(pendingCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else Color.Transparent
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (selected) screen.selectedIcon else screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        },
                        selected = selected,
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}
