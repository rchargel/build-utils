package com.github.rchargel.build.report

import com.github.rchargel.build.report.chart.RawDataLineChartImageMaker
import org.junit.Test
import java.awt.Color

class CanCreateRawDataImageTest {
    private val rawData: DoubleArray = doubleArrayOf(12.5, 6.2, 7.9, 8.1, 30.2, 0.16, -12.5, 8.9)

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