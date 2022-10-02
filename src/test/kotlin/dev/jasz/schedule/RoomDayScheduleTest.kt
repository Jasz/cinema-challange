package dev.jasz.schedule

import dev.jasz.common.TimeRange
import dev.jasz.movie.Movie
import dev.jasz.room.DayOfWeekOrAny
import dev.jasz.room.Room
import dev.jasz.room.RoomUnavailability
import dev.jasz.schedule.exceptions.ConflictingScreeningsException
import dev.jasz.movie.exceptions.MovieScreenedOutsideOfPossibleHoursException
import dev.jasz.schedule.exceptions.RoomUnavailableException
import dev.jasz.schedule.exceptions.ScreeningNotWithinOperatingHoursException
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import strikt.api.expect
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.*
import java.time.DayOfWeek.*
import java.time.Duration
import java.time.LocalDate.parse as date
import java.time.LocalDateTime.parse as dateTime
import java.time.LocalTime.parse as time

internal class RoomDayScheduleTest : FreeSpec({

    val aRoom = Room(
        id = "roomId",
        name = "name",
        cleanUpTime = Duration.ofMinutes(10),
    )

    val aMovie = Movie(
        id = "movieId",
        name = "movie name",
        duration = Duration.ofMinutes(120),
        requires3dGlasses = false,
    )

    "should return a copy with a new screening" {
        // given
        val original = RoomDaySchedule(aRoom, date("2022-01-01"), time("08:00").rangeTo(time("22:00")))

        // when
        val result = original.addScreening(aMovie, time("10:00"))

        // then
        expect {
            that(original.screenings).isEmpty()
            that(result.screenings).containsExactly(Screening(aMovie, aRoom, dateTime("2022-01-01T10:00")))
        }
    }

    "should throw when adding a screening" - {
        // given
        withData(
            mapOf(
                "before the operating hours" to "07:59",
                "after the operating hours" to "22:01",
            )
        ) { time ->
            val original = RoomDaySchedule(
                aRoom,
                date("2022-01-01"),
                time("08:00").rangeTo(time("22:00")),
            )

            // when
            val codeThatShouldThrow = { original.addScreening(aMovie, time(time)) }

            // then
            expectCatching(codeThatShouldThrow)
                .isFailure()
                .isA<ScreeningNotWithinOperatingHoursException>()
                .and {
                    get { screening }.isEqualTo(Screening(aMovie, aRoom, date("2022-01-01").atTime(time(time))))
                    get { operatingHours }.isEqualTo(time("08:00").rangeTo(time("22:00")))
                }
        }
    }

    "should throw when a new screening conflicts" - {
        // given
        val newScreeningTime = time("14:00")

        withData(
            mapOf(
                "with an existing screening at the same time" to "14:00",
                "with a preceding screening when movies overlap" to "13:00",
                "with a preceding screening when room cleaning time overlaps" to "11:55",
                "with a preceding screening when it would end exactly at the start of the new one" to "11:50",
                "with a succeeding screening when movies overlap" to "15:00",
                "with a succeeding screening when room cleaning time overlaps" to "16:05",
                "with a succeeding screening when it would start exactly at the end of the new one" to "16:10",
            )
        ) { existingScreeningTime ->
            val original = RoomDaySchedule(
                aRoom,
                date("2022-01-01"),
                time("08:00").rangeTo(time("22:00")),
            )
                .addScreening(aMovie, time(existingScreeningTime))

            // when
            val codeThatShouldThrow = { original.addScreening(aMovie, newScreeningTime) }

            // then
            expectCatching(codeThatShouldThrow)
                .isFailure()
                .isA<ConflictingScreeningsException>()
                .and {
                    get { newScreening }
                        .isEqualTo(Screening(aMovie, aRoom, date("2022-01-01").atTime(newScreeningTime)))

                    get { conflictingScreenings }
                        .containsExactly(
                            Screening(aMovie, aRoom, date("2022-01-01").atTime(time(existingScreeningTime)))
                        )
                }
        }
    }

    "should throw when adding a screening outside of hours available for the movie" - {
        // given
        withData(
            MovieValidTimesTestData(
                validTimes = setOf(time("08:00").rangeTo(time("12:00"))),
                attemptedScreeningTime = "12:01",
            ),
            MovieValidTimesTestData(
                validTimes = setOf(time("08:00").rangeTo(time("12:00"))),
                attemptedScreeningTime = "13:00",
            ),
            MovieValidTimesTestData(
                validTimes = setOf(time("08:00").rangeTo(time("12:00")), time("14:00").rangeTo(time("15:00"))),
                attemptedScreeningTime = "13:00",
            ),
        ) { (validMovieTimes, attemptedTime) ->
            val movieWithValidScreeningTimes = aMovie.copy(screeningCanStartBetweenAny = validMovieTimes)

            // when
            val codeThatShouldThrow = {
                RoomDaySchedule(
                    aRoom,
                    date("2022-01-01"),
                    time("08:00").rangeTo(time("22:00")),
                )
                    .addScreening(movieWithValidScreeningTimes, time(attemptedTime))
            }

            // then
            expectCatching(codeThatShouldThrow)
                .isFailure()
                .isA<MovieScreenedOutsideOfPossibleHoursException>()
                .and {
                    get { screeningStartTime }.isEqualTo(time(attemptedTime))
                    get { movie }.isEqualTo(movieWithValidScreeningTimes)
                }
        }
    }

    "should add a screening when inside the hours available for the movie" - {
        // given
        withData(
            MovieValidTimesTestData(
                validTimes = setOf(time("08:00").rangeTo(time("12:00"))),
                attemptedScreeningTime = "09:00",
            ),
            MovieValidTimesTestData(
                validTimes = setOf(time("08:00").rangeTo(time("12:00"))),
                attemptedScreeningTime = "08:00",
            ),
            MovieValidTimesTestData(
                validTimes = setOf(time("08:00").rangeTo(time("12:00"))),
                attemptedScreeningTime = "12:00",
            ),
            MovieValidTimesTestData(
                validTimes = setOf(time("08:00").rangeTo(time("12:00")), time("14:00").rangeTo(time("15:00"))),
                attemptedScreeningTime = "09:00",
            ),
            MovieValidTimesTestData(
                validTimes = setOf(time("08:00").rangeTo(time("12:00")), time("14:00").rangeTo(time("15:00"))),
                attemptedScreeningTime = "14:00",
            ),
        ) { (validMovieTimes, attemptedTime) ->
            val movie = aMovie.copy(screeningCanStartBetweenAny = validMovieTimes)

            // when
            val result = RoomDaySchedule(
                aRoom,
                date("2022-01-01"),
                time("08:00").rangeTo(time("22:00")),
            )
                .addScreening(movie, time(attemptedTime))

            // then
            expectThat(result.screenings)
                .containsExactly(Screening(movie, aRoom, dateTime("2022-01-01T$attemptedTime")))
        }
    }

    "should throw if screening on Saturday 11:00-13:10 would be during room unavailability" - {
        // given
        withData(
            nameFn = { it.toString() },
            setOf(RoomUnavailability(DayOfWeekOrAny.Any, time("09:00").rangeTo(time("12:00")))),
            setOf(RoomUnavailability(DayOfWeekOrAny.Any, time("13:10").rangeTo(time("14:00")))),
            setOf(RoomUnavailability(DayOfWeekOrAny.of(SATURDAY), time("09:00").rangeTo(time("12:00")))),
            setOf(
                RoomUnavailability(DayOfWeekOrAny.of(FRIDAY), time("12:00").rangeTo(time("14:00"))),
                RoomUnavailability(DayOfWeekOrAny.of(SATURDAY), time("09:00").rangeTo(time("12:00"))),
            ),
        ) { unavailabilities ->
            val roomWithUnavailabilities = aRoom.copy(unavailabilities = unavailabilities)

            // when
            val codeThatShouldThrow = {
                RoomDaySchedule(
                    roomWithUnavailabilities,
                    date("2022-01-01"), // Saturday
                    time("08:00").rangeTo(time("22:00")),
                )
                    .addScreening(aMovie, time("11:00"))
            }

            // then
            expectCatching(codeThatShouldThrow)
                .isFailure()
                .isEqualTo(
                    RoomUnavailableException(
                        roomWithUnavailabilities,
                        Screening(aMovie, roomWithUnavailabilities, date("2022-01-01").atTime(time("11:00"))),
                    )
                )
        }
    }

    "should add screening on Saturday 11:00-13:10 if it's not during room unavailability" - {
        // given
        withData(
            nameFn = { it.toString() },
            setOf(RoomUnavailability(DayOfWeekOrAny.Any, time("14:00").rangeTo(time("16:00")))),
            setOf(RoomUnavailability(DayOfWeekOrAny.of(SUNDAY), time("09:00").rangeTo(time("12:00")))),
            setOf(
                RoomUnavailability(DayOfWeekOrAny.of(FRIDAY), time("09:00").rangeTo(time("14:00"))),
                RoomUnavailability(DayOfWeekOrAny.of(SUNDAY), time("09:00").rangeTo(time("12:00"))),
            ),
        ) { unavailabilities ->
            val roomWithUnavailabilities = aRoom.copy(unavailabilities = unavailabilities)

            // when
            val result = RoomDaySchedule(
                roomWithUnavailabilities,
                date("2022-01-01"), // Saturday
                time("08:00").rangeTo(time("22:00")),
            )
                .addScreening(aMovie, time("11:00"))

            // then
            expectThat(result.screenings)
                .containsExactly(
                    Screening(aMovie, roomWithUnavailabilities, date("2022-01-01").atTime(time("11:00"))),
                )
        }
    }

})

private data class MovieValidTimesTestData(
    val validTimes: Set<TimeRange>,
    val attemptedScreeningTime: String,
) : WithDataTestName {
    override fun dataTestName() = "valid times = $validTimes, attempted time = $attemptedScreeningTime"
}
