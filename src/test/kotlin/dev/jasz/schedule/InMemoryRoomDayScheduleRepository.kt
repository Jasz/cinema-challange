package dev.jasz.schedule

import dev.jasz.common.VersionedEntity
import java.time.LocalDate

class InMemoryRoomDayScheduleRepository : RoomDayScheduleRepository {

    private val schedulesByRoomAndDay = mutableMapOf<Pair<String, LocalDate>, VersionedEntity<RoomDaySchedule>>()

    override fun get(roomId: String, day: LocalDate): VersionedEntity<RoomDaySchedule>? {
        return schedulesByRoomAndDay[Pair(roomId, day)]
    }

    override fun save(updatedSchedule: VersionedEntity<RoomDaySchedule>) {
        val roomId = updatedSchedule.entity.room.id
        val day = updatedSchedule.entity.day
        schedulesByRoomAndDay.compute(Pair(roomId, day)) { _, existingEntity ->
            if (existingEntity == null || existingEntity.version == updatedSchedule.version - 1) {
                updatedSchedule
            } else {
                throw IllegalStateException(
                    "Tried to update schedule for room $roomId $day to version ${updatedSchedule.version}, " +
                            "but was already at version ${existingEntity.version}"
                )
            }
        }
    }

    override fun allBetween(startDate: LocalDate, endDate: LocalDate): List<RoomDaySchedule> {
        val range = startDate.rangeTo(endDate)
        return schedulesByRoomAndDay.filterKeys { (_, day) ->  range.contains(day)}.values.map { it.entity }
    }

    fun clear() {
        schedulesByRoomAndDay.clear()
    }

}