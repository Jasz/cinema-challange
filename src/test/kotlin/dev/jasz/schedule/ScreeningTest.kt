package dev.jasz.schedule

import dev.jasz.movie.Movie
import dev.jasz.room.Room
import io.kotest.core.spec.style.FreeSpec
import strikt.api.expect
import strikt.assertions.isEqualTo
import java.time.Duration
import java.time.LocalDateTime.parse as dateTime
import java.time.LocalTime.parse as time

internal class ScreeningTest : FreeSpec({

    "should properly construct a screening" {
        // given
        val movie = Movie(
            id = "movieId",
            name = "movie name",
            duration = Duration.ofMinutes(180),
            screeningCanStartBetweenAny = setOf(time("17:00").rangeTo(time("21:00"))),
            requires3dGlasses = true,
        )
        val room = Room(
            id = "roomId",
            name = "room 1",
            cleanUpTime = Duration.ofMinutes(5),
        )

        // when
        val screening = Screening(
            movie,
            room,
            startTime = dateTime("2022-01-01T19:00"),
        )

        // then
        expect {
            that(screening.startDateTime).isEqualTo(dateTime("2022-01-01T19:00"))
            that(screening.durationTotal).isEqualTo(Duration.ofMinutes(185))
            that(screening.endDateTime).isEqualTo(dateTime("2022-01-01T22:05"))
            that(screening.dateTimeRange)
                .isEqualTo(dateTime("2022-01-01T19:00").rangeTo(dateTime("2022-01-01T22:05")))
        }
    }

})