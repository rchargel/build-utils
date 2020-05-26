package com.github.rchargel.build.benchmark.results

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.rchargel.build.common.DistributionStatistics
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.EMPTY
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest
import org.openjdk.jmh.results.RunResult
import org.openjdk.jmh.util.ListStatistics
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors.toMap
import kotlin.collections.HashMap
import kotlin.math.abs

data class BenchmarkTestResult(
        val packageName: String = EMPTY,
        val className: String = EMPTY,
        val methodName: String = EMPTY,
        val mode: String = EMPTY,
        val numberOfTestThreads: Int = 0,
        val numberOfTestRepetitions: Int = 0,
        val numberOfWarmupIterations: Int = 0,
        val numberOfMeasurementIterations: Int = 0,
        val measurementTimeInMilliseconds: Long = 0,
        val warmupTimeInMilliseconds: Long = 0,
        val scoreUnits: String = EMPTY,
        val distributionStatistics: DistributionStatistics = DistributionStatistics(),
        val median: Double = 0.0,
        val meanErrorAt999: Double = 0.0,
        val firstQuarter: Double = 0.0,
        val thirdQuarter: Double = 0.0,
        val rawMeasurements: List<Double> = emptyList(),
        val pvalue: Double? = null,
        val baselineDistributionStatistics: DistributionStatistics? = null,
        val baselineMedian: Double? = null,
        val baselineFirstQuarter: Double? = null,
        val baselineThirdQuarter: Double? = null,
        val baselineMeasurements: List<Double>? = null
) {
    @get:JsonIgnore
    val key: String
        get() = "$packageName.$className.$methodName - $mode"

    @get:JsonIgnore
    val rawMeasurementsWithoutOutliers: List<Double>
        get() {
            val interQuartileRange = abs(thirdQuarter - firstQuarter)
            val allowedLow = firstQuarter - interQuartileRange * 1.5
            val allowedHigh = thirdQuarter + interQuartileRange * 1.5
            return rawMeasurements.filter { it in allowedLow..allowedHigh }
        }

    @get:JsonIgnore
    val baselineMeasurementsWithoutOutliers: List<Double>?
        get() {
            val interQuartileRange = abs((baselineThirdQuarter ?: 0.0) - (baselineFirstQuarter ?: 0.0))
            val allowedLow = (baselineFirstQuarter ?: 0.0) - interQuartileRange * 1.5
            val allowedHigh = (baselineThirdQuarter ?: 0.0) + interQuartileRange * 1.5
            return baselineMeasurements?.filter { it in allowedLow..allowedHigh }
        }

    @get:JsonIgnore
    val hasBaselineComparison: Boolean
        get() = baselineMeasurements?.isEmpty() == false

    fun merge(other: BenchmarkTestResult): BenchmarkTestResult {
        val rawData = rawMeasurements.toMutableList()
        rawData.addAll(other.rawMeasurements)

        val stats = ListStatistics(rawData.toDoubleArray())
        return builder("$packageName.$className.$methodName")
                .packageName(packageName)
                .className(className)
                .methodName(methodName)
                .numberOfTestThreads(numberOfTestThreads)
                .numberOfTestRepititions(numberOfTestRepetitions + other.numberOfTestRepetitions)
                .numberOfWarmupIterations(numberOfWarmupIterations)
                .numberOfMeasurementIterations(numberOfMeasurementIterations)
                .warmupTimeInMilliseconds(warmupTimeInMilliseconds)
                .measurementTimeInMilliseconds(measurementTimeInMilliseconds)
                .mode(mode)
                .scoreUnits(scoreUnits)
                .medianMeasurement(stats.getPercentile(50.0))
                .firstQuarterMeasurement(stats.getPercentile(25.0))
                .thirdQuarterMeasurement(stats.getPercentile(75.0))
                .meanErrorAt999(stats.getMeanErrorAt(0.999))
                .rawMeasurements(rawData)
                .build()
    }

    fun compareWithBaseline(baseline: BenchmarkTestResult?) = if (baseline == null || baseline.key == key)
        BenchmarkTestResult(
                packageName,
                className,
                methodName,
                mode,
                numberOfTestThreads,
                numberOfTestRepetitions,
                numberOfWarmupIterations,
                numberOfMeasurementIterations,
                measurementTimeInMilliseconds,
                warmupTimeInMilliseconds,
                scoreUnits,
                distributionStatistics,
                median,
                firstQuarter,
                thirdQuarter,
                meanErrorAt999,
                rawMeasurements,
                if (baseline?.rawMeasurements?.isEmpty() == false) KolmogorovSmirnovTest()
                        .kolmogorovSmirnovTest(rawMeasurementsWithoutOutliers.toDoubleArray(), baseline?.rawMeasurementsWithoutOutliers?.toDoubleArray()) else Double.NaN,
                baseline?.distributionStatistics ?: DistributionStatistics(),
                baseline?.median ?: Double.NaN,
                baseline?.firstQuarter,
                baseline?.thirdQuarter,
                baseline?.rawMeasurements.orEmpty()
        )
    else throw RuntimeException("Measurements don't belong to same test: $key != ${baseline.key}")

    companion object {
        internal fun stringifyParams(paramEntries: Set<Map.Entry<String, String>>?) =
                paramEntries?.sortedBy { entry -> entry.key }?.joinToString(", ", "[ ", " ]") { entry -> "${entry.key}=${entry.value}" }.orEmpty()

        @JvmStatic
        fun builder(name: String) = Builder(name)

        @JvmStatic
        fun fromRunResult(runResult: RunResult) = builder(runResult.params.benchmark)
                .params(runResult.params.paramsKeys.stream().collect(toMap({ s -> s }, { s -> runResult.params.getParam(s) })))
                .numberOfTestThreads(runResult.params.threads)
                .numberOfTestRepititions(1)
                .numberOfWarmupIterations(runResult.params.warmup.count)
                .numberOfMeasurementIterations(runResult.params.measurement.count)
                .warmupTimeInMilliseconds(runResult.params.warmup.time.convertTo(TimeUnit.MILLISECONDS))
                .measurementTimeInMilliseconds(runResult.params.measurement.time.convertTo(TimeUnit.MILLISECONDS))
                .mode(runResult.params.mode.toString())
                .scoreUnits(runResult.aggregatedResult.scoreUnit)
                .medianMeasurement(runResult.aggregatedResult.primaryResult.getStatistics().getPercentile(50.0))
                .firstQuarterMeasurement(runResult.aggregatedResult.primaryResult.getStatistics().getPercentile(25.0))
                .thirdQuarterMeasurement(runResult.aggregatedResult.primaryResult.getStatistics().getPercentile(75.0))
                .meanErrorAt999(runResult.aggregatedResult.primaryResult.getStatistics().getMeanErrorAt(0.999))
                .rawMeasurements(runResult.aggregatedResult.iterationResults.map { it.primaryResult.getScore() })
                .build()

        class Builder internal constructor(
                private var name: String,
                private var internalMethodName: String? = null,
                private var internalClassName: String? = null,
                private var internalPackageName: String? = null,
                private var mode: String? = null,
                private var numberOfTestThreads: Int = 1,
                private var numberOfTestRepetitions: Int = 1,
                private var numberOfWarmupIterations: Int = 0,
                private var numberOfMeasurementIterations: Int = 0,
                private var measurementTimeInMilliseconds: Long = 0,
                private var warmupTimeInMilliseconds: Long = 0,
                private var scoreUnits: String? = null,
                private var medianMeasurement: Double = 0.0,
                private var firstQuarterMeasurement: Double = 0.0,
                private var thirdQuarterMeasurement: Double = 0.0,
                private var meanErrorAt999: Double = 0.0,
                private var rawMeasurements: ArrayList<Double> = ArrayList<Double>(),
                private var params: HashMap<String, String> = HashMap()
        ) {
            fun methodName(methodName: String) = apply { this.internalMethodName = methodName }
            fun className(className: String) = apply { this.internalClassName = className }
            fun packageName(packageName: String) = apply { this.internalPackageName = packageName }
            fun mode(mode: String) = apply { this.mode = mode }
            fun numberOfTestThreads(numberOfTestThreads: Int) = apply { this.numberOfTestThreads = numberOfTestThreads }
            fun numberOfTestRepititions(numberOfTestRepetitions: Int) = apply { this.numberOfTestRepetitions = numberOfTestRepetitions }
            fun numberOfWarmupIterations(numberOfWarmupIterations: Int) = apply { this.numberOfWarmupIterations = numberOfWarmupIterations }
            fun numberOfMeasurementIterations(numberOfMeasurementIterations: Int) = apply { this.numberOfMeasurementIterations = numberOfMeasurementIterations }
            fun measurementTimeInMilliseconds(measurementTimeInMilliseconds: Long) = apply { this.measurementTimeInMilliseconds = measurementTimeInMilliseconds }
            fun warmupTimeInMilliseconds(warmupTimeInMilliseconds: Long) = apply { this.warmupTimeInMilliseconds = warmupTimeInMilliseconds }
            fun scoreUnits(scoreUnits: String) = apply { this.scoreUnits = scoreUnits }
            fun medianMeasurement(medianMeasurement: Double) = apply { this.medianMeasurement = medianMeasurement }
            fun firstQuarterMeasurement(firstQuarterMeasurement: Double) = apply { this.firstQuarterMeasurement = firstQuarterMeasurement }
            fun thirdQuarterMeasurement(thirdQuarterMeasurement: Double) = apply { this.thirdQuarterMeasurement = thirdQuarterMeasurement }
            fun meanErrorAt999(meanErrorAt999: Double) = apply { this.meanErrorAt999 = meanErrorAt999 }
            fun addRawMeasurement(rawMeasurement: Double) = apply { this.rawMeasurements.add(rawMeasurement) }
            fun rawMeasurements(rawMeasurements: List<Double>) = apply { this.rawMeasurements = ArrayList(rawMeasurements) }
            fun addParam(name: String, value: String) = apply { this.params[name] = value }
            fun params(params: Map<String, String>) = apply { this.params = HashMap(params) }

            fun build(): BenchmarkTestResult {
                val nameParts = name.split(".")
                val packageName = internalPackageName ?: StringUtils.join(nameParts.subList(0, nameParts.size - 2), ".")
                val className = internalClassName ?: nameParts[nameParts.size - 2]
                val methodName = internalMethodName
                        ?: "${nameParts[nameParts.size - 1]}${stringifyParams(params?.entries)}"
                val stats = rawMeasurements.stream().reduce(DistributionStatistics(),
                        { r, x -> DistributionStatistics.aggregate(r, x) },
                        { a, b -> DistributionStatistics.merge(a, b) })
                return BenchmarkTestResult(
                        packageName = packageName,
                        className = className,
                        methodName = methodName,
                        mode = mode ?: "",
                        numberOfTestThreads = numberOfTestThreads,
                        numberOfTestRepetitions = numberOfTestRepetitions,
                        numberOfWarmupIterations = numberOfWarmupIterations,
                        numberOfMeasurementIterations = numberOfMeasurementIterations,
                        warmupTimeInMilliseconds = warmupTimeInMilliseconds,
                        measurementTimeInMilliseconds = measurementTimeInMilliseconds,
                        scoreUnits = scoreUnits ?: "",
                        median = medianMeasurement,
                        firstQuarter = firstQuarterMeasurement,
                        thirdQuarter = thirdQuarterMeasurement,
                        meanErrorAt999 = meanErrorAt999,
                        distributionStatistics = stats,
                        rawMeasurements = rawMeasurements
                )
            }
        }
    }
}
