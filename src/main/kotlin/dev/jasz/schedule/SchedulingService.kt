package dev.jasz.schedule

import dev.jasz.common.TimeRange
import dev.jasz.common.VersionedEntity
import dev.jasz.movie.MovieRepository
import dev.jasz.room.RoomRepository
import dev.jasz.movie.exceptions.MovieNotFoundException
import dev.jasz.room.exceptions.RoomNotFoundException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters.previousOrSame
import java.time.LocalTime.parse as time

class SchedulingService(
    private val openingHours: TimeRange = time("08:00").rangeTo(time("22:00")),
    private val movieRepository: MovieRepository,
    private val roomRepository: RoomRepository,
    private val roomDayScheduleRepository: RoomDayScheduleRepository,
) {

    fun addScreening(roomId: String, movieId: String, screeningDateTime: LocalDateTime) {
        val movie = movieRepository.get(movieId) ?: throw MovieNotFoundException(movieId)

        val (version, roomDailySchedule) = roomDayScheduleRepository.get(roomId, screeningDateTime.toLocalDate())
            ?: createNewRoomSchedule(roomId, screeningDateTime.toLocalDate())

        val updatedSchedule = roomDailySchedule.addScreening(movie, screeningDateTime.toLocalTime())

        roomDayScheduleRepository.save(VersionedEntity(version + 1, updatedSchedule))
    }

    private fun createNewRoomSchedule(roomId: String, day: LocalDate): VersionedEntity<RoomDaySchedule> {
        val room = roomRepository.get(roomId) ?: throw RoomNotFoundException(roomId)

        return VersionedEntity(0, RoomDaySchedule(room, day, openingHours))
    }

    fun getWeeklySchedule(dayInTheWeek: LocalDate): Schedule {
        val monday = dayInTheWeek.with(previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(7)

        val dailySchedules = roomDayScheduleRepository.allBetween(monday, sunday)

        return Schedule(dailySchedules)
    }

}