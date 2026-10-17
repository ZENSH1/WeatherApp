package com.example.weathertracking.weather.view

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import com.example.weathertracking.R
import com.example.weathertracking.data.model.FailureReason
import kotlin.math.roundToInt

/** A whole-degree temperature with a superscript "°", e.g. 21°. */
@Composable
internal fun TemperatureText(
    celsius: Double,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Text(
        text = buildAnnotatedString {
            append(celsius.roundToInt().toString())
            withStyle(SpanStyle(fontSize = style.fontSize * 0.5f, baselineShift = BaselineShift.Superscript)) {
                append("°")
            }
        },
        modifier = modifier,
        color = color,
        style = style,
    )
}

/** User-facing explanation for a [FailureReason]. */
@StringRes
internal fun FailureReason.messageRes(): Int = when (this) {
    FailureReason.Network -> R.string.error_network
    FailureReason.InvalidApiKey -> R.string.error_invalid_api_key
    FailureReason.Unknown -> R.string.error_unknown
}
