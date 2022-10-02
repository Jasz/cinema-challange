package dev.jasz.schedule

import dev.jasz.movie.Movie
import dev.jasz.room.Room
import io.kotest.core.spec.style.FreeSpec
import strikt.api.expectThat
import strikt.assertions.containsExactly
import java.time.Duration
import java.time.LocalDate.parse as date
import java.time.LocalTime.parse as time

internal class ScheduleTest : FreeSpec({

    val aRoom1 = Room(id = "id1", name = "name1", cleanUpTime = Duration.ofMinutes(10))
    val aRoom2 = Room(id = "id2", name = "name2", cleanUpTime = Duration.ofMinutes(5))

    val aMovieA = Movie(
        id = "idA",
        name = "movieA",
        duration = Duration.ofMinutes(120),
        requires3dGlasses = false,
    )
    val aMovieB = Movie(
        id = "idB",
        name = "movieB",
        duration = Duration.ofMinutes(120),
        requires3dGlasses = false,
    )

    "should group schedules by day and room" {
        // given
        val roomSchedules = listOf(
            // room1
            RoomDaySchedule(aRoom1, date("2022-01-01"), time("08:00").rangeTo(time("22:00")))
                .addScreening(aMovieA, time("09:30"))
                .addScreening(aMovieA, time("13:30")),
            RoomDaySchedule(aRoom1, date("2022-01-02"), time("08:00").rangeTo(time("22:00")))
                .addScreening(aMovieA, time("11:00"))
                .addScreening(aMovieB, time("13:30")),

            // room2
            RoomDaySchedule(aRoom2, date("2022-01-01"), time("08:00").rangeTo(time("22:00")))
                .addScreening(aMovieB, time("16:00"))
                .addScreening(aMovieB, time("20:30")),
            RoomDaySchedule(aRoom2, date("2022-01-02"), time("08:00").rangeTo(time("22:00")))
                .addScreening(aMovieB, time("21:00")),
            RoomDaySchedule(aRoom2, date("2022-01-03"), time("08:00").rangeTo(time("22:00")))
                .addScreening(aMovieB, time("18:00")),
        )

        // when
        val schedule = Schedule(roomSchedules)

        // then
        expectThat(schedule.schedulesByDay).containsExactly(
            DailySchedule(
                day = date("2022-01-01"),
                roomSchedules = mapOf(
                    aRoom1 to roomSchedules[0],
                    aRoom2 to roomSchedules[2],
                ),
            ),
            DailySchedule(
                day = date("2022-01-02"),
                roomSchedules = mapOf(
                    aRoom1 to roomSchedules[1],
                    aRoom2 to roomSchedules[3],
                ),
            ),
            DailySchedule(
                day = date("2022-01-03"),
                roomSchedules = mapOf(
                    aRoom2 to roomSchedules[4],
                ),
            ),
        )
    }

    "should order schedules by day" {
        // given
        val roomSchedules = listOf(
            RoomDaySchedule(aRoom1, date("2022-01-02"), time("08:00").rangeTo(time("22:00")))
                .addScreening(aMovieA, time("11:00"))
                .addScreening(aMovieB, time("13:30")),
            RoomDaySchedule(aRoom1, date("2022-01-01"), time("08:00").rangeTo(time("22:00")))
                .addScreening(aMovieA, time("09:30"))
                .addScreening(aMovieA, time("13:30")),
        )

        // when
        val schedule = Schedule(roomSchedules)

        // then
        expectThat(schedule.schedulesByDay).containsExactly(
            DailySchedule(
                day = date("2022-01-01"),
                roomSchedules = mapOf(aRoom1 to roomSchedules[1]),
            ),
            DailySchedule(
                day = date("2022-01-02"),
                roomSchedules = mapOf(aRoom1 to roomSchedules[0]),
            ),
        )
    }

})