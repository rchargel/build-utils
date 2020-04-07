package com.github.rchargel.build.benchmark.results

import oshi.SystemInfo

data class BenchmarkResults(
        val results: Collection<BenchmarkTestResult>? = null,
        val systemModel: String? = null,
        val operatingSystem: String? = null,
        val cpu: String? = null,
        val architecture: String? = null,
        val physicalProcessors: Int? = null,
        val logicalProcessors: Int? = null,
        val totalMemoryInBytes: Long? = null,
        val swapTotalInBytes: Long? = null
) {
    val size: Long
        get() = results?.size?.toLong() ?: 0

    companion object {
        private fun architecture(is64: Boolean?): String? {
            if (is64 == null)
                return null
            return if (is64) "x86_64 (64 bit)" else "x86 (32 bit)"
        }

        @JvmStatic
        fun buildFromResults(results: Collection<BenchmarkTestResult>): BenchmarkResults {
            val sysInfo = SystemInfo()
            return BenchmarkResults(
                    results.toList(),
                    sysInfo?.hardware?.computerSystem?.model,
                    sysInfo?.operatingSystem?.toString(),
                    sysInfo?.hardware?.processor?.name,
                    architecture(sysInfo?.hardware?.processor?.isCpu64bit),
                    sysInfo?.hardware?.processor?.physicalProcessorCount,
                    sysInfo?.hardware?.processor?.logicalProcessorCount,
                    sysInfo?.hardware?.memory?.total,
                    sysInfo?.hardware?.memory?.swapTotal
            )
        }
    }
}