package com.adyghe.keyboard.di

import android.content.Context
import com.adyghe.keyboard.core.haptics.VibrationProvider
import com.adyghe.keyboard.core.input.InputConnectionController
import com.adyghe.keyboard.core.language.LanguagePreferences
import com.adyghe.keyboard.core.language.SharedLanguagePreferences
import com.adyghe.keyboard.core.prefs.KeyboardPreferences
import com.adyghe.keyboard.core.prefs.SharedKeyboardPreferences
import com.adyghe.keyboard.core.layout.LayoutProvider
import com.adyghe.keyboard.core.state.ImeStore
import com.adyghe.keyboard.predictive.SuggestionEngine
import com.adyghe.keyboard.predictive.NoOpSuggestionEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLayoutProvider(): LayoutProvider = LayoutProvider()

    @Provides
    @Singleton
    fun provideLanguagePreferences(
        @ApplicationContext context: Context
    ): LanguagePreferences = SharedLanguagePreferences(context)

    @Provides
    @Singleton
    fun provideImeStore(
        layoutProvider: LayoutProvider,
        inputConnectionController: InputConnectionController,
        suggestionEngine: SuggestionEngine,
        languagePreferences: LanguagePreferences,
        keyboardPreferences: KeyboardPreferences
    ): ImeStore = ImeStore(
        layoutProvider,
        inputConnectionController,
        suggestionEngine,
        languagePreferences,
        keyboardPreferences
    )

    @Provides
    @Singleton
    fun provideSuggestionEngine(): SuggestionEngine = NoOpSuggestionEngine()

    @Provides
    @Singleton
    fun provideKeyboardPreferences(
        @ApplicationContext context: Context
    ): KeyboardPreferences = SharedKeyboardPreferences(context)

    @Provides
    @Singleton
    fun provideVibrationProvider(
        @ApplicationContext context: Context,
        keyboardPreferences: KeyboardPreferences
    ): VibrationProvider = VibrationProvider(context, keyboardPreferences)

    @Provides
    @Singleton
    fun provideInputConnectionController(): InputConnectionController = InputConnectionController()
}
