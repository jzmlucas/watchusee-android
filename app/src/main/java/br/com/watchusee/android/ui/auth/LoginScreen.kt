package br.com.watchusee.android.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
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

    var nick by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

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
            .background(Color(0xFF0B0D0F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Brand Header matching mockup (with app name watchusee)
            Text(
                text = "watchusee",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    letterSpacing = 2.sp
                ),
                fontWeight = FontWeight.Black,
                color = CinemaText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Todo filme deixa uma cena.",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold,
                color = CinemaAccent,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Descubra, avalie e compartilhe as histórias que continuam depois dos créditos.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = CinemaMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Inputs
            GlassTextField(
                value = nick,
                onValueChange = { nick = it },
                label = "Usuário ou E-mail",
                leadingIcon = Icons.Rounded.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassTextField(
                value = password,
                onValueChange = { password = it },
                label = "Senha",
                leadingIcon = Icons.Rounded.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Error Message
            AnimatedVisibility(
                visible = uiState is AuthUiState.Error,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1518))
                ) {
                    Text(
                        text = (uiState as? AuthUiState.Error)?.message ?: "Erro ao entrar",
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Entrar Button (Mockup styling: #ADF03C pill button)
            Button(
                onClick = { viewModel.login(nick, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaAccent,
                    contentColor = Color(0xFF0B0D08)
                ),
                enabled = uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text(
                        text = "Entrar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Criar conta Button (Mockup styling: Dark surface pill button)
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF111418),
                    contentColor = CinemaText
                ),
                border = BorderStroke(1.dp, Color(0xFF2A3038))
            ) {
                Text(
                    text = "Criar conta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Divider "ou continue com"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1A1F26)
                )
                Text(
                    text = "  ou continue com  ",
                    color = CinemaMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1A1F26)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Login (Google / Apple style pills or buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF111418),
                        contentColor = CinemaText
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2A3038))
                ) {
                    Text(text = "Google", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF111418),
                        contentColor = CinemaText
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2A3038))
                ) {
                    Text(text = "Apple", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer texts from mockup
            Text(
                text = "Sua próxima sessão começa aqui",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = CinemaText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Crie seu repertório e encontre pessoas que assistem ao mundo como você.",
                style = MaterialTheme.typography.bodySmall,
                color = CinemaMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ao continuar, você aceita os Termos de uso e a Política de privacidade do watchusee.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = CinemaMuted.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
