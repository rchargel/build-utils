package com.github.rchargel.build.benchmark.results

import com.fasterxml.jackson.annotation.JsonIgnore
import oshi.SystemInfo

data class BenchmarkResults(
        val results: Collection<BenchmarkTestResult>? = null,
        val systemModel: String? = null,
        val operatingSystem: String? = null,
        val cpu: String? = null,
        val cpuSpeedInHertz: Long? = null,
        val architecture: String? = null,
        val physicalProcessors: Int? = null,
        val logicalProcessors: Int? = null,
        val totalMemoryInBytes: Long? = null,
        val swapTotalInBytes: Long? = null,
        val memoryPageSizeInBytes: Long? = null,
        val memoryBanks: List<MemoryBank> = emptyList(),
        val minAllowedPValue: Double = 0.05
) {
    @get:JsonIgnore
    val size: Long
        get() = results?.size?.toLong() ?: 0

    @get:JsonIgnore
    val hasPValueResults: Boolean
        get() = results?.mapNotNull { it.pvalue }?.count()?.or(0) != 0

    @get:JsonIgnore
    val passesPValueTest: Boolean
        get() = results?.mapNotNull { it.pvalue }?.filter { it < minAllowedPValue }?.count()?.or(0) == 0

    fun compareToBaseline(baseline: BenchmarkResults, validateSystemSpec: Boolean): BenchmarkResults {
        if (!validateSystemSpec ||
                architecture != baseline.architecture ||
                cpuSpeedInHertz != baseline.cpuSpeedInHertz ||
                logicalProcessors != baseline.logicalProcessors ||
                totalMemoryInBytes != baseline.totalMemoryInBytes ||
                swapTotalInBytes != baseline.swapTotalInBytes) {
            throw RuntimeException("System specifications have deviated from baseline")
        }
        val baselineMap = baseline.results?.map { it.key to it }?.toMap().orEmpty().toMutableMap()
        return BenchmarkResults(
                results?.map { it.compareWithBaseline(baselineMap.remove(it.key)) }.orEmpty(),
                systemModel,
                operatingSystem,
                cpu,
                cpuSpeedInHertz,
                architecture,
                physicalProcessors,
                logicalProcessors,
                totalMemoryInBytes,
                swapTotalInBytes,
                memoryPageSizeInBytes,
                memoryBanks,
                minAllowedPValue
        )
    }

    companion object {
        private fun architecture(is64: Boolean?): String? {
            if (is64 == null)
                return null
            return if (is64) "x86_64 (64 bit)" else "x86 (32 bit)"
        }

        @JvmStatic
        fun buildFromResults(results: Collection<BenchmarkTestResult>, minAllowedPValue: Double): BenchmarkResults {
            val sysInfo = SystemInfo()
            return BenchmarkResults(
                    results.toList(),
                    sysInfo.hardware?.computerSystem?.model,
                    sysInfo.operatingSystem?.toString(),
                    sysInfo.hardware?.processor?.processorIdentifier?.name,
                    sysInfo.hardware?.processor?.maxFreq,
                    architecture(sysInfo.hardware?.processor?.processorIdentifier?.isCpu64bit),
                    sysInfo.hardware?.processor?.physicalProcessorCount,
                    sysInfo.hardware?.processor?.logicalProcessorCount,
                    sysInfo.hardware?.memory?.total,
                    sysInfo.hardware?.memory?.virtualMemory?.swapTotal,
                    sysInfo.hardware?.memory?.pageSize,
                    sysInfo.hardware?.memory?.physicalMemory?.map {
                        MemoryBank(
                                it.memoryType,
                                it.bankLabel,
                                it.capacity,
                                it.clockSpeed
                        )
                    }.orEmpty(),
                    minAllowedPValue
            )
        }
    }
}

data class MemoryBank(
        val type: String? = null,
        val label: String? = null,
        val capacityInBytes: Long? = null,
        val clockSpeed: Long? = null
)