package com.remmi.app.core.di

import android.content.Context
import com.remmi.app.core.eventBus.EventBus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Core Hilt module providing singleton dependencies across the Remmi application.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideEventBus(): EventBus {
        return EventBus()
    }
}
