package com.example.weathertracking.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.weathertracking.data.local.CityWeatherDao
import com.example.weathertracking.data.local.DataStoreSelectedCityStore
import com.example.weathertracking.data.local.SelectedCityStore
import com.example.weathertracking.data.local.WeatherDatabase
import com.example.weathertracking.data.repository.DefaultWeatherRepository
import com.example.weathertracking.data.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindWeatherRepository(impl: DefaultWeatherRepository): WeatherRepository

    @Binds
    abstract fun bindSelectedCityStore(impl: DataStoreSelectedCityStore): SelectedCityStore

    companion object {

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): WeatherDatabase =
            Room.databaseBuilder(context, WeatherDatabase::class.java, WeatherDatabase.NAME)
                // Only a cache of downloaded weather.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

        @Provides
        fun provideCityWeatherDao(database: WeatherDatabase): CityWeatherDao = database.cityWeatherDao()

        @Provides
        @Singleton
        fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = { context.preferencesDataStoreFile("weather_preferences") },
            )
    }
}
