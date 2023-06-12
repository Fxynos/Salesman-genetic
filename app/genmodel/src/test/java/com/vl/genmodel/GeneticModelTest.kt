package com.vl.genmodel

import com.vl.genmodel.salesman.BreederImpl
import com.vl.genmodel.salesman.MutatorImpl
import com.vl.genmodel.salesman.PopulationSupplierImpl
import com.vl.genmodel.salesman.SelectorImpl
import com.vl.genmodel.salesman.VerbosePath
import org.junit.jupiter.api.RepeatedTest

class GeneticModelTest {
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

    private val geneticModel = GeneticModel.newBuilder<VerbosePath>()
        .setBreedStrategy(GeneticModel.BreedStrategy.ALL)
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
        .build()

    @RepeatedTest(10)
    fun test() {
        val iterations = 10_000L
        val ms = System.currentTimeMillis()
        geneticModel.start(iterations)
        geneticModel.awaitResult(false).let {
            println("${System.currentTimeMillis() - ms} ms for $iterations iterations")
            val best = it.population[0]
            println("${it.iterations} iterations: ${best.length} ${best.points.contentToString()}")
            assert(best.length <= 18) { "${it.iterations} iterations: ${best.length} ${best.points.contentToString()}" }
        }
    }
}