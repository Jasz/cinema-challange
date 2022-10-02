package dev.jasz.schedule

class Schedule private constructor(
    val schedulesByDay: List<DailySchedule>,
) {

    constructor(dailySchedules: Collection<RoomDaySchedule>) : this(
        dailySchedules
            .groupBy { it.day }
            .map { (day, roomSchedules) -> DailySchedule(day, roomSchedules) }
            .sortedBy { it.day }
    )

}