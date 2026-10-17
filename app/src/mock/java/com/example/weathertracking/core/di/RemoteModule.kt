package com.example.weathertracking.core.di

import com.example.weathertracking.data.remote.FakeWeatherRemoteDataSource
import com.example.weathertracking.data.remote.WeatherRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** `mock` flavor: canned data. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteModule {

    @Binds
    abstract fun bindWeatherRemoteDataSource(impl: FakeWeatherRemoteDataSource): WeatherRemoteDataSource
}
