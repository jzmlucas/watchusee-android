package br.com.watchusee.android.ui.shares.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import br.com.watchusee.android.data.dto.ShareStatus

object ShareStatusTokens {

    private val PendingColor = Color(0xFFFFA000)
    private val AcceptedColor = Color(0xFF2E7D32)

    @Composable
    @ReadOnlyComposable
    fun color(status: ShareStatus): Color = when (status) {
        ShareStatus.PENDING -> PendingColor
        ShareStatus.ACCEPTED -> AcceptedColor
        ShareStatus.REJECTED -> MaterialTheme.colorScheme.error
    }

    fun icon(status: ShareStatus): ImageVector = when (status) {
        ShareStatus.PENDING -> Icons.Rounded.Schedule
        ShareStatus.ACCEPTED -> Icons.Rounded.CheckCircle
        ShareStatus.REJECTED -> Icons.Rounded.Cancel
    }

    fun label(status: ShareStatus): String = when (status) {
        ShareStatus.PENDING -> "Pendente"
        ShareStatus.ACCEPTED -> "Aceito"
        ShareStatus.REJECTED -> "Recusado"
    }
}