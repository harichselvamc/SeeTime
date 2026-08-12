package com.harichselvamc.seetime.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import java.util.TimeZone

object CalendarHelper {

    fun createCalendarEventIntent(
        title: String,
        description: String,
        startMillis: Long,
        endMillis: Long,
        timeZone: String
    ): Intent {
        return Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            .putExtra(CalendarContract.Events.TITLE, title)
            .putExtra(CalendarContract.Events.DESCRIPTION, description)
            .putExtra(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
    }
}
