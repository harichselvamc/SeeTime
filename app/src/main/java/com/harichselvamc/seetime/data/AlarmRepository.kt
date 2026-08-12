package com.harichselvamc.seetime.data

import android.content.Context
import android.content.SharedPreferences
import com.harichselvamc.seetime.ui.AlarmUi
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

class AlarmRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("seetime_alarms_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var INSTANCE: AlarmRepository? = null

        fun getInstance(context: Context): AlarmRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AlarmRepository(context.applicationContext).also { INSTANCE = it }
            }
    }

    fun getAlarms(): List<AlarmUi> {
        val jsonStr = prefs.getString("alarms_json", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<AlarmUi>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val idStr = obj.getString("id")
                val title = obj.getString("title")
                val targetTime = obj.getString("targetTime")
                val zone = obj.getString("zone")
                val scheduledAt = obj.getLong("scheduledAt")
                val firesAt = obj.getLong("firesAt")
                val daysArr = obj.getJSONArray("repeatDays")
                val repeatDays = mutableListOf<Int>()
                for (j in 0 until daysArr.length()) {
                    repeatDays.add(daysArr.getInt(j))
                }
                val isEnabled = obj.getBoolean("isEnabled")

                list.add(
                    AlarmUi(
                        id = UUID.fromString(idStr),
                        title = title,
                        targetTime = targetTime,
                        zone = zone,
                        scheduledAt = scheduledAt,
                        firesAt = firesAt,
                        repeatDays = repeatDays,
                        isEnabled = isEnabled
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveAlarms(alarms: List<AlarmUi>) {
        val array = JSONArray()
        for (alarm in alarms) {
            val obj = JSONObject()
            obj.put("id", alarm.id.toString())
            obj.put("title", alarm.title)
            obj.put("targetTime", alarm.targetTime)
            obj.put("zone", alarm.zone)
            obj.put("scheduledAt", alarm.scheduledAt)
            obj.put("firesAt", alarm.firesAt)

            val daysArr = JSONArray()
            alarm.repeatDays.forEach { daysArr.put(it) }
            obj.put("repeatDays", daysArr)

            obj.put("isEnabled", alarm.isEnabled)
            array.put(obj)
        }
        prefs.edit().putString("alarms_json", array.toString()).apply()
    }

    fun addAlarm(alarm: AlarmUi) {
        val current = getAlarms().toMutableList()
        // Deduplicate by title + zone + targetTime
        current.removeAll { it.title == alarm.title && it.zone == alarm.zone && it.targetTime == alarm.targetTime }
        current.add(0, alarm)
        saveAlarms(current)
    }

    fun updateAlarm(alarm: AlarmUi) {
        val current = getAlarms().map { if (it.id == alarm.id) alarm else it }
        saveAlarms(current)
    }

    fun deleteAlarm(id: UUID) {
        val current = getAlarms().filter { it.id != id }
        saveAlarms(current)
    }
}
