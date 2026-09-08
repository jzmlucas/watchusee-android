package br.com.watchusee.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.watchusee.android.viewmodel.SearchViewModel

@Composable
fun WatchuSeeTopBar(
    title: String,
    onProfileClick: () -> Unit,
    onHomeClick: (() -> Unit)? = null,
    onRequireLogin: ((() -> Unit)) -> Unit = {},
    isAuthenticated: Boolean = false,
    searchViewModel: SearchViewModel? = null,
    onSearchToggle: ((Boolean) -> Unit)? = null,
    onFilterClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearchActive && searchViewModel != null) {
            val searchQuery by searchViewModel.query
                .collectAsStateWithLifecycle()

            IconButton(
                onClick = {
                    isSearchActive = false
                    searchViewModel.onQueryChange("")
                    onSearchToggle?.invoke(false)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Fechar busca",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            TextField(
                value = searchQuery,
                onValueChange = {
                    searchViewModel.onQueryChange(it)
                },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Buscar filmes e séries...",
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                            .copy(alpha = 0.65f),
                        fontSize = 15.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchViewModel.onQueryChange("")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Limpar busca",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor =
                        MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor =
                        MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor =
                        MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor =
                        MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor =
                        MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor =
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor =
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        searchViewModel.onQueryChange(searchQuery)
                    }
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp
                )
            )

            IconButton(
                onClick = {
                    onFilterClick?.invoke()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Filtros",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (onHomeClick != null) {
                            Modifier.clickable(
                                onClick = onHomeClick
                            )
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Movie,
                    contentDescription = "WatchUsee",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(25.dp)
                )

                Spacer(
                    modifier = Modifier.width(9.dp)
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
            }

            if (searchViewModel != null) {
                IconButton(
                    onClick = {
                        isSearchActive = true
                        onSearchToggle?.invoke(true)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = {
                    if (isAuthenticated) {
                        onProfileClick()
                    } else {
                        onRequireLogin(onProfileClick)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Perfil",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}