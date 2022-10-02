package dev.jasz.movie

interface MovieRepository {

    fun get(movieId: String): Movie?

}
