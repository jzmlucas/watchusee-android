package br.com.watchusee.android.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onContinueAsGuest: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var nick by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Animações
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
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
        // Efeito de brilho de fundo
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PremiumGold.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 80.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo com efeito de brilho
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(
                            elevation = 40.dp,
                            shape = RoundedCornerShape(60.dp),
                            ambientColor = PremiumGold.copy(alpha = 0.3f),
                            spotColor = PremiumGold.copy(alpha = 0.2f)
                        )
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1E2433),
                                    Color(0xFF0D1117)
                                )
                            ),
                            shape = RoundedCornerShape(60.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    PremiumGold.copy(alpha = 0.6f),
                                    PremiumGold.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(60.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalMovies,
                        contentDescription = "Logo",
                        modifier = Modifier.size(56.dp),
                        tint = PremiumGold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "WATCHUSEE",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 42.sp,
                        letterSpacing = 12.sp
                    ),
                    fontWeight = FontWeight.Black,
                    color = PremiumGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Sua agenda de cinema na palma da mão",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGrey.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassTextField(
                    value = nick,
                    onValueChange = { nick = it },
                    label = "Login",
                    leadingIcon = Icons.Outlined.Person
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Senha",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible }
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        "Esqueceu a senha?",
                        color = TextGrey.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall
                    )
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
                    onClick = { viewModel.login(nick, password) },
                    text = "ENTRAR",
                    isLoading = uiState is AuthUiState.Loading,
                    enabled = uiState !is AuthUiState.Loading,
                    glowAlpha = glowAlpha
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp),
                        color = GraySubtle.copy(alpha = 0.3f)
                    )
                    Text(
                        "  ou continue com  ",
                        color = TextGrey.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Divider(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp),
                        color = GraySubtle.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SocialButton(
                        icon = Icons.Outlined.Email,
                        onClick = { /* TODO: Login com Google */ },
                        modifier = Modifier.weight(1f)
                    )
                    SocialButton(
                        icon = Icons.Outlined.Phone,
                        onClick = { /* TODO: Login com Apple */ },
                        modifier = Modifier.weight(1f)
                    )
                    SocialButton(
                        icon = Icons.Outlined.Facebook,
                        onClick = { /* TODO: Login com Facebook */ },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ainda não tem conta?",
                        color = TextGrey.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            "Crie agora",
                            color = PremiumGold,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

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

            Text(
                "v1.0.0 • Feito com ❤️ por Lucas Joly",
                color = TextGrey.copy(alpha = 0.2f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    isError: Boolean = false,
    supportingText: String? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGrey) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isFocused) PremiumGold else TextGrey.copy(alpha = 0.5f)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .shadow(
                elevation = if (isFocused) 20.dp else 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isFocused) PremiumGold.copy(alpha = 0.15f) else Color.Transparent,
                spotColor = if (isFocused) PremiumGold.copy(alpha = 0.1f) else Color.Transparent
            )
            .background(
                Color(0xFF1A1F2E).copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                brush = if (isFocused) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            PremiumGold.copy(alpha = 0.6f),
                            PremiumGold.copy(alpha = 0.2f),
                            PremiumGold.copy(alpha = 0.6f)
                        )
                    )
                } else Brush.horizontalGradient(
                    colors = listOf(
                        GraySubtle.copy(alpha = 0.3f),
                        GraySubtle.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = TextWhite,
            fontWeight = FontWeight.Medium
        ),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = TextGrey.copy(alpha = 0.5f)
                    )
                }
            }
        } else null,
        isError = isError,
        supportingText = if (supportingText != null) {
            { Text(supportingText, color = if (isError) CinemaRed else TextGrey) }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = PremiumGold,
            focusedLabelColor = PremiumGold,
            unfocusedLabelColor = TextGrey,
            focusedLeadingIconColor = PremiumGold,
            unfocusedLeadingIconColor = TextGrey.copy(alpha = 0.5f),
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite
        )
    )
}

@Composable
fun GlowingButton(
    onClick: () -> Unit,
    text: String,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    glowAlpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-4).dp)
                .blur(20.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            PremiumGold.copy(alpha = 0.0f),
                            PremiumGold.copy(alpha = glowAlpha * 0.4f),
                            PremiumGold.copy(alpha = 0.0f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = PremiumGold.copy(alpha = 0.2f),
                    spotColor = PremiumGold.copy(alpha = 0.15f)
                ),
            shape = RoundedCornerShape(16.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = PremiumGold,
                contentColor = Color.Black,
                disabledContainerColor = GraySubtle,
                disabledContentColor = TextGrey
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun SocialButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .background(
                Color(0xFF1A1F2E).copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = TextWhite
        ),
        border = BorderStroke(
            width = 1.dp,
            color = GraySubtle.copy(alpha = 0.3f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = TextGrey
        )
    }
}