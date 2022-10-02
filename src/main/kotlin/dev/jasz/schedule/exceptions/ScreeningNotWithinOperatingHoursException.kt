package dev.jasz.schedule.exceptions

import dev.jasz.common.TimeRange
import dev.jasz.schedule.Screening

data class ScreeningNotWithinOperatingHoursException(val screening: Screening, val operatingHours: TimeRange)
    : Exception("The new screening $screening is not within the operating hours $operatingHours")