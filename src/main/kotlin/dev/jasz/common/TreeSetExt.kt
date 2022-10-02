package dev.jasz.common

import java.util.*

fun <E> TreeSet<E>.copyAndAdd(item: E): TreeSet<E> {
    // a TreeSet constructed with another TreeSet will use the same ordering
    return TreeSet(this).apply { add(item) }
}