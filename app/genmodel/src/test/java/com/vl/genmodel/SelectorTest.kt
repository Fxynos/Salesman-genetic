package com.vl.genmodel

import com.vl.genmodel.salesman.SelectorImpl
import com.vl.genmodel.salesman.VerbosePath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class SelectorTest {
    companion object {
        val selector = SelectorImpl()

        @JvmStatic
        fun supply_selectorIsCorrect(): Stream<Triple<VerbosePath, VerbosePath, Int>> {
            val distances = arrayOf(
                arrayOf(null, 1.0, 0.25),
                arrayOf(null, null, 5.0),
                arrayOf(0.5, null, null)
            )
            return Stream.of(
                Triple(
                    VerbosePath(
                        intArrayOf(0, 1, 2, 0),
                        distances
                    ),
                    VerbosePath(
                        intArrayOf(0, 1, 2, 0),
                        distances
                    ),
                    0
                ),
                Triple(
                    VerbosePath(
                        intArrayOf(0, 1, 2),
                        distances
                    ),
                    VerbosePath(
                        intArrayOf(0, 1, 2, 0),
                        distances
                    ),
                    1
                ),
                Triple(
                    VerbosePath(
                        intArrayOf(0, 1, 2),
                        distances
                    ),
                    VerbosePath(
                        intArrayOf(0, 1),
                        distances
                    ),
                    -1
                ),
                Triple(
                    VerbosePath(
                        intArrayOf(0, 1, 2),
                        distances
                    ),
                    VerbosePath(
                        intArrayOf(2, 0, 1),
                        distances
                    ),
                    1
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("supply_selectorIsCorrect")
    fun selector_isCorrect(triple: Triple<VerbosePath, VerbosePath, Int>) =
        assertEquals(selector.compare(triple.first, triple.second), triple.third)
}