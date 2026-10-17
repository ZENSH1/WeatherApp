package com.example.weathertracking.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CityWeatherDao {

    /** Emits the stored weather for [id] (or `null`) and again every time that row changes. */
    @Query("SELECT * FROM city_weather WHERE id = :id")
    fun observe(id: Long): Flow<CityWeatherEntity?>

    @Upsert
    suspend fun upsert(city: CityWeatherEntity)
}
