package dev.jasz.schedule

import dev.jasz.common.VersionedEntity
import java.time.LocalDate

interface RoomDayScheduleRepository {
    fun get(roomId: String, day: LocalDate): VersionedEntity<RoomDaySchedule>?
    fun save(updatedSchedule: VersionedEntity<RoomDaySchedule>)
    fun allBetween(startDate: LocalDate, endDate: LocalDate): List<RoomDaySchedule>
}
