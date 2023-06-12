package com.vl.genmodel

import com.vl.genmodel.salesman.VerbosePath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class PathTest {
    companion object {
        @JvmStatic
        private fun supplyLength_isCorrect(): Stream<InputLengthIsCorrect> {
            val distances = arrayOf(
                arrayOf(null, 1.0, 0.25),
                arrayOf(null, null, 5.0),
                arrayOf(0.5, null, null)
            )
            return Stream.of(
                InputLengthIsCorrect(distances, intArrayOf(0, 1), 1.0),
                InputLengthIsCorrect(distances, intArrayOf(2, 0, 1, 2), 6.5),
                InputLengthIsCorrect(distances, intArrayOf(0, 2, 0, 1), 1.75),
                InputLengthIsCorrect(distances, intArrayOf(1, 2), 5.0)
            )
        }
    }

    @ParameterizedTest
    @MethodSource("supplyLength_isCorrect")
    fun length_isCorrect(input: InputLengthIsCorrect) =
        assertEquals(
            VerbosePath(
                input.points,
                input.distances
            ).length, input.expectedLength)

    class InputLengthIsCorrect(
        val distances: Array<Array<Double?>>,
        val points: IntArray,
        val expectedLength: Double
    )
}