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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.viewmodel.AuthUiState
import br.com.watchusee.android.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
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

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var confirmPasswordVisible by remember {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(
        label = "register_glow"
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
        label = "register_glow_alpha"
    )

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess()
        }
    }

    val isPasswordValid =
        password.length >= 6

    val doPasswordsMatch =
        password == confirmPassword

    val isFormValid =
        nick.length >= 3 &&
                isPasswordValid &&
                doPasswordsMatch

    AuthBackground {

        Scaffold(
            containerColor = Color.Transparent,

            topBar = {

                TopAppBar(

                    title = {
                        Text(
                            text = "Criar Conta",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = onBack
                        ) {

                            Icon(
                                imageVector =
                                    Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Voltar",
                                tint = TextWhite
                            )
                        }
                    },

                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),

                    modifier = Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF080B10).copy(alpha = 0.70f),
                                Color.Transparent
                            )
                        )
                    )
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 28.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AuthContent {

                    AuthBrandHeader(
                        title = "Junte-se ao WatchUsee",
                        subtitle = "Crie seu perfil e organize sua lista de filmes"
                    )

                    Spacer(
                        modifier = Modifier.height(34.dp)
                    )

                    // NICK
                    GlassTextField(
                        value = nick,
                        onValueChange = {
                            nick = it
                        },
                        label = "Nick (Mín. 3 caracteres)",
                        leadingIcon = Icons.Rounded.Person,
                        isError =
                            nick.isNotEmpty() &&
                                    nick.length < 3,
                        supportingText =
                            if (
                                nick.isNotEmpty() &&
                                nick.length < 3
                            ) {
                                "Mínimo 3 caracteres"
                            } else {
                                null
                            }
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
                        label = "Senha (Mín. 6 caracteres)",
                        leadingIcon = Icons.Rounded.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = {
                            passwordVisible =
                                !passwordVisible
                        },
                        isError =
                            password.isNotEmpty() &&
                                    !isPasswordValid,
                        supportingText =
                            if (
                                password.isNotEmpty() &&
                                !isPasswordValid
                            ) {
                                "Mínimo 6 caracteres"
                            } else {
                                null
                            }
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    // CONFIRMAR SENHA
                    GlassTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                        },
                        label = "Confirmar Senha",
                        leadingIcon = Icons.Rounded.Verified,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordToggle = {
                            confirmPasswordVisible =
                                !confirmPasswordVisible
                        },
                        isError =
                            confirmPassword.isNotEmpty() &&
                                    !doPasswordsMatch,
                        supportingText =
                            if (
                                confirmPassword.isNotEmpty() &&
                                !doPasswordsMatch
                            ) {
                                "As senhas não coincidem"
                            } else {
                                null
                            }
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    // FORÇA DA SENHA
                    if (
                        password.isNotEmpty() &&
                        isPasswordValid
                    ) {

                        PasswordStrengthIndicator(
                            password = password
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    // ERRO
                    AnimatedVisibility(
                        visible =
                            uiState is AuthUiState.Error,
                        enter =
                            fadeIn() +
                                    slideInVertically(),
                        exit =
                            fadeOut() +
                                    slideOutVertically()
                    ) {

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),

                            shape =
                                RoundedCornerShape(8.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        CinemaRed.copy(
                                            alpha = 0.15f
                                        )
                                )
                        ) {

                            Text(
                                text =
                                    (
                                            uiState as?
                                                    AuthUiState.Error
                                            )?.message
                                        ?: "Erro",

                                color = CinemaRed,

                                style =
                                    MaterialTheme.typography.bodySmall,

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),

                                textAlign =
                                    TextAlign.Center
                            )
                        }
                    }

                    // CRIAR CONTA
                    GlowingButton(
                        onClick = {
                            viewModel.register(
                                nick,
                                password
                            )
                        },
                        text = "CRIAR CONTA",
                        isLoading =
                            uiState is AuthUiState.Loading,
                        enabled =
                            uiState !is AuthUiState.Loading &&
                                    isFormValid,
                        glowAlpha = glowAlpha
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    // TERMOS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.Center,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = true,
                            onCheckedChange = {},
                            colors =
                                CheckboxDefaults.colors(
                                    checkedColor =
                                        PremiumGold,
                                    uncheckedColor =
                                        GraySubtle
                                ),
                            modifier =
                                Modifier.size(20.dp)
                        )

                        Text(
                            text = "Li e aceito os ",
                            color =
                                TextGrey.copy(
                                    alpha = 0.6f
                                ),
                            style =
                                MaterialTheme.typography.labelSmall
                        )

                        TextButton(
                            onClick = {},
                            contentPadding =
                                PaddingValues(
                                    horizontal = 2.dp
                                )
                        ) {

                            Text(
                                text = "Termos de Uso",
                                color = PremiumGold,
                                style =
                                    MaterialTheme.typography.labelSmall,
                                fontWeight =
                                    FontWeight.Medium
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    // DIVISOR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        HorizontalDivider(
                            modifier =
                                Modifier.weight(1f),
                            color =
                                GraySubtle.copy(
                                    alpha = 0.2f
                                )
                        )

                        Text(
                            text = "  ou  ",
                            color =
                                TextGrey.copy(
                                    alpha = 0.3f
                                ),
                            style =
                                MaterialTheme.typography.labelSmall
                        )

                        HorizontalDivider(
                            modifier =
                                Modifier.weight(1f),
                            color =
                                GraySubtle.copy(
                                    alpha = 0.2f
                                )
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    // CONVIDADO
                    TextButton(
                        onClick =
                            onContinueAsGuest,
                        modifier =
                            Modifier.padding(
                                bottom = 20.dp
                            )
                    ) {

                        Text(
                            text =
                                "Continuar como convidado",
                            color =
                                TextGrey.copy(
                                    alpha = 0.4f
                                ),
                            style =
                                MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}