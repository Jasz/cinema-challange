package dev.jasz.schedule

import dev.jasz.room.Room
import java.time.LocalDate

data class DailySchedule(val day: LocalDate, val roomSchedules: Map<Room, RoomDaySchedule>) {

    constructor(day: LocalDate, roomSchedules: Collection<RoomDaySchedule>)
            : this(day, roomSchedules.associateBy { it.room })

}
