package br.com.watchusee.android.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import br.com.watchusee.android.ui.theme.*

@Composable
fun AuthBackground(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D0F))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B0D0F),
                            Color(0xFF161A20)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PremiumGold.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 900f,
                        center = Offset(0f, 0f)
                    )
                )
        )

        content()
    }
}

@Composable
fun AuthContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val horizontalPadding = when {
            maxWidth < 360.dp -> 18.dp
            maxWidth < 600.dp -> 24.dp
            else -> 32.dp
        }

        val contentWidth = minOf(maxWidth - horizontalPadding * 2, 520.dp)

        Column(
            modifier = Modifier
                .width(contentWidth)
                .align(Alignment.TopCenter)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun AuthBrandHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WATCHUSEE",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 38.sp,
                letterSpacing = 10.sp
            ),
            fontWeight = FontWeight.Black,
            color = PremiumGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 26.sp
            ),
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGrey.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SocialButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFF1A1F2E).copy(alpha = 0.5f),
            contentColor = TextWhite
        ),
        border = BorderStroke(
            width = 1.dp,
            color = GraySubtle.copy(alpha = 0.2f)
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

    val shape = RoundedCornerShape(16.dp)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (isFocused) {
                    PremiumGold
                } else {
                    TextGrey.copy(alpha = 0.5f)
                }
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(
                    onClick = onPasswordToggle
                ) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Rounded.Visibility
                        } else {
                            Icons.Rounded.VisibilityOff
                        },
                        contentDescription = if (passwordVisible) {
                            "Ocultar senha"
                        } else {
                            "Mostrar senha"
                        },
                        tint = TextGrey.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            null
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                isFocused = it.isFocused
            }
            .shadow(
                elevation = if (isFocused) 18.dp else 6.dp,
                shape = shape,
                ambientColor = if (isFocused) {
                    PremiumGold.copy(alpha = 0.14f)
                } else {
                    Color.Transparent
                },
                spotColor = if (isFocused) {
                    PremiumGold.copy(alpha = 0.10f)
                } else {
                    Color.Transparent
                }
            )
            .background(
                color = Color(0xFF151A25).copy(alpha = 0.72f),
                shape = shape
            )
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                brush = if (isFocused) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            PremiumGold.copy(alpha = 0.65f),
                            PremiumGold.copy(alpha = 0.20f),
                            PremiumGold.copy(alpha = 0.65f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            GraySubtle.copy(alpha = 0.30f),
                            GraySubtle.copy(alpha = 0.10f)
                        )
                    )
                },
                shape = shape
            ),
        shape = shape,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = TextWhite,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        ),
        visualTransformation = if (
            isPassword && !passwordVisible
        ) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = if (isPassword) {
            KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        } else {
            KeyboardOptions.Default
        },
        isError = isError,
        supportingText = supportingText?.let { text ->
            {
                Text(
                    text = text,
                    color = CinemaRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
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
    glowAlpha: Float,
    height: Dp = 56.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-4).dp)
                .blur(20.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            PremiumGold.copy(
                                alpha = glowAlpha * 0.4f
                            ),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = PremiumGold.copy(alpha = 0.20f),
                    spotColor = PremiumGold.copy(alpha = 0.15f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PremiumGold,
                contentColor = Color.Black,
                disabledContainerColor = GraySubtle,
                disabledContentColor = TextGrey
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(23.dp),
                    color = Color.Black,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = text,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(
    password: String
) {
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
                            color = if (index <= strength) {
                                color
                            } else {
                                GraySubtle.copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        if (label.isNotEmpty()) {
            Text(
                text = "Força: $label",
                color = color,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

fun calculatePasswordStrength(
    password: String
): Int {
    var strength = 0

    if (password.length >= 8) strength++
    if (password.any { it.isDigit() }) strength++
    if (password.any { it.isUpperCase() }) strength++
    if (password.any { it.isLowerCase() }) strength++
    if (password.any { !it.isLetterOrDigit() }) strength++

    return (strength / 2).coerceAtMost(3)
}