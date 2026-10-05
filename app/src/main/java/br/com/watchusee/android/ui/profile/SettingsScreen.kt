package br.com.watchusee.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.data.dto.UserResponse
import br.com.watchusee.android.ui.theme.*
import br.com.watchusee.android.util.AvatarMapper
import br.com.watchusee.android.viewmodel.AuthViewModel
import br.com.watchusee.android.viewmodel.PasswordActionState
import br.com.watchusee.android.viewmodel.ProfileUiState
import br.com.watchusee.android.viewmodel.ProfileViewModel
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val profileState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val passwordActionState by viewModel.passwordActionState.collectAsStateWithLifecycle()

    val profile = (profileState as? ProfileUiState.Success)?.profile
    val nick = profile?.nick ?: currentUser?.nick ?: "Usuário"
    val avatarUrl = AvatarMapper.getIconUrl(profile?.avatarIcon)

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    LaunchedEffect(passwordActionState) {
        if (passwordActionState is PasswordActionState.Success) {
            snackbarHostState.showSnackbar("Senha alterada com sucesso!")
            showPasswordDialog = false
            oldPassword = ""
            newPassword = ""
            confirmPassword = ""
            viewModel.resetPasswordActionState()
        } else if (passwordActionState is PasswordActionState.Error) {
            snackbarHostState.showSnackbar((passwordActionState as PasswordActionState.Error).message)
            viewModel.resetPasswordActionState()
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B0D0F),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(modifier = Modifier.statusBarsPadding()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Configurações",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CinemaText
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Box(
                                modifier = Modifier
                                    .size(39.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF111418))
                                    .border(1.dp, Color(0xFF2A3038), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar", tint = CinemaText)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0B0D0F)
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.5.dp))
                    .background(Color(0xFF161A20))
                    .border(1.dp, Color(0xFF2A3038), RoundedCornerShape(13.5.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CinemaPanel)
                        .border(2.dp, CinemaAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = nick,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = nick.take(1).uppercase(),
                            color = CinemaAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = nick,
                        color = CinemaText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${profile?.nick?.lowercase() ?: "usuario"} · Conta CENA+",
                        color = CinemaMuted,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaAccent,
                        contentColor = Color(0xFF0B0D08)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = "Editar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Section: Conta e preferências
            SettingsSectionHeader("Conta e preferências")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Rounded.Person,
                    title = "Conta",
                    subtitle = "E-mail, senha e perfil",
                    onClick = { showPasswordDialog = true }
                )
                HorizontalDivider(color = Color(0xFF1A1F26), thickness = 1.dp)
                SettingsItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Aparência",
                    subtitle = "Escuro",
                    onClick = {}
                )
                HorizontalDivider(color = Color(0xFF1A1F26), thickness = 1.dp)
                SettingsItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Notificações",
                    subtitle = "Indicações, social e novidades",
                    onClick = {}
                )
            }

            // Section: Experiência
            SettingsSectionHeader("Experiência")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Rounded.Lock,
                    title = "Privacidade",
                    subtitle = "Atividade, listas e bloqueios",
                    onClick = {}
                )
                HorizontalDivider(color = Color(0xFF1A1F26), thickness = 1.dp)
                SettingsItem(
                    icon = Icons.Rounded.PlayCircle,
                    title = "Reprodução",
                    subtitle = "Autoplay, qualidade e legendas",
                    onClick = {}
                )
                HorizontalDivider(color = Color(0xFF1A1F26), thickness = 1.dp)
                SettingsItem(
                    icon = Icons.Rounded.Download,
                    title = "Downloads no Wi-Fi",
                    subtitle = "Gerenciamento de cache e mídias",
                    onClick = {}
                )
            }

            // Section: Suporte
            SettingsSectionHeader("Suporte")
            SettingsCard {
                SettingsItem(
                    icon = Icons.AutoMirrored.Rounded.Help,
                    title = "Ajuda e suporte",
                    subtitle = "Central de ajuda e contato",
                    onClick = {}
                )
                HorizontalDivider(color = Color(0xFF1A1F26), thickness = 1.dp)
                SettingsItem(
                    icon = Icons.Rounded.Description,
                    title = "Termos e privacidade",
                    subtitle = "Políticas da plataforma",
                    onClick = {}
                )
            }

            // Version and Logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "v2.8.1",
                    color = CinemaMuted,
                    fontSize = 12.sp
                )
            }

            // Sair da conta Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.5.dp))
                    .background(Color(0xFF111418))
                    .border(1.dp, Color(0xFF2A3038), RoundedCornerShape(13.5.dp))
                    .clickable { authViewModel.logout(onLogout) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2A1518)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Sair da conta",
                        color = Color(0xFFFF6B6B),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = CinemaMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            containerColor = Color(0xFF161A20),
            titleContentColor = CinemaText,
            textContentColor = CinemaMuted,
            title = { Text("Alterar Senha") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Mantenha sua conta segura alterando sua senha regularmente.", fontSize = 12.sp, color = CinemaMuted)
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Senha Atual") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (oldVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { oldVisible = !oldVisible }) {
                                Icon(if (oldVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff, null)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nova Senha") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newVisible = !newVisible }) {
                                Icon(if (newVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff, null)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar Nova Senha") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(if (confirmVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff, null)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.changePassword(oldPassword, newPassword) },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaAccent, contentColor = Color.Black),
                    enabled = oldPassword.isNotBlank() && newPassword.length >= 8 && newPassword == confirmPassword && passwordActionState !is PasswordActionState.Loading
                ) {
                    if (passwordActionState is PasswordActionState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black)
                    } else {
                        Text("Atualizar", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancelar", color = CinemaMuted)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = CinemaMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.5.dp))
            .background(Color(0xFF111418))
            .border(1.dp, Color(0xFF2A3038), RoundedCornerShape(13.5.dp)),
        content = content
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF161A20)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CinemaAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = CinemaText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = CinemaMuted,
                    fontSize = 11.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = CinemaMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
