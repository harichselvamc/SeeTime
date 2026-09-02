package com.harichselvamc.seetime.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.ChronoUnit

enum class HolidayCategory(val displayName: String) {
    NATIONAL_PUBLIC_HOLIDAY("National Public Holiday"),
    BANK_HOLIDAY("Bank Holiday"),
    CULTURAL_FESTIVAL("Cultural Festival"),
    REGIONAL_OBSERVANCE("Observance")
}

data class PublicHoliday(
    val name: String,
    val localName: String,
    val date: LocalDate,
    val countryCode: String,
    val countryName: String,
    val category: HolidayCategory,
    val isOffWork: Boolean,
    val description: String
)

data class SupportedCountry(
    val code: String,
    val name: String,
    val emoji: String,
    val defaultZoneId: String
)

object HolidayCalculationEngine {

    val SUPPORTED_COUNTRIES = listOf(
        SupportedCountry("US", "United States", "🇺🇸", "America/New_York"),
        SupportedCountry("GB", "United Kingdom", "🇬🇧", "Europe/London"),
        SupportedCountry("JP", "Japan", "🇯🇵", "Asia/Tokyo"),
        SupportedCountry("DE", "Germany", "🇩🇪", "Europe/Berlin"),
        SupportedCountry("FR", "France", "🇫🇷", "Europe/Paris"),
        SupportedCountry("IN", "India", "🇮🇳", "Asia/Kolkata"),
        SupportedCountry("AU", "Australia", "🇦🇺", "Australia/Sydney"),
        SupportedCountry("SG", "Singapore", "🇸🇬", "Asia/Singapore"),
        SupportedCountry("CA", "Canada", "🇨🇦", "America/Toronto"),
        SupportedCountry("AE", "United Arab Emirates", "🇦🇪", "Asia/Dubai")
    )

