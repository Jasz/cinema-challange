package dev.jasz.movie.exceptions

import dev.jasz.movie.Movie
import java.time.LocalTime

data class MovieScreenedOutsideOfPossibleHoursException(val movie: Movie, val screeningStartTime: LocalTime)
    : Exception("Movie $movie cannot start at $screeningStartTime")