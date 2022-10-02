package dev.jasz.schedule

import dev.jasz.common.DateTimeRange
import dev.jasz.common.overlaps
import dev.jasz.movie.Movie
import dev.jasz.room.Room
import dev.jasz.movie.exceptions.MovieScreenedOutsideOfPossibleHoursException
import java.time.Duration
import java.time.LocalDateTime

class Screening private constructor(
    val movie: Movie,
    val startDateTime: LocalDateTime,
    val durationTotal: Duration,
) {

    constructor(
        movie: Movie,
        room: Room,
        startTime: LocalDateTime,
    ) : this(
        movie = movie,
        startDateTime = startTime,
        durationTotal = movie.duration + room.cleanUpTime,
    )

    init {
        if (!movie.screeningCanStartAt(startDateTime.toLocalTime())) {
            throw MovieScreenedOutsideOfPossibleHoursException(movie, startDateTime.toLocalTime())
        }
    }

    val endDateTime: LocalDateTime = startDateTime + durationTotal
    val dateTimeRange: DateTimeRange = startDateTime.rangeTo(endDateTime)

    fun conflictsWith(other: Screening): Boolean {
        return this.dateTimeRange.overlaps(other.dateTimeRange)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Screening

        if (movie != other.movie) return false
        if (startDateTime != other.startDateTime) return false
        if (durationTotal != other.durationTotal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = movie.hashCode()
        result = 31 * result + startDateTime.hashCode()
        result = 31 * result + durationTotal.hashCode()
        return result
    }

    override fun toString(): String {
        return "Screening(movie=${movie.name}, dateTimeRange=$dateTimeRange)"
    }

}

