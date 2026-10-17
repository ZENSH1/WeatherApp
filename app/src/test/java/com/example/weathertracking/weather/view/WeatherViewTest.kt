package com.example.weathertracking.weather.view

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weathertracking.R
import com.example.weathertracking.data.model.CityWeather
import com.example.weathertracking.data.model.FailureReason
import com.example.weathertracking.testing.cityWeather
import com.example.weathertracking.ui.theme.WeatherTrackingTheme
import com.example.weathertracking.weather.model.SearchState
import com.example.weathertracking.weather.model.SelectedCityState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The View is a function of the Model's state, so it can be tested by feeding it states and
 * checking which callbacks (Controller methods, in the app) it fires. Runs on the JVM via Robolectric.
 */
@RunWith(AndroidJUnit4::class)
class WeatherViewTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun string(@StringRes id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)

    private fun setView(
        selectedCity: SelectedCityState = SelectedCityState.None,
        search: SearchState = SearchState.Idle,
        onSearch: (String) -> Unit = {},
        onCityClick: (CityWeather) -> Unit = {},
        onRetrySearch: () -> Unit = {},
    ) {
        composeRule.setContent {
            WeatherTrackingTheme(dynamicColor = false) {
                WeatherView(
                    selectedCity = selectedCity,
                    search = search,
                    onSearch = onSearch,
                    onClearSearch = {},
                    onCityClick = onCityClick,
                    onRetrySearch = onRetrySearch,
                    onRefresh = {},
                )
            }
        }
    }

    @Test
    fun noSelectedCity_showsTheEmptyState() {
        setView(selectedCity = SelectedCityState.None)

        composeRule.onNodeWithText(string(R.string.no_city_selected)).assertIsDisplayed()
    }

    @Test
    fun selectedCity_showsItsWeather() {
        setView(selectedCity = SelectedCityState.Selected(cityWeather(1, name = "Lahore")))

        composeRule.onNodeWithText("Lahore").assertIsDisplayed()
        // The details are below the fold on a small screen: the content scrolls.
        composeRule.onNodeWithText(string(R.string.humidity)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun submittingTheSearchField_passesTheTypedTextOn() {
        var submitted: String? = null
        setView(onSearch = { submitted = it })

        composeRule.onNode(hasSetTextAction()).performTextInput("Paris")
        composeRule.onNodeWithContentDescription(string(R.string.search_action)).performClick()

        assertEquals("Paris", submitted)
    }

    @Test
    fun clickingAResult_passesThatCityOn() {
        val city = cityWeather(2, name = "Tokyo")
        var clicked: CityWeather? = null
        setView(search = SearchState.Results("To", listOf(city)), onCityClick = { clicked = it })

        composeRule.onNodeWithText("Tokyo").performClick()

        assertEquals(city, clicked)
    }

    @Test
    fun failedSearch_showsTheReasonAndCanRetry() {
        var retried = false
        setView(search = SearchState.Failed("offline", FailureReason.Network), onRetrySearch = { retried = true })

        composeRule.onNodeWithText(string(R.string.error_network)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.retry)).performClick()

        assertTrue(retried)
    }
}
