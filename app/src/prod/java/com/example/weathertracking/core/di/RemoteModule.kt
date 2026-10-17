package com.example.weathertracking.core.di

import com.example.weathertracking.BuildConfig
import com.example.weathertracking.data.remote.RetrofitWeatherRemoteDataSource
import com.example.weathertracking.data.remote.WeatherApi
import com.example.weathertracking.data.remote.WeatherRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** `prod` flavor: weatherapi.com. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteModule {

    @Binds
    abstract fun bindWeatherRemoteDataSource(impl: RetrofitWeatherRemoteDataSource): WeatherRemoteDataSource

    companion object {
        @Provides
        @Singleton
        fun provideWeatherApi(): WeatherApi = WeatherApi.create(apiKey = BuildConfig.WEATHER_API_KEY)
    }
}
