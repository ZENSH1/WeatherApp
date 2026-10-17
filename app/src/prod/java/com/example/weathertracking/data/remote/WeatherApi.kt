package com.example.weathertracking.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit definition of the weatherapi.com endpoints the app uses. The API key is added by an interceptor. */
interface WeatherApi {

    @GET("search.json")
    suspend fun search(@Query("q") query: String): List<NetworkLocation>

    /** [query] can be a city name or `id:<location id>` from [search]. */
    @GET("current.json")
    suspend fun current(@Query("q") query: String): NetworkCurrentResponse

    companion object {
        const val BASE_URL = "https://api.weatherapi.com/v1/"

        fun create(apiKey: String, baseUrl: String = BASE_URL): WeatherApi {
            val json = Json { ignoreUnknownKeys = true }
            val client = OkHttpClient.Builder()
                .addInterceptor(ApiKeyInterceptor(apiKey))
                .build()
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(WeatherApi::class.java)
        }
    }
}

/** Adds the API key to every request so call sites never deal with it. */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.newBuilder().addQueryParameter("key", apiKey).build()
        return chain.proceed(request.newBuilder().url(url).build())
    }
}

@Serializable
data class NetworkLocation(
    val id: Long,
    val name: String,
    val region: String = "",
    val country: String = "",
)

@Serializable
data class NetworkCurrentResponse(
    val location: NetworkLocationDetails,
    val current: NetworkCurrent,
)

@Serializable
data class NetworkLocationDetails(
    val name: String,
    val region: String = "",
    val country: String = "",
)

@Serializable
data class NetworkCurrent(
    @SerialName("last_updated_epoch") val lastUpdatedEpoch: Long,
    @SerialName("temp_c") val temperatureCelsius: Double,
    @SerialName("feelslike_c") val feelsLikeCelsius: Double,
    val humidity: Int,
    val uv: Double,
    val condition: NetworkCondition,
)

@Serializable
data class NetworkCondition(
    val text: String,
    /** Protocol-relative URL such as `//cdn.weatherapi.com/weather/64x64/day/116.png`. */
    val icon: String,
)
