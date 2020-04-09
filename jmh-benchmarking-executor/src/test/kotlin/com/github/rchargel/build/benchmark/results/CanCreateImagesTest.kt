package com.github.rchargel.build.benchmark.results

import com.github.rchargel.build.benchmark.report.ECDFChartMaker
import com.github.rchargel.build.benchmark.report.NormalDistributionChartMaker
import org.junit.Test
import java.awt.Color

class CanCreateImagesTest {
    private val rawData: DoubleArray = doubleArrayOf(12.5, 6.2, 7.9, 8.1, 30.2, 0.16, -12.5, 8.9)

    @Test
    fun canCreateDistributionImage() {
        val image = NormalDistributionChartMaker("ms", "Probability", -12.5, 30.2)
                .addDataset("Results", Color.blue, 1, rawData)
                .toImageBuilder(300, 200).build()

        assert("image/gif" == image.contentType)
        assert(image.dataURL.startsWith("data:image/gif;base64,R0lGODlhLAHIAPcAAEBAQEFBQUJCQkREREZGRkhISEp"))
    }

    @Test
    fun canCreateECDFImage() {
        val image = ECDFChartMaker("ms", "Cumulative Probability")
                .addDataset("Results", Color.blue, 1, rawData)
                .toImageBuilder(300, 200).build()

        assert("image/gif" == image.contentType)
        assert(image.dataURL.startsWith("data:image/gif;base64,R0lGODlhLAHIAPcAAEBAQEFBQUJCQkREREZGRkhISEp"))
    }
}