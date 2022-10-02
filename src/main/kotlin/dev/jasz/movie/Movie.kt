package dev.jasz.movie

import dev.jasz.common.TimeRange
import java.time.Duration
import java.time.LocalTime

data class Movie(
    val id: String,
    val name: String,
    val duration: Duration,
    val screeningCanStartBetweenAny: Set<TimeRange>,
    val requires3dGlasses: Boolean,
) {

    constructor(
        id: String,
        name: String,
        duration: Duration,
        requires3dGlasses: Boolean,
    ) : this(
        id = id,
        name = name,
        duration = duration,
        screeningCanStartBetweenAny = setOf(LocalTime.MIN.rangeTo(LocalTime.MAX)),
        requires3dGlasses = requires3dGlasses,
    )

    fun screeningCanStartAt(time: LocalTime): Boolean {
        return screeningCanStartBetweenAny.any { it.contains(time) }
    }

}
