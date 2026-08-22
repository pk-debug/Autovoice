package com.pawan.autovoice.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pawan.autovoice.presentation.dashboard.ClimateUiState
import com.pawan.autovoice.presentation.theme.CarDimens

/**
 * ## ClimateWidget
 *
 * Read-only display card for the current climate state. Per the "Parked
 * Experiences" (PE-1) and "Image Usage" style guidelines for automotive
 * templated categories, this widget intentionally shows NO interactive
 * fine-grained controls (no +/- stepper, no slider) — per the JD-linked IoT
 * functionality guidance, fine-grained control adjustment (like a precise
 * temperature slider) is exactly the kind of interaction that should be
 * voice-driven while in motion, not a multi-tap on-screen control the
 * driver has to look at and hit precisely.
 *
 * Temperature text uses [MaterialTheme.typography.headlineMedium] — the
 * 32sp "primary/decision-making" size from the driving typography spec,
 * since the current cabin temperature is exactly the kind of at-a-glance
 * decision-relevant info the guideline describes.
 */
@Composable
public fun ClimateWidget(state: ClimateUiState, modifier: Modifier = Modifier) {
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
                text = "Climate",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                Text(
                    text = if (state.isOn) "${state.temperatureCelsius}°" else "Off",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
