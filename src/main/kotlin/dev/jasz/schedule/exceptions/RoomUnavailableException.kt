package dev.jasz.schedule.exceptions

import dev.jasz.room.Room
import dev.jasz.schedule.Screening

data class RoomUnavailableException(val room: Room, val screening: Screening)
    : Exception("Cannot add $screening because the room ${room.name} is unavailable at that time " +
        "(unavailabilities: ${room.unavailabilities})"
)
