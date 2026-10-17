package com.example.weathertracking.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weathertracking.data.model.CityWeather

@Entity(tableName = "city_weather")
data class CityWeatherEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val region: String,
    val country: String,
    @ColumnInfo(name = "temperature_celsius") val temperatureCelsius: Double,
    @ColumnInfo(name = "feels_like_celsius") val feelsLikeCelsius: Double,
    val condition: String,
    @ColumnInfo(name = "icon_url") val iconUrl: String,
    @ColumnInfo(name = "humidity_percent") val humidityPercent: Int,
    @ColumnInfo(name = "uv_index") val uvIndex: Double,
    @ColumnInfo(name = "last_updated_epoch_seconds") val lastUpdatedEpochSeconds: Long,
)

fun CityWeatherEntity.toModel() = CityWeather(
    id = id,
    name = name,
    region = region,
    country = country,
    temperatureCelsius = temperatureCelsius,
    feelsLikeCelsius = feelsLikeCelsius,
    condition = condition,
    iconUrl = iconUrl,
    humidityPercent = humidityPercent,
    uvIndex = uvIndex,
    lastUpdatedEpochSeconds = lastUpdatedEpochSeconds,
)

fun CityWeather.toEntity() = CityWeatherEntity(
    id = id,
    name = name,
    region = region,
    country = country,
    temperatureCelsius = temperatureCelsius,
    feelsLikeCelsius = feelsLikeCelsius,
    condition = condition,
    iconUrl = iconUrl,
    humidityPercent = humidityPercent,
    uvIndex = uvIndex,
    lastUpdatedEpochSeconds = lastUpdatedEpochSeconds,
)
