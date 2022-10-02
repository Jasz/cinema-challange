package dev.jasz.common

import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.property.forAll
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

internal class ClosedRangeExtTest : FreeSpec({

    "should not overlap" - {
        // given
        withData(
            1.rangeTo(2) and 3.rangeTo(4),
            3.rangeTo(4) and 1.rangeTo(2),
            1.rangeTo(1) and 2.rangeTo(2),
            IntRange.EMPTY and IntRange.EMPTY named "when ranges are empty",
        ) { (range1, range2) ->
            // when
            val result = range1.overlaps(range2)

            // then
            expectThat(result).isFalse()
        }
    }

    "should overlap" - {
        // given
        withData(
            2.rangeTo(4) and 1.rangeTo(3),
            1.rangeTo(3) and 2.rangeTo(4),
            1.rangeTo(2) and 2.rangeTo(3),
            2.rangeTo(3) and 1.rangeTo(2),
            1.rangeTo(4) and 2.rangeTo(3) named "when a range is within the other",
            2.rangeTo(3) and 1.rangeTo(4) named "when a range is within the other",
            1.rangeTo(1) and 1.rangeTo(1),
        ) { (range1, range2) ->
            // when
            val result = range1.overlaps(range2)

            // then
            expectThat(result).isTrue()
        }
    }

    "overlapping should be commutative" - {
        forAll<Int, Int, Int, Int> { a, b, c, d ->
            // given
            val range1 = a.rangeTo(b)
            val range2 = c.rangeTo(d)

            // when
            val result1 = range1.overlaps(range2)
            val result2 = range2.overlaps(range1)

            // then
            result1 == result2
        }
    }

})

private infix fun <T : Comparable<T>> ClosedRange<T>.and(other: ClosedRange<T>): ClosedRangesPair<T> {
    return ClosedRangesPair(this, other)
}

private data class ClosedRangesPair<T : Comparable<T>>(
    val range1: ClosedRange<T>,
    val range2: ClosedRange<T>,
    val testName: String? = null,
) : WithDataTestName {

    override fun dataTestName() = testName?.plus(" ($this)") ?: toString()

    override fun toString() = "$range1 and $range2"

    infix fun named(testName: String) = this.copy(testName = testName)

}