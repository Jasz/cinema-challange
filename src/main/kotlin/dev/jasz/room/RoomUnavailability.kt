package dev.jasz.room

import dev.jasz.common.DateTimeRange
import dev.jasz.common.TimeRange
import dev.jasz.common.atDate
import dev.jasz.common.overlaps
import java.time.DayOfWeek
import java.time.temporal.Temporal
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters

data class RoomUnavailability(
    val dayOfWeek: DayOfWeekOrAny,
    val hours: TimeRange,
) {

    fun isUnavailableDuring(timeRange: DateTimeRange): Boolean {
        val unavailabilityDate = timeRange.start.toLocalDate().with(dayOfWeek)
        val unavailabilityDateTimeRange = hours.atDate(unavailabilityDate)
        return timeRange.overlaps(unavailabilityDateTimeRange)
    }

}

sealed interface DayOfWeekOrAny : TemporalAdjuster {

    object Any : DayOfWeekOrAny {
        override fun adjustInto(temporal: Temporal) = temporal

        override fun toString() = "AnyDayOfWeek"
    }

    data class WeekDay(val dayOfWeek: DayOfWeek) : DayOfWeekOrAny {
        override fun adjustInto(temporal: Temporal): Temporal = temporal.with(TemporalAdjusters.nextOrSame(dayOfWeek))
    }

    companion object {
        fun of(dayOfWeek: DayOfWeek) = WeekDay(dayOfWeek)
    }

}