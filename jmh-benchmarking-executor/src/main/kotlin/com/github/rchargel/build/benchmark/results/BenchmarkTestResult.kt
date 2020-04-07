package com.github.rchargel.build.benchmark.results

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.rchargel.build.common.DistributionStatistics
import org.apache.commons.lang3.StringUtils
import org.openjdk.jmh.results.RunResult
import org.openjdk.jmh.util.ListStatistics
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors.toMap
import kotlin.collections.HashMap

data class BenchmarkTestResult(
        val packageName: String,
        val className: String,
        val methodName: String,
        val mode: String,
        val numberOfTestThreads: Int,
        val numberOfTestRepetitions: Int,
        val numberOfWarmupIterations: Int,
        val numberOfMeasurementIterations: Int,
        val measurementTimeInMilliseconds: Long,
        val warmupTimeInMilliseconds: Long,
        val scoreUnits: String,
        val distributionStatistics: DistributionStatistics,
        val median: Double,
        val meanErrorAt999: Double,
        val rawMeasurements: List<Double>
) {
    @get:JsonIgnore
    val key: String
        get() = "$packageName.$className.$methodName - $mode"

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
                .meanErrorAt999(stats.getMeanErrorAt(0.999))
                .rawMeasurements(rawData)
                .build()
    }

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
                .meanErrorAt999(runResult.aggregatedResult.primaryResult.getStatistics().getMeanErrorAt(0.999))
                .rawMeasurements(runResult.aggregatedResult
                        .iterationResults
                        .map { it.primaryResult.getScore() })
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
                        meanErrorAt999 = meanErrorAt999,
                        distributionStatistics = stats,
                        rawMeasurements = rawMeasurements
                )
            }
        }
    }
}
