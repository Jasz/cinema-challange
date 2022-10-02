package dev.jasz.schedule

import dev.jasz.common.TimeRange
import dev.jasz.common.copyAndAdd
import dev.jasz.movie.Movie
import dev.jasz.room.Room
import dev.jasz.schedule.exceptions.ConflictingScreeningsException
import dev.jasz.schedule.exceptions.RoomUnavailableException
import dev.jasz.schedule.exceptions.ScreeningNotWithinOperatingHoursException
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class RoomDaySchedule private constructor(
    val room: Room,
    val day: LocalDate,
    val operatingHours: TimeRange,
    private val orderedScreenings: TreeSet<Screening>, // TreeSet maintains order
) {

    val screenings = orderedScreenings.toSet()

    constructor(
        room: Room,
        day: LocalDate,
        timeRange: TimeRange,
    ) : this(
        room = room,
        day = day,
        operatingHours = timeRange,
        orderedScreenings = TreeSet(compareBy(Screening::startDateTime)),
    )

    fun addScreening(movie: Movie, startDateTime: LocalTime): RoomDaySchedule {
        val newScreening = Screening(movie, room, day.atTime(startDateTime))

        validate(newScreening)

        val screeningsCopy = orderedScreenings.copyAndAdd(newScreening)
        return this.copy(screenings = screeningsCopy)
    }

    private fun validate(newScreening: Screening) {
        checkIfRoomAvailable(newScreening)
        checkIfDuringOperatingHours(newScreening)
        checkForConflicts(newScreening)
    }

    private fun checkIfRoomAvailable(newScreening: Screening) {
        if (!room.isAvailableDuring(newScreening.dateTimeRange)) {
            throw RoomUnavailableException(room, newScreening)
        }
    }

    private fun checkIfDuringOperatingHours(newScreening: Screening) {
        if (!operatingHours.contains(newScreening.startDateTime.toLocalTime())) {
            throw ScreeningNotWithinOperatingHoursException(newScreening, operatingHours)
        }
    }

    private fun checkForConflicts(newScreening: Screening) {
        val conflictingScreenings = listOfNotNull(previousScreening(newScreening), nextScreening(newScreening))
            .filter { it.conflictsWith(newScreening) }

        if (conflictingScreenings.isNotEmpty()) {
            throw ConflictingScreeningsException(newScreening, conflictingScreenings)
        }
    }

    private fun previousScreening(screening: Screening): Screening? = orderedScreenings.floor(screening)
    private fun nextScreening(screening: Screening): Screening? = orderedScreenings.higher(screening)

    private fun copy(screenings: TreeSet<Screening>): RoomDaySchedule {
        return RoomDaySchedule(
            room = room,
            day = day,
            operatingHours = operatingHours,
            orderedScreenings = screenings,
        )
    }

}

