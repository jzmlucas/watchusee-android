package br.com.watchusee.android.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.watchusee.android.viewmodel.AuthUiState
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    onContinueAsGuest: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var nick by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess()
        }
    }

    val isPasswordValid = password.length >= 6
    val doPasswordsMatch = password == confirmPassword
    val isFormValid = nick.length >= 3 && isPasswordValid && doPasswordsMatch

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Criar Conta",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                            Color(0xFF0D1117),
                            Color.Transparent
                        )
                    )
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A1F2E),
                            Color(0xFF0A0E14),
                            Color(0xFF05080C)
                        ),
                        radius = 1500f,
                        center = Offset(0f, 0f)
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = 200.dp, y = 200.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PremiumGold.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            radius = 300f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
                    .padding(top = 20.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(
                                elevation = 30.dp,
                                shape = RoundedCornerShape(40.dp),
                                ambientColor = PremiumGold.copy(alpha = 0.2f),
                                spotColor = PremiumGold.copy(alpha = 0.15f)
                            )
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E2433),
                                        Color(0xFF0D1117)
                                    )
                                ),
                                shape = RoundedCornerShape(40.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        PremiumGold.copy(alpha = 0.4f),
                                        PremiumGold.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(40.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = "Criar Conta",
                            modifier = Modifier.size(36.dp),
                            tint = PremiumGold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Junte-se ao WatchUsee",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Text(
                        "Crie seu perfil e organize sua lista de filmes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGrey.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    GlassTextField(
                        value = nick,
                        onValueChange = { nick = it },
                        label = "Nick (Mín. 3 caracteres)",
                        leadingIcon = Icons.Outlined.Person,
                        isError = nick.isNotEmpty() && nick.length < 3,
                        supportingText = if (nick.isNotEmpty() && nick.length < 3) "Mínimo 3 caracteres" else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Senha (Mín. 6 caracteres)",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible },
                        isError = password.isNotEmpty() && !isPasswordValid,
                        supportingText = if (password.isNotEmpty() && !isPasswordValid) "Mínimo 6 caracteres" else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirmar Senha",
                        leadingIcon = Icons.Outlined.Verified,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                        isError = confirmPassword.isNotEmpty() && !doPasswordsMatch,
                        supportingText = if (confirmPassword.isNotEmpty() && !doPasswordsMatch) {
                            "As senhas não coincidem"
                        } else null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (password.isNotEmpty() && isPasswordValid) {
                        PasswordStrengthIndicator(password = password)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
                                containerColor = CinemaRed.copy(alpha = 0.15f)
                            )
                        ) {
                            Text(
                                text = (uiState as? AuthUiState.Error)?.message ?: "Erro",
                                color = CinemaRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    GlowingButton(
                        onClick = { viewModel.register(nick, password) },
                        text = "CRIAR CONTA",
                        isLoading = uiState is AuthUiState.Loading,
                        enabled = uiState !is AuthUiState.Loading && isFormValid,
                        glowAlpha = glowAlpha
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = { /* TODO: Termos */ },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PremiumGold,
                                uncheckedColor = GraySubtle
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Li e aceito os ",
                            color = TextGrey.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        TextButton(
                            onClick = { /* TODO: Termos */ },
                            modifier = Modifier.padding(horizontal = 0.dp)
                        ) {
                            Text(
                                "Termos de Uso",
                                color = PremiumGold,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divisor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp),
                            color = GraySubtle.copy(alpha = 0.2f)
                        )
                        Text(
                            "  ou  ",
                            color = TextGrey.copy(alpha = 0.3f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Divider(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp),
                            color = GraySubtle.copy(alpha = 0.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onContinueAsGuest,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            "Continuar como convidado",
                            color = TextGrey.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(password: String) {
    val strength = calculatePasswordStrength(password)
    val color = when (strength) {
        0 -> CinemaRed
        1 -> Color(0xFFFFA500)
        2 -> PremiumGold
        3 -> Color(0xFF4CAF50)
        else -> TextGrey
    }
    val label = when (strength) {
        0 -> "Fraca"
        1 -> "Média"
        2 -> "Boa"
        3 -> "Forte"
        else -> ""
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            if (index <= strength) color else GraySubtle.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
        if (label.isNotEmpty()) {
            Text(
                "Força: $label",
                color = color,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun calculatePasswordStrength(password: String): Int {
    var strength = 0
    if (password.length >= 8) strength++
    if (password.any { it.isDigit() }) strength++
    if (password.any { it.isUpperCase() }) strength++
    if (password.any { it.isLowerCase() }) strength++
    if (password.any { !it.isLetterOrDigit() }) strength++
    return (strength / 2).coerceAtMost(3)
}