package dev.jasz.movie.exceptions

data class MovieNotFoundException(val movieId: String) : Exception("Movie with id $movieId not found")