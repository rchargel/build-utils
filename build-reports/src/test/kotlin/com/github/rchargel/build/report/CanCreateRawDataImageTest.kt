package com.github.rchargel.build.report

import com.github.rchargel.build.report.chart.RawDataLineChartImageMaker
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.awt.Color

@RunWith(Parameterized::class)
class CanCreateRawDataImageTest(private val rawData: DoubleArray) {
    companion object {
        @Parameterized.Parameters
        @JvmStatic
        fun params() = listOf(
                doubleArrayOf(12.5, 6.2, 7.9, 8.1, 30.2, 0.16, -12.5, 8.9),
                doubleArrayOf(-100.4, -100.3, -100.2, -100.9, -100.7),
                doubleArrayOf(100.4, 100.3, 100.2, 100.9, 100.7)
        )

    }

    @Test
    fun testRawDataImage() {
        val image = RawDataLineChartImageMaker("Iteration #", "ms", rawData.size)
                .addDataset("Results", Color.blue, 1, rawData)
                .toImageBuilder(400, 200)
                .build()

        assert("image/gif" == image.contentType)
        assert(image.dataURL.startsWith("data:image/gif;base64,R0lGODlhkAHIAPcAAEBAQEFBQUJCQk"))
    }
}