    /**
     * Calculates Easter Sunday for a given [year] using the Anonymous Gregorian algorithm.
     */
    fun calculateEasterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }

    /**
     * Helper to get N-th [dayOfWeek] in a given [month] (e.g. 4th Thursday of Nov).
     */
    fun nthDayOfWeekInMonth(year: Int, month: Int, dayOfWeek: DayOfWeek, n: Int): LocalDate {
        val firstOfMonth = LocalDate.of(year, month, 1)
        val firstTargetDay = firstOfMonth.with(TemporalAdjusters.firstInMonth(dayOfWeek))
        return firstTargetDay.plusWeeks((n - 1).toLong())
    }

    /**
     * Helper to get last [dayOfWeek] in a given [month] (e.g. last Monday of May).
     */
    fun lastDayOfWeekInMonth(year: Int, month: Int, dayOfWeek: DayOfWeek): LocalDate {
        val firstOfMonth = LocalDate.of(year, month, 1)
        return firstOfMonth.with(TemporalAdjusters.lastInMonth(dayOfWeek))
    }

    /**
     * Calculates all public holidays for [countryCode] in [year].
     */
    fun getHolidaysForCountry(countryCode: String, year: Int): List<PublicHoliday> {
        val holidays = mutableListOf<PublicHoliday>()
        val easter = calculateEasterSunday(year)
        val goodFriday = easter.minusDays(2)
        val easterMonday = easter.plusDays(1)
        val ascensionDay = easter.plusDays(39)
        val whitMonday = easter.plusDays(50)

        when (countryCode.uppercase()) {
            "US" -> {
                val country = "United States"
                holidays.add(PublicHoliday("New Year's Day", "New Year's Day", LocalDate.of(year, 1, 1), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Celebration of the first day of the year."))
                holidays.add(PublicHoliday("Martin Luther King Jr. Day", "MLK Day", nthDayOfWeekInMonth(year, 1, DayOfWeek.MONDAY, 3), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Honors civil rights leader Martin Luther King Jr."))
                holidays.add(PublicHoliday("Presidents' Day", "Washington's Birthday", nthDayOfWeekInMonth(year, 2, DayOfWeek.MONDAY, 3), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Honors all past American presidents."))
                holidays.add(PublicHoliday("Memorial Day", "Memorial Day", lastDayOfWeekInMonth(year, 5, DayOfWeek.MONDAY), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Remembrance of US military personnel who died in service."))
                holidays.add(PublicHoliday("Juneteenth", "National Independence Day", LocalDate.of(year, 6, 19), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Commemorates the end of slavery in the United States."))
                holidays.add(PublicHoliday("Independence Day", "4th of July", LocalDate.of(year, 7, 4), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Celebrates the Declaration of Independence in 1776."))
                holidays.add(PublicHoliday("Labor Day", "Labor Day", nthDayOfWeekInMonth(year, 9, DayOfWeek.MONDAY, 1), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Honors the American labor movement and workers."))
                holidays.add(PublicHoliday("Columbus / Indigenous Peoples' Day", "Indigenous Peoples' Day", nthDayOfWeekInMonth(year, 10, DayOfWeek.MONDAY, 2), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Federal observance."))
                holidays.add(PublicHoliday("Veterans Day", "Veterans Day", LocalDate.of(year, 11, 11), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Honors military veterans who served in the US Armed Forces."))
                holidays.add(PublicHoliday("Thanksgiving Day", "Thanksgiving", nthDayOfWeekInMonth(year, 11, DayOfWeek.THURSDAY, 4), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Traditional autumn harvest holiday and family gathering."))
                holidays.add(PublicHoliday("Christmas Day", "Christmas", LocalDate.of(year, 12, 25), "US", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Christian holiday celebrating the birth of Jesus."))
            }
            "GB" -> {
                val country = "United Kingdom"
                holidays.add(PublicHoliday("New Year's Day", "New Year's Day", LocalDate.of(year, 1, 1), "GB", country, HolidayCategory.BANK_HOLIDAY, true, "Official UK bank holiday."))
                holidays.add(PublicHoliday("Good Friday", "Good Friday", goodFriday, "GB", country, HolidayCategory.BANK_HOLIDAY, true, "Christian holy day commemorating the crucifixion."))
                holidays.add(PublicHoliday("Easter Monday", "Easter Monday", easterMonday, "GB", country, HolidayCategory.BANK_HOLIDAY, true, "Official UK bank holiday following Easter Sunday."))
                holidays.add(PublicHoliday("Early May Bank Holiday", "May Day", nthDayOfWeekInMonth(year, 5, DayOfWeek.MONDAY, 1), "GB", country, HolidayCategory.BANK_HOLIDAY, true, "Spring bank holiday celebrating workers and spring."))
                holidays.add(PublicHoliday("Spring Bank Holiday", "Late May Bank Holiday", lastDayOfWeekInMonth(year, 5, DayOfWeek.MONDAY), "GB", country, HolidayCategory.BANK_HOLIDAY, true, "Official late spring bank holiday."))
                holidays.add(PublicHoliday("Summer Bank Holiday", "August Bank Holiday", lastDayOfWeekInMonth(year, 8, DayOfWeek.MONDAY), "GB", country, HolidayCategory.BANK_HOLIDAY, true, "Late summer bank holiday in England and Wales."))
                holidays.add(PublicHoliday("Christmas Day", "Christmas Day", LocalDate.of(year, 12, 25), "GB", country, HolidayCategory.BANK_HOLIDAY, true, "National bank holiday."))
                holidays.add(PublicHoliday("Boxing Day", "Boxing Day", LocalDate.of(year, 12, 26), "GB", country, HolidayCategory.BANK_HOLIDAY, true, "Day following Christmas, traditionally giving to charity."))
            }
            "JP" -> {
                val country = "Japan"
                holidays.add(PublicHoliday("New Year's Day", "元日 (Ganjitsu)", LocalDate.of(year, 1, 1), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Celebration of the New Year."))
                holidays.add(PublicHoliday("Coming of Age Day", "成人の日 (Seijin no Hi)", nthDayOfWeekInMonth(year, 1, DayOfWeek.MONDAY, 2), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Congratulates young adults reaching age of majority."))
                holidays.add(PublicHoliday("National Foundation Day", "建国記念の日 (Kenkoku Kinen no Hi)", LocalDate.of(year, 2, 11), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Celebrates the founding of the nation."))
                holidays.add(PublicHoliday("Emperor's Birthday", "天皇誕生日 (Tennō Tanjōbi)", LocalDate.of(year, 2, 23), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Birthday of the reigning Emperor."))
                holidays.add(PublicHoliday("Vernal Equinox Day", "春分の日 (Shunbun no Hi)", LocalDate.of(year, 3, 20), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Celebrates nature and living things."))
                holidays.add(PublicHoliday("Shōwa Day", "昭和の日 (Shōwa no Hi)", LocalDate.of(year, 4, 29), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Start of Golden Week. Honors Emperor Showa."))
                holidays.add(PublicHoliday("Constitution Memorial Day", "憲法記念日 (Kenpō Kinenbi)", LocalDate.of(year, 5, 3), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Golden Week. Commemorates 1947 Constitution."))
                holidays.add(PublicHoliday("Greenery Day", "みどりの日 (Midori no Hi)", LocalDate.of(year, 5, 4), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Golden Week. Appreciation of nature and blessing."))
                holidays.add(PublicHoliday("Children's Day", "こどもの日 (Kodomo no Hi)", LocalDate.of(year, 5, 5), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Golden Week. Celebrates children's happiness."))
                holidays.add(PublicHoliday("Marine Day", "海の日 (Umi no Hi)", nthDayOfWeekInMonth(year, 7, DayOfWeek.MONDAY, 3), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Gratitude for the oceans and maritime bounty."))
                holidays.add(PublicHoliday("Mountain Day", "山の日 (Yama no Hi)", LocalDate.of(year, 8, 11), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Appreciation of Japan's majestic mountains."))
                holidays.add(PublicHoliday("Respect for the Aged Day", "敬老の日 (Keirō no Hi)", nthDayOfWeekInMonth(year, 9, DayOfWeek.MONDAY, 3), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Honors elderly citizens and longevity."))
                holidays.add(PublicHoliday("Autumnal Equinox Day", "秋分の日 (Shūbun no Hi)", LocalDate.of(year, 9, 22), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Remembrance of ancestors."))
                holidays.add(PublicHoliday("Sports Day", "スポーツの日 (Supōtsu no Hi)", nthDayOfWeekInMonth(year, 10, DayOfWeek.MONDAY, 2), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Promoting health and athletic spirit."))
                holidays.add(PublicHoliday("Culture Day", "文化の日 (Bunka no Hi)", LocalDate.of(year, 11, 3), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Promotes culture, arts, and academic endeavors."))
                holidays.add(PublicHoliday("Labor Thanksgiving Day", "勤労感謝の日 (Kinrō Kansha no Hi)", LocalDate.of(year, 11, 23), "JP", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Praising labor and celebrating production."))
            }
            "DE" -> {
                val country = "Germany"
                holidays.add(PublicHoliday("New Year's Day", "Neujahr", LocalDate.of(year, 1, 1), "DE", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "New Year celebration."))
                holidays.add(PublicHoliday("Good Friday", "Karfreitag", goodFriday, "DE", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Statutory public holiday."))
                holidays.add(PublicHoliday("Easter Monday", "Ostermontag", easterMonday, "DE", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Statutory public holiday."))
                holidays.add(PublicHoliday("Labour Day", "Tag der Arbeit", LocalDate.of(year, 5, 1), "DE", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "International Workers' Day."))
                holidays.add(PublicHoliday("Ascension Day", "Christi Himmelfahrt", ascensionDay, "DE", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "National public holiday."))
                holidays.add(PublicHoliday("Whit Monday", "Pfingstmontag", whitMonday, "DE", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "National public holiday."))
                holidays.add(PublicHoliday("German Unity Day", "Tag der Deutschen Einheit", LocalDate.of(year, 10, 3), "DE", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Commemorates the anniversary of German reunification in 1990."))
                holidays.add(PublicHoliday("Christmas Day", "1. Weihnachtstag", LocalDate.of(year, 12, 25), "DE", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Christmas celebration."))
                holidays.add(PublicHoliday("Boxing Day", "2. Weihnachtstag", LocalDate.of(year, 12, 26), "DE", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Second day of Christmas."))
            }
            "IN" -> {
                val country = "India"
                holidays.add(PublicHoliday("Republic Day", "गणतंत्र दिवस (Gantantra Diwas)", LocalDate.of(year, 1, 26), "IN", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Honors the Constitution of India coming into effect in 1950."))
                holidays.add(PublicHoliday("Independence Day", "स्वतंत्रता दिवस (Swatantrata Diwas)", LocalDate.of(year, 8, 15), "IN", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Celebrates Indian independence from British rule in 1947."))
                holidays.add(PublicHoliday("Gandhi Jayanti", "गांधी जयंती", LocalDate.of(year, 10, 2), "IN", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Celebrates the birthday of Mahatma Gandhi, Father of the Nation."))
                holidays.add(PublicHoliday("Diwali (Deepavali)", "दिवाली / दीपोत्सव", LocalDate.of(year, 11, 8), "IN", country, HolidayCategory.CULTURAL_FESTIVAL, true, "Festival of Lights celebrating victory of light over darkness."))
                holidays.add(PublicHoliday("Holi Festival", "होली", LocalDate.of(year, 3, 25), "IN", country, HolidayCategory.CULTURAL_FESTIVAL, true, "Festival of Colors celebrating spring and love."))
                holidays.add(PublicHoliday("Christmas Day", "बड़ा दिन (Christmas)", LocalDate.of(year, 12, 25), "IN", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "National public holiday."))
            }
            "AU" -> {
                val country = "Australia"
                holidays.add(PublicHoliday("New Year's Day", "New Year's Day", LocalDate.of(year, 1, 1), "AU", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Official public holiday."))
                holidays.add(PublicHoliday("Australia Day", "Australia Day", LocalDate.of(year, 1, 26), "AU", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "National day of Australia."))
                holidays.add(PublicHoliday("Good Friday", "Good Friday", goodFriday, "AU", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Public holiday."))
                holidays.add(PublicHoliday("Easter Monday", "Easter Monday", easterMonday, "AU", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Public holiday."))
                holidays.add(PublicHoliday("Anzac Day", "ANZAC Day", LocalDate.of(year, 4, 25), "AU", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Commemorates Australians and New Zealanders who served in military operations."))
                holidays.add(PublicHoliday("King's Birthday", "King's Birthday", nthDayOfWeekInMonth(year, 6, DayOfWeek.MONDAY, 2), "AU", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Celebrates official monarch birthday."))
                holidays.add(PublicHoliday("Christmas Day", "Christmas Day", LocalDate.of(year, 12, 25), "AU", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Public holiday."))
                holidays.add(PublicHoliday("Boxing Day", "Boxing Day", LocalDate.of(year, 12, 26), "AU", country, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Public holiday."))
            }
            else -> {
                // Generic global fallback for other countries
                val c = SUPPORTED_COUNTRIES.find { it.code.equals(countryCode, ignoreCase = true) }?.name ?: countryCode
                holidays.add(PublicHoliday("New Year's Day", "New Year's Day", LocalDate.of(year, 1, 1), countryCode, c, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Global New Year."))
                holidays.add(PublicHoliday("International Workers' Day", "Labour Day", LocalDate.of(year, 5, 1), countryCode, c, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Workers holiday."))
                holidays.add(PublicHoliday("Christmas Day", "Christmas Day", LocalDate.of(year, 12, 25), countryCode, c, HolidayCategory.NATIONAL_PUBLIC_HOLIDAY, true, "Christmas holiday."))
            }
        }

        return holidays.sortedBy { it.date }
    }

    /**
     * Cross-Timezone Meeting Conflict Detector:
     * Checks if [meetingDate] is a statutory public holiday in any of the participant's [countryCodes].
     */
    fun checkMeetingHolidayConflicts(
        meetingDate: LocalDate,
        countryCodes: List<String>
    ): List<PublicHoliday> {
        val conflicts = mutableListOf<PublicHoliday>()
        val year = meetingDate.year

        countryCodes.distinct().forEach { code ->
            val holidays = getHolidaysForCountry(code, year)
            val matching = holidays.filter { it.date == meetingDate && it.isOffWork }
            conflicts.addAll(matching)
        }

        return conflicts
    }

    /**
     * Finds the next upcoming holiday from today for a given country.
     */
    fun getNextUpcomingHoliday(countryCode: String, today: LocalDate = LocalDate.now()): PublicHoliday? {
        val year = today.year
        val thisYearHolidays = getHolidaysForCountry(countryCode, year).filter { !it.date.isBefore(today) }
        if (thisYearHolidays.isNotEmpty()) {
            return thisYearHolidays.first()
        }
        val nextYearHolidays = getHolidaysForCountry(countryCode, year + 1)
        return nextYearHolidays.firstOrNull()
    }
}
