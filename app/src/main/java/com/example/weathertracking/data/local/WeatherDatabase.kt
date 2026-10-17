package com.example.weathertracking.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CityWeatherEntity::class], version = 1, exportSchema = true)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun cityWeatherDao(): CityWeatherDao

    companion object {
        const val NAME = "weather.db"
    }
}
