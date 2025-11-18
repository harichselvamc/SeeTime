package com.harichselvamc.seetime.data.remote

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query

data class CurrentTimeResponse(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val seconds: Int,
    val milliSeconds: Int,
    val dateTime: String,
    val date: String,
    val time: String,
    val timeZone: String,
    val dayOfWeek: String,
    val dstActive: Boolean
)

data class ConvertTimezoneRequest(
    val fromTimeZone: String,
    val dateTime: String,
    val toTimeZone: String
)

data class ConvertTimezoneResponse(
    val fromTimeZone: String,
    val dateTimeFrom: String,
    val toTimeZone: String,
    val dateTimeTo: String
)

interface TimeApiService {
    @GET("/api/timezone/availabletimezones")
    suspend fun getAvailableTimeZones(): List<String>

    @GET("/api/time/current/zone")
    suspend fun getCurrentTime(
        @Query("timeZone") tz: String
    ): CurrentTimeResponse

    @POST("/api/conversion/converttimezone")
    suspend fun convertTimezone(
        @Body body: ConvertTimezoneRequest
    ): ConvertTimezoneResponse
}
