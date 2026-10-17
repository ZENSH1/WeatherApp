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

/**
 * The composition root of the MVC triad: it gets the Model and the Controller from Hilt and
 * hands both to the View.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** M: app-wide singleton, so its state survives this Activity being recreated. */
    @Inject lateinit var model: WeatherModel

    /** C: stateless, a fresh instance per Activity is fine. */
    @Inject lateinit var controller: WeatherController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WeatherTrackingTheme {
                // V: renders the Model and forwards input to the Controller.
                WeatherScreen(model = model, controller = controller)
            }
        }
    }
}
