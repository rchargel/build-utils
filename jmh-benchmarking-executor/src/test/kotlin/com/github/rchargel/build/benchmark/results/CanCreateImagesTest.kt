package com.github.rchargel.build.benchmark.results

import com.github.rchargel.build.benchmark.report.BoxPlotChartImageMaker
import com.github.rchargel.build.benchmark.report.ECDFChartMaker
import com.github.rchargel.build.benchmark.report.NormalDistributionChartMaker
import org.jfree.data.statistics.BoxAndWhiskerItem
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
        val imagePrefix = "data:image/gif;base64,R0lGODlhLAHIAPcAAEBAQEFBQUJCQkNDQ0REREZGRkhISEpKSkx"
        val dataURL = image.dataURL
        assert(dataURL.startsWith(imagePrefix)) {
            "Should have started with $imagePrefix, but was $dataURL"
        }
    }

    @Test
    fun canCreateECDFImage() {
        val image = ECDFChartMaker("ms", "Cumulative Probability")
                .addDataset("Results", Color.blue, 1, rawData)
                .toImageBuilder(300, 200).build()

        assert("image/gif" == image.contentType)
        val imagePrefix = "data:image/gif;base64,R0lGODlhLAHIAPcAAEBAQEFBQUJCQkREREZGRkhISEp"
        val dataURL = image.dataURL
        assert(dataURL.startsWith(imagePrefix)) {
            "Should have started with $imagePrefix, but was $dataURL"
        }
    }

    @Test
    fun canCreateBoxPlotImage() {
        val result = BoxAndWhiskerItem(
                13.1,
                12.5,
                11.4,
                14.1,
                10.3,
                14.9,
                15.6,
                20.0,
                mutableListOf(15.6, 17.9, 17.9, 17.0, 17.9, 20.0)
        )
        val image = BoxPlotChartImageMaker("Executions", "ms")
                .addDataset("Results", Color.blue, 1, result)
                .toImageBuilder(300, 200).build()
        assert("image/gif" == image.contentType)
        val imagePrefix = "data:image/gif;base64,R0lGODlhLAHIAPcAAAAAAAICAgQEBAYGBgg"
        val dataURL = image.dataURL
        assert(dataURL.startsWith(imagePrefix)) {
            "Should have started with $imagePrefix, but was $dataURL"
        }
    }
}