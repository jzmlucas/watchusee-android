package br.com.watchusee.android.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import br.com.watchusee.android.ui.auth.LoginScreen
import br.com.watchusee.android.ui.auth.RegisterScreen
import br.com.watchusee.android.ui.detail.MovieDetailScreen
import br.com.watchusee.android.ui.detail.MovieCastScreen
import br.com.watchusee.android.ui.shares.SharesScreen
import br.com.watchusee.android.ui.watchlist.LibraryScreen
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.ProfileViewModel
import br.com.watchusee.android.viewmodel.ShareViewModel
import br.com.watchusee.android.viewmodel.WatchlistViewModel
import br.com.watchusee.android.ui.search.SearchScreen
import br.com.watchusee.android.ui.search.CinemaExploreScreen
import br.com.watchusee.android.ui.home.CinemaFeedScreen
import br.com.watchusee.android.ui.profile.CinemaProfileScreen
import br.com.watchusee.android.ui.theme.PremiumGold
import br.com.watchusee.android.ui.theme.TextGrey
import br.com.watchusee.android.ui.watchlist.CinemaLibraryScreen

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Login : Screen(
        "login",
        "Entrar",
        Icons.Rounded.Lock,
        Icons.Rounded.Lock
    )

    data object Register : Screen(
        "register",
        "Cadastrar",
        Icons.Rounded.PersonAdd,
        Icons.Rounded.PersonAdd
    )

    data object Home : Screen(
        "home",
        "Home",
        Icons.Outlined.Home,
        Icons.Rounded.Home
    )

    data object Library : Screen(
        "library",
        "Biblioteca",
        Icons.Outlined.VideoLibrary,
        Icons.Rounded.VideoLibrary
    )

    data object Shares : Screen(
        "shares",
        "Download",
        Icons.Rounded.Share,
        Icons.Rounded.Share
    )

    data object Profile : Screen(
        "profile",
        "My Profile",
        Icons.Outlined.Person,
        Icons.Rounded.Person
    )

    data object About : Screen(
        "about",
        "Sobre",
        Icons.Rounded.Info,
        Icons.Rounded.Info
    )

    data object ToWatch : Screen(
        "to_watch",
        "Lista",
        Icons.Rounded.BookmarkBorder,
        Icons.Rounded.Bookmark
    )

    data object Watched : Screen(
        "watched",
        "Vistos",
        Icons.Rounded.Visibility,
        Icons.Rounded.Visibility
    )

    data object Friends : Screen(
        "friends",
        "Amigos",
        Icons.Rounded.People,
        Icons.Rounded.People
    )

    data object Settings : Screen(
        "settings",
        "Configurações",
        Icons.Rounded.Settings,
        Icons.Rounded.Settings
    )

    data object OtherProfile : Screen(
        "other_profile/{userId}",
        "Perfil",
        Icons.Rounded.Person,
        Icons.Rounded.Person
    )

    data object EditProfile : Screen(
        "edit_profile",
        "Editar Perfil",
        Icons.Rounded.Edit,
        Icons.Rounded.Edit
    )

    data object Search : Screen(
        "search",
        "Buscar",
        Icons.Rounded.Search,
        Icons.Rounded.Search
    )
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
            popUpTo(0) {
                inclusive = true
            }
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

                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onContinueAsGuest = onContinueAsGuest,
                viewModel = authViewModel
            )
        }

        composable(Screen.Search.route) {
            CinemaExploreScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    profileViewModel.loadProfile()

                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
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
            CinemaFeedScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route) {
                        launchSingleTop = true
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
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.ToWatch.route) {
            br.com.watchusee.android.ui.watchlist.ToWatchScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = watchlistViewModel,
                authViewModel = authViewModel,
                onRequireLogin = {
                    onRequireLogin {}
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Watched.route) {
            br.com.watchusee.android.ui.watchlist.WatchedScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                viewModel = watchlistViewModel,
                authViewModel = authViewModel,
                onRequireLogin = {
                    onRequireLogin {}
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = "${Screen.Library.route}?tab={tab}",
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getInt("tab") ?: 0

            CinemaLibraryScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                viewModel = watchlistViewModel,
                authViewModel = authViewModel,
                onRequireLogin = {
                    onRequireLogin {}
                },
                initialTab = tab
            )
        }

        composable(Screen.Profile.route) {
            CinemaProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onRequireLogin = {
                    onRequireLogin {}
                },
                viewModel = profileViewModel,
                authViewModel = authViewModel,
                onFriendsClick = {
                    navController.navigate(Screen.Friends.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) }
            )
        }

        composable(Screen.About.route) {
            br.com.watchusee.android.ui.profile.AboutScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditProfile.route) {
            br.com.watchusee.android.ui.profile.EditProfileScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Friends.route) {
            br.com.watchusee.android.ui.social.FriendsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                }
            )
        }

        composable(Screen.Settings.route) {
            br.com.watchusee.android.ui.profile.SettingsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.OtherProfile.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val userId =
                backStackEntry.arguments?.getLong("userId") ?: 0L

            br.com.watchusee.android.ui.social.FigmaOtherUserProfileScreen(
                userId = userId,
                onBack = {
                    navController.popBackStack()
                },
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                }
            )
        }

        composable(
            route = "detail/{movieId}",
            arguments = listOf(
                navArgument("movieId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val movieId =
                backStackEntry.arguments?.getLong("movieId") ?: 0L

            MovieDetailScreen(
                movieId = movieId,
                onBack = {
                    navController.popBackStack()
                },
                onCastClick = {
                    navController.navigate("cast/$movieId")
                },
                onRequireLogin = onRequireLogin,
                authViewModel = authViewModel
            )
        }

        composable(
            route = "cast/{movieId}",
            arguments = listOf(
                navArgument("movieId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getLong("movieId") ?: 0L

            MovieCastScreen(
                movieId = movieId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun WatchuSeeBottomBar(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    shareViewModel: ShareViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentRoute = currentDestination?.route

    if (
        currentRoute == Screen.Login.route ||
        currentRoute == Screen.Register.route
    ) {
        return
    }

    if (currentRoute == Screen.OtherProfile.route) {
        val navigateToRoute: (String) -> Unit = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
        br.com.watchusee.android.ui.social.FigmaProfileBottomBar(
            currentRoute = currentRoute,
            onHomeClick = { navigateToRoute(Screen.Home.route) },
            onSearchClick = { navigateToRoute(Screen.Search.route) },
            onLibraryClick = {
                if (authViewModel.isAuthenticated()) {
                    navigateToRoute(Screen.Library.route)
                } else {
                    navController.navigate(Screen.Login.route)
                }
            },
            onProfileClick = { navigateToRoute(Screen.Profile.route) }
        )
        return
    }

    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Library,
        Screen.Profile
    )

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(
            animationSpec = tween(300)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            color = Color(0xFF0B0D0F),
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = Color(0xFF2A3038), thickness = 1.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { screen ->
                            val selected = currentDestination
                                ?.hierarchy
                                ?.any { destination ->
                                    destination.route == screen.route ||
                                            destination.route?.startsWith(
                                                "${screen.route}?"
                                            ) == true
                                } == true

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        },
                                        indication = null
                                    ) {
                                        if (screen == Screen.Library && !authViewModel.isAuthenticated()) {
                                            navController.navigate(Screen.Login.route)
                                        } else if (screen == Screen.Home && currentRoute == Screen.Search.route) {
                                            navController.popBackStack(Screen.Home.route, inclusive = false)
                                        } else {
                                            navController.navigate(screen.route) {
                                                popUpTo(
                                                    navController.graph.findStartDestination().id
                                                ) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(52.dp)
                                        .width(64.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = if (selected) screen.selectedIcon else screen.icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (selected) PremiumGold else TextGrey
                                        )
                                        Text(
                                            text = screen.title,
                                            color = if (selected) PremiumGold else TextGrey,
                                            fontSize = 10.sp,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
