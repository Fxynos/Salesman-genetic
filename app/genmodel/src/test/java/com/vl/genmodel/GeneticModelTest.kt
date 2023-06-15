package com.vl.genmodel

import com.vl.genmodel.GeneticModel.BreedStrategy
import com.vl.genmodel.salesman.BreederImpl
import com.vl.genmodel.salesman.MutatorImpl
import com.vl.genmodel.salesman.PopulationSupplierImpl
import com.vl.genmodel.salesman.SelectorImpl
import com.vl.genmodel.salesman.VerbosePath
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.lang.IllegalStateException

class GeneticModelTest {
    companion object {
        const val iterations = 10_000L

        private fun printAndAssertResult(
            result: GeneticModel.Result<VerbosePath>,
            startMs: Long,
            assertedMax: Double
        ) {
            println("${System.currentTimeMillis() - startMs} ms for $iterations iterations")
            val best = result.population[0]
            println("${result.iterations} iterations: ${best.length} ${best.points.contentToString()}")
            assert(best.length <= assertedMax) {
                "${result.iterations} iterations: ${best.length} ${best.points.contentToString()}"
            }
        }
    }

    private val distances = arrayOf(
        arrayOf(null, 1.0, 2.0, null, null, 5.0, null, null),
        arrayOf(1.0, null, null, 1.0, null, 2.0, null, null),
        arrayOf(2.0, null, null, null, 3.0, 1.0, null, null),
        arrayOf(null, 1.0, null, null, null, 3.0, 5.0, null),
        arrayOf(null, null, 3.0, null, null, null, 1.0, 0.5),
        arrayOf(5.0, 2.0, 1.0, 3.0, null, null, null, null),
        arrayOf(null, null, null, 5.0, 1.0, null, null, null),
        arrayOf(null, null, null, null, 0.5, null, null, null)
    )

    private val geneticModelBuilder = GeneticModel.newBuilder<VerbosePath>()
        .setBreedStrategy(BreedStrategy.ALL)
        .setBreeder(BreederImpl(distances))
        .setMutator(MutatorImpl(distances, 1, 3))
        .setMutationRate(0.5)
        .setPopulationSize(500)
        .setPopulationSupplier(
            PopulationSupplierImpl(
                8,
                12,
                distances
            )
        )
        .setSelector(SelectorImpl())


    private val geneticModel = geneticModelBuilder.build()

    @RepeatedTest(10)
    fun testBlockingResulting() {
        val ms = System.currentTimeMillis()
        geneticModel.start(iterations)
        val result = geneticModel.awaitResult(false)
        printAndAssertResult(result, ms, 18.0)
    }

    @Test
    fun testAsyncResulting() {
        val ms = System.currentTimeMillis()
        geneticModel.start(iterations)
        geneticModel.requestResult(false) { printAndAssertResult(it, ms, 18.0) }
        while (geneticModel.isStarted)
            Thread.sleep(10)
    }

    @Test
    fun testInterruption() {
        val ms = System.currentTimeMillis()
        geneticModel.start()
        Thread.sleep(1000)
        val result = geneticModel.awaitResult(true)
        print("Interruption: "); printAndAssertResult(result, ms, Double.MAX_VALUE)
    }

    @ParameterizedTest
    @EnumSource(BreedStrategy::class)
    fun testBreedStrategies(strategy: BreedStrategy) {
        val ms = System.currentTimeMillis()
        val geneticModel = geneticModelBuilder.setBreedStrategy(strategy).build()
        geneticModel.start(iterations)
        val result = geneticModel.awaitResult(false)
        print("${strategy.name}: "); printAndAssertResult(result, ms, 25.0)
    }

    @Test
    fun testTwiceStart() {
        geneticModel.start()
        assertThrows<IllegalStateException> { geneticModel.start() }
        geneticModel.awaitResult(true)
    }

    @Test
    fun testIntermediateResult() {
        assertThrows<IllegalStateException> { geneticModel.intermediateResult }
        geneticModel.start()
        geneticModel.intermediateResult
        geneticModel.awaitResult(true)
    }
}