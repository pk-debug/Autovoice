package com.pawan.autovoice.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ## Typography
 *
 * Font sizes map directly onto Google's Design for Driving spec, which is
 * unusually specific here (most style guides leave this to taste):
 *
 * - **Primary text — 32sp** ("required for decision making, such as song
 *   titles or contact names") -> [Typography.headlineMedium], used for the
 *   now-playing track title and the voice orb's status word.
 * - **Secondary text — 24sp** ("supporting information, such as artist
 *   name") -> [Typography.bodyLarge], used for climate sub-labels and the
 *   media artist line.
 *
 * We deliberately do NOT reuse Material 3's stock default type scale
 * (its `bodyLarge` default is 16sp) — those sizes are tuned for a phone
 * held ~30cm from the eyes, not a center-stack display viewed from
 * across the cabin.
 */
val AutomotiveTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 24.sp,
    ),
)
