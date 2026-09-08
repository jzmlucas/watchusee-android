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
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
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
import br.com.watchusee.android.ui.shares.SharesScreen
import br.com.watchusee.android.ui.watchlist.LibraryScreen
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.ProfileViewModel
import br.com.watchusee.android.viewmodel.ShareViewModel
import br.com.watchusee.android.viewmodel.WatchlistViewModel
import br.com.watchusee.android.ui.search.SearchScreen

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
        Icons.Rounded.Home,
        Icons.Rounded.Home
    )

    data object Library : Screen(
        "library",
        "Play List",
        Icons.Rounded.VideoLibrary,
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
        Icons.Rounded.Person,
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
        "Busca",
        Icons.Rounded.Explore,
        Icons.Rounded.Explore
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
            SearchScreen(
                onMovieClick = { movieId ->
                    navController.navigate("detail/$movieId")
                },
                onBack = {
                    navController.popBackStack()
                },
                onRequireLogin = onRequireLogin
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
            br.com.watchusee.android.ui.home.WatchuSeeHomeScreen(
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

            LibraryScreen(
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
                viewModel = watchlistViewModel,
                authViewModel = authViewModel,
                onRequireLogin = {
                    onRequireLogin {}
                },
                initialTab = tab
            )
        }

        composable(Screen.Profile.route) {
            br.com.watchusee.android.ui.profile.ProfileScreen(
                onWatchedClick = {
                    navController.navigate(
                        "${Screen.Library.route}?tab=1"
                    ) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onToWatchClick = {
                    navController.navigate(
                        "${Screen.Library.route}?tab=0"
                    ) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
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

            br.com.watchusee.android.ui.social.OtherUserProfileScreen(
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
                onRequireLogin = onRequireLogin,
                authViewModel = authViewModel
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
        Screen.Library,
        Screen.Shares,
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
            color = br.com.watchusee.android.ui.theme.SurfaceGrey,
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp
            ),
            shadowElevation = 16.dp
        ) {
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
                        // Considera a tela de Busca como parte da aba Home para manter o ícone selecionado
                        val isSearchOnHome = screen == Screen.Home && currentRoute == Screen.Search.route

                        val selected = isSearchOnHome || currentDestination
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
                                        // Se estiver na busca e clicar em Home, volta para a Home limpando a busca
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
                                    .height(44.dp)
                                    .width(64.dp)
                                    .clip(
                                        RoundedCornerShape(20.dp)
                                    )
                                    .background(
                                        if (selected) {
                                            br.com.watchusee.android.ui.theme.PremiumGold
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
                                    modifier = Modifier.size(26.dp),
                                    tint = if (selected) {
                                        Color.Black
                                    } else {
                                        br.com.watchusee.android.ui.theme.TextGrey
                                    }
                                )

                                if (
                                    screen == Screen.Shares &&
                                    pendingCount > 0
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(
                                                br.com.watchusee.android.ui.theme.CinemaRed
                                            )
                                            .align(Alignment.TopEnd)
                                            .offset(
                                                x = 4.dp,
                                                y = (-4).dp
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (pendingCount > 9) {
                                                "9+"
                                            } else {
                                                pendingCount.toString()
                                            },
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
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