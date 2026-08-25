package com.pawan.autovoice.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pawan.autovoice.presentation.components.ClimateWidget
import com.pawan.autovoice.presentation.components.MediaWidget
import com.pawan.autovoice.presentation.components.VoiceHistoryWidget
import com.pawan.autovoice.presentation.components.VoiceOrb
import com.pawan.autovoice.presentation.theme.AutoVoiceTheme
import com.pawan.autovoice.presentation.theme.CarDimens

/**
 * ## DashboardScreen
 *
 * Stateful entry point wired to Hilt: obtains a [DashboardViewModel] scoped
 * to this screen and collects its [DashboardUiState] as Compose state via
 * [collectAsStateWithLifecycle] — the lifecycle-aware collector that
 * automatically pauses collection when the screen is stopped (e.g. the
 * backup-camera-interrupt scenario from the interview doc's Scenario 2:
 * we simply stop collecting, and resume with whatever the LATEST state is
 * the moment the screen restarts, which is exactly the "UI correctly
 * catches up to the finished state without dropping data" requirement —
 * StateFlow's replay-latest-value semantics solve this for free).
 *
 * All the actual rendering happens in the stateless [DashboardContent]
 * below it, which takes a plain [DashboardUiState] + a lambda — this split
 * is what makes [DashboardContent] trivially previewable and UI-testable
 * without a real ViewModel or Hilt graph.
 */
@Composable
public fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(uiState = uiState, onIntent = viewModel::onIntent)
}

@Composable
internal fun DashboardContent(uiState: DashboardUiState, onIntent: (DashboardIntent) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }

    // One-shot error surfacing: whenever errorMessage transitions from null
    // to non-null we show it once, then immediately tell the ViewModel it
    // was shown (clearing it back to null) — the "consume once" pattern
    // referenced in DashboardUiState's KDoc.
    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onIntent(DashboardIntent.ErrorMessageShown)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) { androidx.compose.material3.Text(data.visuals.message) }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Left Column: Climate and Media
                Column(verticalArrangement = Arrangement.spacedBy(CarDimens.MinTouchTargetSpacing)) {
                    ClimateWidget(state = uiState.climateState)
                    MediaWidget(
                        state = uiState.nowPlaying,
                        onTogglePlayback = { onIntent(DashboardIntent.ToggleMediaPlayback) }
                    )
                }

                // Right: Voice Orb
                Box(
                    modifier = Modifier.padding(start = CarDimens.MinTouchTargetSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    VoiceOrb(
                        sessionState = uiState.voiceSessionState,
                        onTap = {
                            onIntent(DashboardIntent.StartListening)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom: Voice History
            VoiceHistoryWidget(
                history = uiState.voiceHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }
}

@Preview(widthDp = 1024, heightDp = 480, showBackground = true)
@Composable
private fun DashboardContentPreview() {
    AutoVoiceTheme {
        DashboardContent(
            uiState = DashboardUiState(
                voiceSessionState = VoiceSessionState.Speaking("Climate set to 22°"),
                climateState = ClimateUiState(isOn = true, temperatureCelsius = 22),
            ),
            onIntent = {},
        )
    }
}
