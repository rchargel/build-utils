package com.github.rchargel.build.benchmark.results

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.rchargel.build.common.DistributionStatistics
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.EMPTY
import org.openjdk.jmh.results.RunResult
import org.openjdk.jmh.util.ListStatistics
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors.toMap
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
        val mean: Double = 0.0,
        val meanErrorAt999: Double = 0.0,
        val min: Double = 0.0,
        val max: Double = 0.0,
        val firstQuarter: Double = 0.0,
        val thirdQuarter: Double = 0.0,
        val rawMeasurements: List<List<Double>> = emptyList(),
        val baselineDistributionStatistics: DistributionStatistics? = null,
        val baselineMedian: Double? = null,
        val baselineMean: Double? = null,
        val baselineMin: Double? = null,
        val baselineMax: Double? = null,
        val baselineFirstQuarter: Double? = null,
        val baselineThirdQuarter: Double? = null,
        val baselineMeasurements: List<List<Double>>? = null
) {
    @get:JsonIgnore
    val key: String
        get() = "$packageName.$className.$methodName - $mode"

    @get:JsonIgnore
    val outlierMinimum: Double
        get() = firstQuarter - abs(thirdQuarter - firstQuarter) * 1.5

    @get:JsonIgnore
    val outlierMaximum: Double
        get() = thirdQuarter + abs(thirdQuarter - firstQuarter) * 1.5

    @get:JsonIgnore
    val aggregatedMeasurements: List<Double>
        get() = rawMeasurements.flatten()

    @get:JsonIgnore
    val stripOutliers: List<Double>
        get() = aggregatedMeasurements.filter { it in outlierMinimum..outlierMaximum }

    @get:JsonIgnore
    val baselineOutlierMinimum: Double
        get() = if (baselineFirstQuarter != null)
            baselineFirstQuarter - abs((baselineThirdQuarter ?: 0.0) - (baselineFirstQuarter ?: 0.0)) * 1.5
        else Double.NaN

    @get:JsonIgnore
    val baselineOutlierMaximum: Double
        get() = if (baselineThirdQuarter != null)
            baselineThirdQuarter + abs((baselineThirdQuarter) - (baselineFirstQuarter ?: 0.0)) * 1.5
        else Double.NaN

    @get:JsonIgnore
    val baselineAggregatedMeasurements: List<Double>?
        get() = baselineMeasurements?.flatten()

    @get:JsonIgnore
    val baselineStripOutliers: List<Double>?
        get() = baselineAggregatedMeasurements?.filter { it in baselineOutlierMinimum..baselineOutlierMaximum }

    @get:JsonIgnore
    val zScore: Double?
        get() = if (baselineMean != null && baselineDistributionStatistics != null)
            (mean - baselineMean) / baselineDistributionStatistics.standardDeviation
        else null

    @get:JsonIgnore
    val hasBaselineComparison: Boolean
        get() = baselineMeasurements?.isEmpty() == false

    fun shortString() = mapOf(
            "min" to min,
            "out-min" to outlierMinimum,
            "1qtr" to firstQuarter,
            "media" to median,
            "3qtr" to thirdQuarter,
            "out-max" to outlierMaximum,
            "max" to max
    ).toString()

    fun failsZScore(maxAbsoluteZScore: Double) = if (zScore != null) abs(zScore ?: 0.0) > maxAbsoluteZScore else false

    fun merge(other: BenchmarkTestResult): BenchmarkTestResult {
        val rawData = this.rawMeasurements + other.rawMeasurements
        val stats = ListStatistics(rawData.flatten().toDoubleArray())
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
                .minimum(stats.min)
                .maximum(stats.max)
                .firstQuarterMeasurement(stats.getPercentile(25.0))
                .thirdQuarterMeasurement(stats.getPercentile(75.0))
                .mean(stats.mean)
                .meanErrorAt999(stats.getMeanErrorAt(0.999))
                .rawMeasurements(rawData)
                .build()
    }

    fun compareWithBaseline(baseline: BenchmarkTestResult?) = if (baseline == null || baseline.key == key)
        BenchmarkTestResult(
                packageName = packageName,
                className = className,
                methodName = methodName,
                mode = mode,
                numberOfTestThreads = numberOfTestThreads,
                numberOfTestRepetitions = numberOfTestRepetitions,
                numberOfWarmupIterations = numberOfWarmupIterations,
                numberOfMeasurementIterations = numberOfMeasurementIterations,
                measurementTimeInMilliseconds = measurementTimeInMilliseconds,
                warmupTimeInMilliseconds = warmupTimeInMilliseconds,
                scoreUnits = scoreUnits,
                distributionStatistics = distributionStatistics,
                median = median,
                mean = mean,
                min = min,
                max = max,
                firstQuarter = firstQuarter,
                thirdQuarter = thirdQuarter,
                meanErrorAt999 = meanErrorAt999,
                rawMeasurements = rawMeasurements,
                baselineDistributionStatistics = baseline?.distributionStatistics ?: DistributionStatistics(),
                baselineMedian = baseline?.median ?: Double.NaN,
                baselineMean = baseline?.mean ?: Double.NaN,
                baselineMin = baseline?.min,
                baselineMax = baseline?.max,
                baselineFirstQuarter = baseline?.firstQuarter,
                baselineThirdQuarter = baseline?.thirdQuarter,
                baselineMeasurements = baseline?.rawMeasurements.orEmpty()
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
                .mean(runResult.aggregatedResult.primaryResult.getStatistics().mean)
                .minimum(runResult.aggregatedResult.primaryResult.getStatistics().min)
                .maximum(runResult.aggregatedResult.primaryResult.getStatistics().max)
                .meanErrorAt999(runResult.aggregatedResult.primaryResult.getStatistics().getMeanErrorAt(0.999))
                .rawMeasurements(runResult.benchmarkResults.map { run -> run.iterationResults.map { it.primaryResult.getScore() } })
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
                private var mean: Double = 0.0,
                private var meanErrorAt999: Double = 0.0,
                private var minimum: Double = 0.0,
                private var maximum: Double = 0.0,
                private var rawMeasurements: ArrayList<List<Double>> = ArrayList(),
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
            fun mean(mean: Double) = apply { this.mean = mean }
            fun maximum(maximum: Double) = apply { this.maximum = maximum }
            fun minimum(minimum: Double) = apply { this.minimum = minimum }
            fun meanErrorAt999(meanErrorAt999: Double) = apply { this.meanErrorAt999 = meanErrorAt999 }
            fun addRawMeasurement(rawMeasurement: List<Double>) = apply { this.rawMeasurements.add(rawMeasurement) }
            fun rawMeasurements(rawMeasurements: List<List<Double>>) = apply { this.rawMeasurements = ArrayList(rawMeasurements) }
            fun addParam(name: String, value: String) = apply { this.params[name] = value }
            fun params(params: Map<String, String>) = apply { this.params = HashMap(params) }

            fun build(): BenchmarkTestResult {
                val nameParts = name.split(".")
                val packageName = internalPackageName ?: StringUtils.join(nameParts.subList(0, nameParts.size - 2), ".")
                val className = internalClassName ?: nameParts[nameParts.size - 2]
                val methodName = internalMethodName
                        ?: "${nameParts[nameParts.size - 1]}${stringifyParams(params.entries)}"
                val stats = rawMeasurements.flatten().stream().reduce(DistributionStatistics(),
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
                        mean = mean,
                        min = minimum,
                        max = maximum,
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
