package com.pawan.autovoice

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * ## AutoVoiceApplication
 *
 * Root [Application] class, annotated `@HiltAndroidApp` to generate the
 * top-level Hilt component that every `@AndroidEntryPoint`/`@HiltViewModel`
 * in the app attaches to.
 *
 * Deliberately empty [onCreate] beyond the Hilt requirement: per the "SDK
 * Initialization" interview question (Q10), NOTHING related to `:voice-sdk`
 * happens here. The [com.pawan.voicesdk.engine.VoiceEngine] is created
 * lazily by Hilt (see [com.pawan.autovoice.di.AppModule]) the first time a
 * screen actually needs it, so app cold-start time is unaffected by
 * whether the voice pipeline is ever used in a given session at all.
 */
@HiltAndroidApp
class AutoVoiceApplication : Application()
