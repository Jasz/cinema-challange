package dev.jasz.common

fun <T : Comparable<T>> ClosedRange<T>.overlaps(other: ClosedRange<T>): Boolean {
    return !(this.endInclusive < other.start || this.start > other.endInclusive)
}