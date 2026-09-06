package br.com.watchusee.android.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Facebook
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.viewmodel.AuthUiState
import br.com.watchusee.android.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueAsGuest: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var nick by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(
        label = "login_glow"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "login_glow_alpha"
    )

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    AuthBackground {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(
                    horizontal = 20.dp,
                    vertical = 32.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AuthContent {

                Spacer(
                    modifier = Modifier.height(48.dp)
                )

                AuthBrandHeader(
                    title = "Bem-vindo de volta",
                    subtitle = "Sua agenda de cinema na palma da mão"
                )

                Spacer(
                    modifier = Modifier.height(42.dp)
                )

                // LOGIN
                GlassTextField(
                    value = nick,
                    onValueChange = {
                        nick = it
                    },
                    label = "Login",
                    leadingIcon = Icons.Rounded.Person
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // SENHA
                GlassTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    label = "Senha",
                    leadingIcon = Icons.Rounded.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = {
                        passwordVisible = !passwordVisible
                    }
                )

                // ESQUECEU SENHA
                TextButton(
                    onClick = {},
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Esqueceu a senha?",
                        color = TextGrey.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                // ERRO
                AnimatedVisibility(
                    visible = uiState is AuthUiState.Error,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CinemaRed.copy(
                                alpha = 0.15f
                            )
                        )
                    ) {

                        Text(
                            text = (uiState as? AuthUiState.Error)
                                ?.message
                                ?: "Erro",
                            color = CinemaRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ENTRAR
                GlowingButton(
                    onClick = {
                        viewModel.login(
                            nick,
                            password
                        )
                    },
                    text = "ENTRAR",
                    isLoading = uiState is AuthUiState.Loading,
                    enabled = uiState !is AuthUiState.Loading,
                    glowAlpha = glowAlpha
                )

                Spacer(
                    modifier = Modifier.height(28.dp)
                )

                // DIVISOR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = GraySubtle.copy(alpha = 0.2f)
                    )

                    Text(
                        text = "  ou continue com  ",
                        color = TextGrey.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelSmall
                    )

                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = GraySubtle.copy(alpha = 0.2f)
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // LOGIN SOCIAL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    SocialButton(
                        icon = Icons.Rounded.Email,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )

                    SocialButton(
                        icon = Icons.Rounded.Phone,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )

                    SocialButton(
                        icon = Icons.Rounded.Facebook,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(
                    modifier = Modifier.height(28.dp)
                )

                // REGISTRO
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "Ainda não tem conta?",
                        color = TextGrey.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    TextButton(
                        onClick = onNavigateToRegister
                    ) {

                        Text(
                            text = "Crie agora",
                            color = PremiumGold,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // CONVIDADO
                TextButton(
                    onClick = onContinueAsGuest
                ) {

                    Text(
                        text = "Continuar como convidado",
                        color = TextGrey.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "v1.0.0 • Feito com ❤️ por Lucas Joly",
                    color = TextGrey.copy(alpha = 0.2f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        }
    }
}