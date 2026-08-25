package com.pawan.autovoice.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pawan.autovoice.presentation.dashboard.MediaUiState
import com.pawan.autovoice.presentation.theme.CarDimens

/**
 * ## MediaWidget
 *
 * A widget for controlling media playback. Shows the current track title
 * and provides a play/pause toggle.
 */
@Composable
public fun MediaWidget(
    state: MediaUiState,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .size(width = CarDimens.MinTouchTarget * 3, height = CarDimens.MinTouchTarget * 2),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Media",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.trackTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onTogglePlayback,
                    modifier = Modifier.size(CarDimens.MinTouchTarget)
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) {
                            // Using a built-in icon for simplicity, 
                            // though a real app would have custom automotive assets.
                            Icons.Default.PlayArrow 
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
