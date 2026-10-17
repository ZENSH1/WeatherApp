package com.example.weathertracking.testing

import com.example.weathertracking.data.model.CityWeather

fun cityWeather(
    id: Long,
    name: String = "City $id",
    temperatureCelsius: Double = 20.0,
    lastUpdatedEpochSeconds: Long = 0,
) = CityWeather(
    id = id,
    name = name,
    region = "Region",
    country = "Country",
    temperatureCelsius = temperatureCelsius,
    feelsLikeCelsius = temperatureCelsius,
    condition = "Sunny",
    iconUrl = "",
    humidityPercent = 50,
    uvIndex = 5.0,
    lastUpdatedEpochSeconds = lastUpdatedEpochSeconds,
)
