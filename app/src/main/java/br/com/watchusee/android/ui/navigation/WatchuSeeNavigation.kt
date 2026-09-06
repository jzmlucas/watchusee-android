package br.com.watchusee.android.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import br.com.watchusee.android.viewmodel.ProfileViewModel
import br.com.watchusee.android.viewmodel.WatchlistViewModel

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Login : Screen("login", "Entrar", Icons.Rounded.Lock, Icons.Rounded.Lock)
    data object Register : Screen("register", "Cadastrar", Icons.Rounded.PersonAdd, Icons.Rounded.PersonAdd)
    data object Home : Screen("home", "Início", Icons.Rounded.Home, Icons.Rounded.Home)
    data object Search : Screen("search", "Busca", Icons.Rounded.Search, Icons.Rounded.Search)
    data object Shares : Screen("shares", "Shares", Icons.Rounded.Share, Icons.Rounded.Share)
    data object Library : Screen("library", "Biblioteca", Icons.Rounded.VideoLibrary, Icons.Rounded.VideoLibrary)
    data object Profile : Screen("profile", "Perfil", Icons.Rounded.AccountCircle, Icons.Rounded.AccountCircle)
    data object About : Screen("about", "Sobre", Icons.Rounded.Info, Icons.Rounded.Info)
    data object ToWatch : Screen("to_watch", "Lista", Icons.Rounded.BookmarkBorder, Icons.Rounded.Bookmark)
    data object Watched : Screen("watched", "Vistos", Icons.Rounded.Visibility, Icons.Rounded.Visibility)
    data object Friends : Screen("friends", "Amigos", Icons.Rounded.People, Icons.Rounded.People)
    data object Settings : Screen("settings", "Configurações", Icons.Rounded.Settings, Icons.Rounded.Settings)
    data object OtherProfile : Screen("other_profile/{userId}", "Perfil", Icons.Rounded.Person, Icons.Rounded.Person)
    data object EditProfile : Screen("edit_profile", "Editar Perfil", Icons.Rounded.Edit, Icons.Rounded.Edit)
}

@Composable
fun WatchuSeeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val watchlistViewModel: WatchlistViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()

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
                },
                onFriendsClick = {
                    navController.navigate(Screen.Friends.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onEditProfileClick = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onOtherProfileClick = { userId ->
                    navController.navigate("other_profile/$userId")
                }
            )
        }
        composable(Screen.About.route) {
            br.com.watchusee.android.ui.profile.AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.EditProfile.route) {
            br.com.watchusee.android.ui.profile.EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Friends.route) {
            br.com.watchusee.android.ui.social.FriendsScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                }
            )
        }
        composable(Screen.Settings.route) {
            br.com.watchusee.android.ui.profile.SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.OtherProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            br.com.watchusee.android.ui.social.OtherUserProfileScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                }
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
    val currentDestination = navBackStackEntry?.destination

    val pendingCount by shareViewModel.pendingCount.collectAsStateWithLifecycle()

    val currentRoute = currentDestination?.route

    if (
        currentRoute == Screen.Login.route ||
        currentRoute == Screen.Register.route
    ) {
        return
    }

    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Library,
        Screen.Shares,
        Screen.Profile
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            )
        ) {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                items.forEach { screen ->

                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { destination ->
                            destination.route == screen.route ||
                                    destination.route?.startsWith("${screen.route}?") == true
                        } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(
                                    navController.graph.findStartDestination().id
                                ) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (
                                        screen == Screen.Shares &&
                                        pendingCount > 0
                                    ) {
                                        Badge {
                                            Text(
                                                text = if (pendingCount > 99) {
                                                    "99+"
                                                } else {
                                                    pendingCount.toString()
                                                }
                                            )
                                        }
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selected) {
                                                MaterialTheme.colorScheme.primary.copy(
                                                    alpha = 0.14f
                                                )
                                            } else {
                                                Color.Transparent
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (selected) {
                                            screen.selectedIcon
                                        } else {
                                            screen.icon
                                        },
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.70f
                            ),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}