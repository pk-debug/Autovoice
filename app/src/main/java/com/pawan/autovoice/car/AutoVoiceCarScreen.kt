package com.pawan.autovoice.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.ItemList
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat

/**
 * ## AutoVoiceCarScreen
 *
 * A minimal [Screen] rendering a [GridTemplate] with quick-action tiles
 * (Climate, Media, Navigate) — the Car App Library equivalent of the
 * Compose dashboard's widgets, but built entirely from **declarative model
 * objects** instead of drawn pixels.
 *
 * ### The AC-1 quality-bar constraint this class is designed around
 * The Android for Cars app-quality checklist's "App Doesn't Crash" (AC-1)
 * criterion requires templated apps to let users complete tasks in **five
 * screens or fewer**. This screen is intentionally flat — one grid, no deep
 * navigation stack — specifically to respect that budget; a real build
 * would track remaining "screen depth" explicitly rather than trusting
 * every future contributor to remember the limit.
 *
 * `invalidate()` is called by [onGetTemplate]'s caller whenever the
 * underlying data changes (e.g. a voice command updates climate state);
 * Car App Library screens are inherently **pull-based** — the host asks
 * you for a fresh [Template] snapshot rather than you pushing pixel
 * updates, which is a different mental model from Compose's push-based
 * recomposition and worth calling out explicitly if asked to compare them.
 */
public class AutoVoiceCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val items = ItemList.Builder()
            .addItem(gridItem(title = "Climate", subtitle = "Off"))
            .addItem(gridItem(title = "Media", subtitle = "Nothing playing"))
            .addItem(gridItem(title = "Navigate", subtitle = "Tap to start"))
            .build()

        return GridTemplate.Builder()
            .setTitle("AutoVoice")
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(items)
            .build()
    }

    private fun gridItem(title: String, subtitle: String): GridItem =
        GridItem.Builder()
            .setTitle(title)
            .setText(subtitle)
            .setImage(
                CarIcon.Builder(
                    IconCompat.createWithResource(carContext, androidx.car.app.R.drawable.arrow_right_turn),
                ).build(),
            )
            .setOnClickListener {
                // A real implementation would route this through the same
                // ProcessVoiceCommandUseCase / repository the Compose UI
                // uses — the Car App Library surface and the Compose
                // surface should always share the SAME domain layer, never
                // duplicate business logic per-surface.
                screenManager.push(this)
            }
            .build()
}
