package dev.jasz.room

import dev.jasz.common.DateTimeRange
import java.time.Duration

data class Room(
    val id: String,
    val name: String,
    val cleanUpTime: Duration,
    val unavailabilities: Set<RoomUnavailability> = emptySet(),
) {

    fun isAvailableDuring(timeRange: DateTimeRange): Boolean {
        return unavailabilities.none { it.isUnavailableDuring(timeRange) }
    }

}

