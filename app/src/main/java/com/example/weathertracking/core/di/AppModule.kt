package com.example.weathertracking.core.di

import com.example.weathertracking.core.TimeSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * A scope that lives as long as the app process. The MVC Model runs its work here so that
 * a search or refresh is not lost when the Activity is recreated (rotation, dark mode, …).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Runs on the main thread so Model state is only ever mutated from one thread.
     * That is safe because everything the Model calls (Room, DataStore, Retrofit) is main-safe.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Provides
    fun provideTimeSource(): TimeSource = TimeSource { System.currentTimeMillis() / 1_000 }
}
