package com.pawan.autovoice.di

import android.content.Context
import com.pawan.autovoice.BuildConfig
import com.pawan.autovoice.data.repository.VoiceRepositoryImpl
import com.pawan.autovoice.domain.repository.VoiceRepository
import com.pawan.voicesdk.audio.VoiceAudioFocusManager
import com.pawan.voicesdk.engine.VoiceEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ## AppModule
 *
 * The **only** place in the app that touches [VoiceEngine.create] — the
 * SDK's public factory function. Everything downstream (repository, use
 * case, ViewModel) receives a [VoiceEngine] purely through Hilt's
 * constructor injection and never calls the factory itself.
 *
 * This directly demonstrates the "SDK Initialization" interview question
 * (Q10 — "how do you design SDK init so it doesn't block Application.onCreate()").
 * We do NOT eagerly create the engine in [com.pawan.autovoice.AutoVoiceApplication.onCreate];
 * Hilt's `@Singleton` + `@Provides` combination creates it **lazily**, the
 * first time something actually injects a [VoiceEngine] — which in this app
 * is the moment the Dashboard screen is first composed, not app startup.
 */
@Module
@InstallIn(SingletonComponent::class)
public object AppModule {

    @Provides
    @Singleton
    public fun provideVoiceEngine(): VoiceEngine =
        VoiceEngine.create(enableVerboseLogging = BuildConfig.DEBUG)

    @Provides
    @Singleton
    public fun provideVoiceAudioFocusManager(
        @ApplicationContext context: Context,
    ): VoiceAudioFocusManager = VoiceAudioFocusManager(context)
}

/**
 * Separate `@Binds` module for interface-to-implementation bindings.
 * Kept apart from [AppModule] purely as a style convention some teams use
 * (`@Provides` for things you construct, `@Binds` for things you just
 * point at an implementation) — either is fine, but consistency matters
 * more than which one you pick, and this is worth mentioning if a
 * reviewer asks "why two modules?"
 */
@Module
@InstallIn(SingletonComponent::class)
public interface RepositoryBindModule {

    @Binds
    public fun bindVoiceRepository(impl: VoiceRepositoryImpl): VoiceRepository
}
