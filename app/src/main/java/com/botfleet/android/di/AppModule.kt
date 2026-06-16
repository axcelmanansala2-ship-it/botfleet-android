package com.botfleet.android.di

import android.content.Context
import com.botfleet.android.data.api.ApiClient
import com.botfleet.android.data.preferences.SessionPreferences
import com.botfleet.android.data.repository.AuthRepository
import com.botfleet.android.data.repository.BotRepository
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
    fun provideSessionPreferences(@ApplicationContext context: Context): SessionPreferences =
        SessionPreferences(context)

    @Provides
    @Singleton
    fun provideApiClient(sessionPreferences: SessionPreferences): ApiClient =
        ApiClient(sessionPreferences)
}
