package com.harichselvamc.seetime.util

import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class MapPoint(
    val x: Float,
    val y: Float
)

data class TimezoneBand(
    val offsetHours: Int,
    val centerLonDeg: Float,
    val startLonDeg: Float,
    val endLonDeg: Float,
    val label: String
)

object WorldMapEngine {

    /**
     * Converts (lat, lon) to 2D canvas pixel coordinates (x, y).
     */
    fun projectEquirectangular(
        latitude: Double,
        longitude: Double,
        canvasWidth: Float,
        canvasHeight: Float
    ): MapPoint {
        val clampedLat = latitude.coerceIn(-90.0, 90.0)
        val clampedLon = longitude.coerceIn(-180.0, 180.0)

        val x = ((clampedLon + 180.0) / 360.0 * canvasWidth).toFloat()
        val y = ((90.0 - clampedLat) / 180.0 * canvasHeight).toFloat()

        return MapPoint(x, y)
    }

    /**
     * Calculates the sequence of (lat, lon) coordinates forming the real-time Solar Day/Night Terminator wave.
     */
    fun calculateTerminatorWave(subsolarPoint: SubsolarPoint, stepDegrees: Int = 4): List<Pair<Double, Double>> {
        val points = mutableListOf<Pair<Double, Double>>()
        val subLatRad = Math.toRadians(subsolarPoint.latitude.coerceIn(-23.44, 23.44))
        val subLonDeg = subsolarPoint.longitude

        val tanSubLat = tan(subLatRad)

        for (lon in -180..180 step stepDegrees) {
            val dLonRad = Math.toRadians(lon - subLonDeg)
            val termLatDeg = if (kotlin.math.abs(tanSubLat) < 0.0001) {
                0.0
            } else {
                val tanTermLat = -cos(dLonRad) / tanSubLat
                Math.toDegrees(atan(tanTermLat)).coerceIn(-89.9, 89.9)
            }
            points.add(Pair(termLatDeg, lon.toDouble()))
        }

        return points
    }

    /**
     * Generates standard 24 15-degree UTC offset iso-contour timezone bands.
     */
    fun generateTimezoneBands(): List<TimezoneBand> {
        val bands = mutableListOf<TimezoneBand>()
        for (offset in -12..12) {
            val center = offset * 15f
            val start = center - 7.5f
            val end = center + 7.5f
            val sign = if (offset >= 0) "+$offset" else "$offset"
            bands.add(
                TimezoneBand(
                    offsetHours = offset,
                    centerLonDeg = center,
                    startLonDeg = start,
                    endLonDeg = end,
                    label = "UTC$sign"
                )
            )
        }
        return bands
    }
}
