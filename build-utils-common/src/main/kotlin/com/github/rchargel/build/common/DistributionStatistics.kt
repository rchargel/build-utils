package com.github.rchargel.build.common

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable
import java.lang.Math.pow
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class DistributionStatistics(
        val count: Long = 0,
        val sum: Double = 0.0,
        val sumOfSquares: Double = 0.0,
        val mean: Double = 0.0,
        val variance: Double = 0.0,
        val skewness: Double = 0.0,
        val kurtosis: Double = 0.0,
        val minimum: Double = Double.POSITIVE_INFINITY,
        val maximum: Double = Double.NEGATIVE_INFINITY,
        private val m2: Double = 0.0,
        private val m3: Double = 0.0,
        private val m4: Double = 0.0
) : Serializable {
    @get:JsonIgnore
    val standardDeviation: Double
        get() = sqrt(variance)

    companion object {
        private const val THREE_OVER_TWO: Double = 3.0 / 2.0

        @JvmStatic
        fun aggregate(moments: DistributionStatistics, x: Double): DistributionStatistics {
            val count = moments.count + 1
            val sum = moments.sum + x
            val min = min(moments.minimum, x)
            val max = max(moments.maximum, x)
            val sumOfSquares = moments.sumOfSquares + x * x
            val delta = x - moments.mean
            val deltaOverCount = delta / count
            val deltaOverCountSquared = deltaOverCount * deltaOverCount
            val deltaSquaredOverCountTimesCountMinusOne = delta * deltaOverCount * moments.count

            val mean = sum / count
            val variance = (count * sumOfSquares - sum * sum) / (count * (count - 1))

            val m4 = moments.m4 + deltaSquaredOverCountTimesCountMinusOne * deltaOverCountSquared * (count * count - 3 * count + 3) +
                    6 * deltaOverCountSquared * moments.m2 - 4 * deltaOverCount * moments.m3
            val m3 = moments.m3 + deltaSquaredOverCountTimesCountMinusOne * deltaOverCount * (count - 2) - 3 * deltaOverCount * moments.m2
            val m2 = moments.m2 + deltaSquaredOverCountTimesCountMinusOne

            val skewness = sqrt(count.toDouble()) * m3 / pow(m2, THREE_OVER_TWO)
            val kurtosis = count * m4 / (m2 * m2)

            return DistributionStatistics(count, sum, sumOfSquares, mean, variance, skewness, kurtosis, min, max, m2, m3, m4)
        }

        @JvmStatic
        fun merge(r1: DistributionStatistics, r2: DistributionStatistics): DistributionStatistics {
            val count = r1.count + r2.count

            // if both partitions are empty, return r1 without performing anymore
            // calculations
            if (count == 0L)
                return r1

            val sum = r1.sum + r2.sum
            val sumOfSquares = r1.sumOfSquares + r2.sumOfSquares

            val delta = r2.mean - r1.mean
            val delta2 = delta * delta
            val delta3 = delta * delta2
            val delta4 = delta2 * delta2

            val mean = sum / count

            val variance = (count * sumOfSquares - sum * sum) / (count * (count - 1))

            val m2 = r1.m2 + r2.m2 + delta2 * r1.count * r2.count / count

            val m3 = r1.m3 + r2.m3 + delta3 * r1.count * r2.count * (r1.count - r2.count) / (count * count) +
                    3.0 * delta * (r1.count * r2.m2 - r2.count * r1.m2) / count

            val m4 = r1.m4 + r2.m4 + delta4 * r1.count * r2.count * (r1.count * r1.count - r1.count * r2.count + r2.count * r2.count) /
                    (count * count * count) + (6.0 * delta2 * (r1.count * r1.count * r2.m2 + r2.count * r2.count * r1.m2) /
                    (count * count) + 4.0 * delta * (r1.count * r2.m3 - r2.count * r1.m3) / count)

            val skewness = sqrt(count.toDouble()) * m3 / pow(m2, THREE_OVER_TWO)
            val kurtosis = count * m4 / (m2 * m2)

            return DistributionStatistics(count, sum, sumOfSquares, mean, variance, skewness, kurtosis,
                    min(r1.minimum, r2.minimum), max(r1.maximum, r2.maximum), m2, m3, m4)
        }
    }
}