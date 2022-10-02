package dev.jasz.common

import io.kotest.core.spec.style.FreeSpec
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class TreeSetExtTest : FreeSpec({

    "should maintain the order when creating a copy" {
        // given
        val original = sortedSetOf(compareBy { -it }, 4, 1, 2)

        val result = original.copyAndAdd(3)

        expectThat(result.toList()).isEqualTo(listOf(4, 3, 2, 1))
    }

})
