package dev.jasz.schedule.exceptions

import dev.jasz.schedule.Screening
import java.lang.Exception

data class ConflictingScreeningsException(val newScreening: Screening, val conflictingScreenings: Collection<Screening>)
    : Exception("The new screening $newScreening would conflict with $conflictingScreenings")