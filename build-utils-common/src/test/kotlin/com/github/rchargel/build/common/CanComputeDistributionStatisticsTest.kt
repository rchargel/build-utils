package com.github.rchargel.build.common

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CanComputeDistributionStatisticsTest(
        private val values: List<Double>,
        private val expectedMean: Double,
        private val expectedVariance: Double,
        private val expectedSkewness: Double,
        private val expectedKurtosis: Double
) {

    @Test
    fun testMean() = assertEquals(expectedMean, toStats().mean, EPSILON)

    @Test
    fun testMeanParallel() = assertEquals(expectedMean, toParallelStats().mean, EPSILON)

    @Test
    fun testVariance() = assertEquals(expectedVariance, toStats().variance, EPSILON)

    @Test
    fun testVarianceParallel() = assertEquals(expectedVariance, toParallelStats().variance, EPSILON)

    @Test
    fun testSkewness() = assertEquals(expectedSkewness, toStats().skewness, EPSILON)

    @Test
    fun testSkewnessParallel() = assertEquals(expectedSkewness, toParallelStats().skewness, EPSILON)

    @Test
    fun testKurtosis() = assertEquals(expectedKurtosis, toStats().kurtosis, EPSILON)

    @Test
    fun testKurtosisParallel() = assertEquals(expectedKurtosis, toParallelStats().kurtosis, EPSILON)


    private fun toStats() = values.stream().reduce(DistributionStatistics(),
            { r, x -> DistributionStatistics.aggregate(r, x) },
            { a, b -> DistributionStatistics.merge(a, b) })

    private fun toParallelStats() = values.parallelStream().reduce(DistributionStatistics(),
            { r, x -> DistributionStatistics.aggregate(r, x) },
            { a, b -> DistributionStatistics.merge(a, b) })

    companion object {
        private const val EPSILON: Double = 0.000001

        @JvmStatic
        @Parameterized.Parameters(name = "List {0} -> mean {1} -> variance {2} -> skewness {3} -> kurtosis {4}")
        fun params() = listOf(
                arrayOf(listOf(-1.0, 0.0, 1.0, 1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 4.0, 5.0, 5.0, 5.0, 6.0, 7.0, 9.0, 9.0, 9.0, 100.0), 8.75, 470.1973684, 4.0008104, 17.3822174128),
                arrayOf(listOf(-1.0, 0.0, 1.0, 1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 4.0, 5.0, 5.0, 5.0, 6.0, 7.0), 3.0, 5.2, 0, 2.0355029585),
                arrayOf(listOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0), 4.5, 9.16666667, 0, 1.77575757576),
                arrayOf(listOf(70.0, 68.0, 12.0, 59.0, 16.0, 66.0, 47.0, 52.0, 64.0, 74.0), 52.8, 485.288889, -1.050393, 2.619318657)
        )

    }
}