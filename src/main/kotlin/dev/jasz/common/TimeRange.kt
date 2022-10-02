package dev.jasz.common

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

typealias TimeRange = ClosedRange<LocalTime>
typealias DateTimeRange = ClosedRange<LocalDateTime>

fun TimeRange.atDate(date: LocalDate): DateTimeRange {
    return start.atDate(date).rangeTo(endInclusive.atDate(date))
}
