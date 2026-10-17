package com.example.weathertracking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.weathertracking.ui.theme.WeatherTrackingTheme
import com.example.weathertracking.weather.controller.WeatherController
import com.example.weathertracking.weather.model.WeatherModel
import com.example.weathertracking.weather.view.WeatherScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Wires the MVC triad: Model and Controller from Hilt, handed to the View. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var model: WeatherModel

    @Inject lateinit var controller: WeatherController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WeatherTrackingTheme {
                WeatherScreen(model = model, controller = controller)
            }
        }
    }
}
