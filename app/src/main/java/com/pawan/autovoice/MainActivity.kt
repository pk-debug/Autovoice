package com.pawan.autovoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pawan.autovoice.presentation.dashboard.DashboardScreen
import com.pawan.autovoice.presentation.theme.AutoVoiceTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * ## MainActivity
 *
 * The Compose-rendered head-unit dashboard. In the manifest this activity
 * is declared with:
 *
 * ```xml
 * <meta-data android:name="distractionOptimized" android:value="true" />
 * ```
 *
 * which is what tells Android Automotive OS's UX Restrictions Engine this
 * activity is safe to remain visible/interactive while the vehicle is in
 * motion — see "Distraction optimization" in Android's AAOS overview docs.
 * **Only this activity carries that flag**; if this project grows a second
 * activity (settings, sign-in) it must NOT be marked distraction-optimized,
 * per the platform docs' explicit warning that doing so on the wrong
 * activity gets an app rejected from Google Play's automotive review.
 *
 * `enableEdgeToEdge()` + [androidx.compose.foundation.layout.fillMaxSize]
 * lets our own Compose layout own the full display, including drawing
 * behind the car's system bars where safe — car system bars can be
 * significantly larger than on phones/tablets (see Google's AAOS system-bar
 * guidance), so we rely on Scaffold's `innerPadding` (see
 * `DashboardScreen.kt`) rather than hardcoding any inset value ourselves.
 */
@AndroidEntryPoint
public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoVoiceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardScreen()
                }
            }
        }
    }
}
