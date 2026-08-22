package com.pawan.autovoice.car

import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * ## AutoVoiceCarAppService
 *
 * The entry point for the **Car App Library** surface — distinct from
 * `MainActivity`'s hand-rolled Compose UI in `presentation/`.
 *
 * ### Why does this project have TWO separate car UIs?
 * This is deliberate, and worth explaining in an interview:
 *
 * - `presentation/dashboard/DashboardScreen.kt` (Compose, in `MainActivity`)
 *   represents the **AAOS "Car ready mobile app" / center-stack app**
 *   surface — an app that OWNS its own pixels, fully custom UI, running
 *   directly on the vehicle's Android Automotive OS.
 * - This `car/` package is the **Car App Library** surface — used for
 *   **Android Auto** (projected from a phone) and, optionally, AAOS too.
 *   Car App Library apps do NOT draw their own pixels for most content;
 *   they hand the *host* (Android Auto / AAOS system UI) a declarative
 *   [androidx.car.app.model.Template] (`ListTemplate`, `GridTemplate`,
 *   `MessageTemplate`, etc), and the host renders it using OEM-approved,
 *   pre-audited layouts. This is literally what "Distraction Optimization
 *   Rules" means in practice: you never get to choose your own font size,
 *   touch target size, or animation on this surface — the host enforces
 *   all of it for you, which is precisely why the Car App Library exists
 *   as a *separate, restricted* API from a normal Activity.
 *
 * A real production app for a JD like this one would need to demonstrate
 * BOTH surfaces, because different OEM head units support different ones —
 * this file is the scaffold for that second surface.
 */
public class AutoVoiceCarAppService : CarAppService() {

    /**
     * The host (Android Auto / AAOS) calls this to validate that OUR app is
     * a legitimate, signed client before it will render any of our
     * templates — [HostValidator.ALLOW_ALL_HOSTS_VALIDATOR] is fine for
     * local development ONLY; a production release build must supply a
     * real allow-list of trusted host package signatures instead.
     */
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: android.content.Intent): Screen =
            AutoVoiceCarScreen(carContext)
    }
}
