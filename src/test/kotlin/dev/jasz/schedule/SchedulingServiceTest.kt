package dev.jasz.schedule

import dev.jasz.movie.Movie
import dev.jasz.movie.MovieRepository
import dev.jasz.room.Room
import dev.jasz.room.RoomRepository
import dev.jasz.movie.exceptions.MovieNotFoundException
import dev.jasz.room.exceptions.RoomNotFoundException
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.*
import java.time.Duration
import java.time.LocalDate.parse as date
import java.time.LocalDateTime.parse as dateTime
import java.time.LocalTime.parse as time

internal class SchedulingServiceTest : FreeSpec({

    val aMovie1 = Movie(
        id = "1",
        name = "movie name",
        duration = Duration.ofMinutes(120),
        requires3dGlasses = false,
    )
    val movieRepository = object : MovieRepository {
        override fun get(movieId: String): Movie? {
            return when (movieId) {
                "1" -> {
                    aMovie1
                }

                else -> null
            }
        }
    }
    val aRoom1 = Room(
        id = "1",
        name = "room 1",
        cleanUpTime = Duration.ofMinutes(5),
    )
    val roomRepository = object : RoomRepository {
        override fun get(roomId: String): Room? {
            return when (roomId) {
                "1" -> {
                    aRoom1
                }

                else -> null
            }
        }
    }
    val roomDayScheduleRepository = InMemoryRoomDayScheduleRepository()

    val sut = SchedulingService(
        openingHours = time("08:00").rangeTo(time("22:00")),
        movieRepository = movieRepository,
        roomRepository = roomRepository,
        roomDayScheduleRepository = roomDayScheduleRepository,
    )

    beforeTest { roomDayScheduleRepository.clear() }

    "should create a room schedule" {
        // given
        val movieId = "1"
        val roomId = "1"
        val screeningDateTime = dateTime("2022-01-01T10:00")

        // when
        sut.addScreening(roomId, movieId, screeningDateTime)
        val versionedSchedule = roomDayScheduleRepository.get(roomId, screeningDateTime.toLocalDate())

        // then
        expectThat(versionedSchedule)
            .isNotNull()
            .and {
                get { version }.isEqualTo(1)
                get { entity.day }.isEqualTo(screeningDateTime.toLocalDate())
                get { entity.room }.isEqualTo(aRoom1)
                get { entity.screenings }.isEqualTo(setOf(Screening(aMovie1, aRoom1, screeningDateTime)))
            }
    }

    "should update an existing room schedule" {
        // given
        val movieId = "1"
        val roomId = "1"
        val screeningDate = date("2022-01-01")
        val screeningDateTime1 = screeningDate.atTime(time("09:00"))
        val screeningDateTime2 = screeningDate.atTime(time("15:00"))

        // when
        sut.addScreening(roomId, movieId, screeningDateTime1)
        sut.addScreening(roomId, movieId, screeningDateTime2)
        val versionedSchedule = roomDayScheduleRepository.get(roomId, screeningDate)

        // then
        expectThat(versionedSchedule)
            .isNotNull()
            .and {
                get { version }.isEqualTo(2)
                get { entity.day }.isEqualTo(screeningDate)
                get { entity.room }.isEqualTo(aRoom1)
                get { entity.screenings }.isEqualTo(
                    setOf(
                        Screening(aMovie1, aRoom1, screeningDateTime1),
                        Screening(aMovie1, aRoom1, screeningDateTime2),
                    )
                )
            }
    }

    "should throw" - {
        "when unknown movie id is provided" {
            // given
            val movieId = "2"
            val roomId = "1"
            val screeningDateTime = dateTime("2022-01-01T09:00")

            // when
            val codeThatShouldThrow = { sut.addScreening(roomId, movieId, screeningDateTime) }

            // then
            expectCatching(codeThatShouldThrow)
                .isFailure()
                .isA<MovieNotFoundException>()
                .get { this.movieId }.isEqualTo("2")
        }

        "when unknown room id is provided" {
            // given
            val movieId = "1"
            val roomId = "2"
            val screeningDateTime = dateTime("2022-01-01T09:00")

            // when
            val codeThatShouldThrow = { sut.addScreening(roomId, movieId, screeningDateTime) }

            // then
            expectCatching(codeThatShouldThrow)
                .isFailure()
                .isA<RoomNotFoundException>()
                .get { this.roomId }.isEqualTo("2")
        }
    }

    "should get a weekly schedule" - {
        // given
        withData(
            mapOf(
                "for the week containing the given Monday" to date("2022-01-03"),
                "for the week containing the given Wednesday" to date("2022-01-05"),
                "for the week containing the given Sunday" to date("2022-01-09"),
            )
        ) { date ->
            val movieId = "1"
            val roomId = "1"
            val screeningDateTime1 = dateTime("2022-01-04T09:00")
            val screeningDateTime2 = dateTime("2022-01-05T15:00")

            // when
            sut.addScreening(roomId, movieId, screeningDateTime1)
            sut.addScreening(roomId, movieId, screeningDateTime2)

            val schedule = sut.getWeeklySchedule(date)

            // then
            expectThat(schedule.schedulesByDay).containsExactly(
                DailySchedule(
                    day = date("2022-01-04"),
                    roomSchedules = mapOf(
                        aRoom1 to roomDayScheduleRepository.get(roomId, date("2022-01-04"))!!.entity,
                    ),
                ),
                DailySchedule(
                    day = date("2022-01-05"),
                    roomSchedules = mapOf(
                        aRoom1 to roomDayScheduleRepository.get(roomId, date("2022-01-05"))!!.entity,
                    ),
                ),
            )
        }
    }

})