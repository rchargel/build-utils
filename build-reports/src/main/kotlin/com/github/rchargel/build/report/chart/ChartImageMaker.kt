package com.github.rchargel.build.report.chart

import com.github.rchargel.build.report.Image
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.Range
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

abstract class ChartImageMaker<T : ChartImageMaker<T>>(
        xAxis: String,
        yAxis: String
) {
    protected val chart: JFreeChart

    init {
        chart = createChart(xAxis, yAxis)
    }

    protected abstract fun createChart(xAxisName: String, yAxisName: String): JFreeChart

    abstract fun addDataset(datasetName: String, color: Color, plotId: Int, data: Any): T

    @Throws(IOException::class)
    fun toImageBuilder(width: Int, height: Int): Image.Builder {
        val image = chart.createBufferedImage(width, height)
        val out = ByteArrayOutputStream()
        out.use {
            ImageIO.write(image, "GIF", it)
        }
        return Image.builder().data(out.toByteArray())
                .contentType("image/gif")
    }
}

abstract class XYChartImageMaker<T : XYChartImageMaker<T>>(xAxis: String, yAxis: String) : ChartImageMaker<T>(xAxis, yAxis) {
    override fun createChart(xAxisName: String, yAxisName: String) =
            ChartFactory.createXYLineChart(null, xAxisName, yAxisName, null)!!

    private val xyPlot: XYPlot = chart.xyPlot

    init {
        xyPlot.backgroundPaint = Color.white
        xyPlot.isDomainGridlinesVisible = false
        xyPlot.isRangeGridlinesVisible = false
        xyPlot.isOutlineVisible = false
    }

    override fun addDataset(datasetName: String, color: Color, plotId: Int, data: Any): T =
            addDataset(datasetName, color, plotId, xyPlot, data)

    protected abstract fun addDataset(datasetName: String, color: Color, plotId: Int, xyPlot: XYPlot, data: Any): T
}

class RawDataLineChartImageMaker(xAxis: String, units: String, private val maxCount: Int) : XYChartImageMaker<RawDataLineChartImageMaker>(xAxis, units) {
    private var rangeSet: Boolean = false

    override fun addDataset(datasetName: String, color: Color, plotId: Int, xyPlot: XYPlot, data: Any) = when (data) {
        is DoubleArray -> addDataset(datasetName, color, plotId, xyPlot, data as DoubleArray)
        is List<*> -> addDataset(datasetName, color, plotId, xyPlot, data.filterIsInstance(Number::class.java).map { it.toDouble() }.toDoubleArray())
        else -> throw IllegalArgumentException("Unable to create raw data chart for type ${data.javaClass}")
    }

    private fun addDataset(datasetName: String, color: Color, plotId: Int, xyPlot: XYPlot, data: DoubleArray) = apply {
        val series = XYSeries(datasetName)
        val length = data.size
        val step = maxCount / length.toDouble()

        IntRange(0, length - 1).forEach { i ->
            series.add(i * step, data[i])
        }

        val collection = XYSeriesCollection()
        collection.addSeries(series)
        xyPlot.setDataset(plotId, collection)
        val range = xyPlot.rangeAxis.range
        val minY = if (rangeSet) min(range.lowerBound, series.minY) else series.minY
        val maxY = if (rangeSet) max(range.upperBound, series.maxY) else series.maxY

        val tenPercentOfDiff = abs(maxY - minY) * 0.1
        xyPlot.rangeAxis.range = Range(minY - tenPercentOfDiff, maxY + tenPercentOfDiff)
        val renderer = XYLineAndShapeRenderer()
        renderer.setSeriesPaint(0, color)
        renderer.setSeriesShapesVisible(0, false)
        xyPlot.setRenderer(plotId, renderer)
    }
}
