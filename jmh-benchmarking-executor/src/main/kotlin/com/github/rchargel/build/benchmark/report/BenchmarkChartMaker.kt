package com.github.rchargel.build.benchmark.report

import com.github.rchargel.build.benchmark.results.BenchmarkTestResult
import com.github.rchargel.build.common.DistributionStatistics
import com.github.rchargel.build.report.Image
import com.github.rchargel.build.report.chart.ChartImageMaker
import com.github.rchargel.build.report.chart.XYChartImageMaker
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.Range
import org.jfree.data.function.NormalDistributionFunction2D
import org.jfree.data.general.DatasetUtils
import org.jfree.data.statistics.BoxAndWhiskerCalculator
import org.jfree.data.statistics.BoxAndWhiskerItem
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class ECDFChartMaker(scoreUnits: String, cumulativeText: String) : XYChartImageMaker<ECDFChartMaker>(scoreUnits, cumulativeText) {
    override fun addDataset(datasetName: String, color: Color, plotId: Int, xyPlot: XYPlot, data: Any) = when (data) {
        is DoubleArray -> addDataset(datasetName, color, plotId, xyPlot, data)
        is List<*> -> addDataset(datasetName, color, plotId, xyPlot, data.filterIsInstance(Number::class.java).map { it.toDouble() }.toDoubleArray())
        is BenchmarkTestResult -> addDataset(datasetName, color, plotId, xyPlot, data.rawMeasurements.toDoubleArray())
        else -> throw IllegalArgumentException("Unable to process data type ${data.javaClass}")
    }

    private fun addDataset(datasetName: String, color: Color, plotId: Int, xyPlot: XYPlot, data: DoubleArray) = apply {
        val series = XYSeries(datasetName)
        val length = data.size
        val sortedValues = data.sorted()

        IntRange(0, length - 1).forEach { i ->
            val expectedCumulativeProbability = (i + 1).toDouble() / length;
            series.add(sortedValues[i], expectedCumulativeProbability);
        };

        val dataset = XYSeriesCollection();
        dataset.addSeries(series);
        xyPlot.setDataset(plotId, dataset);
        val renderer = XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, color);
        renderer.setSeriesShapesVisible(0, false);
        xyPlot.setRenderer(plotId, renderer);

    }
}

class NormalDistributionChartMaker(
        scoreUnits: String,
        probability: String,
        private val min: Double,
        private val max: Double
) : XYChartImageMaker<NormalDistributionChartMaker>(scoreUnits, probability) {
    override fun addDataset(datasetName: String, color: Color, plotId: Int, xyPlot: XYPlot, data: Any) = when (data) {
        is DistributionStatistics -> addDataset(datasetName, color, plotId, xyPlot, data)
        is DoubleArray -> addDataset(datasetName, color, plotId, xyPlot, data)
        is List<*> -> addDataset(datasetName, color, plotId, xyPlot, data.filterIsInstance(Number::class.java).map { it.toDouble() }.toDoubleArray())
        is BenchmarkTestResult -> addDataset(datasetName, color, plotId, xyPlot, data.distributionStatistics)
        else -> throw IllegalArgumentException("Unable to process data type ${data.javaClass}")
    }

    private fun addDataset(datasetName: String, color: Color, plotId: Int, xyPlot: XYPlot, data: DoubleArray) =
            addDataset(datasetName, color, plotId, xyPlot, data.toList().stream().reduce(DistributionStatistics(),
                    { r, x -> DistributionStatistics.aggregate(r, x) },
                    { a, b -> DistributionStatistics.merge(a, b) }))

    private fun addDataset(datasetName: String, color: Color, plotId: Int, xyPlot: XYPlot, data: DistributionStatistics) = apply {
        val function = NormalDistributionFunction2D(data.mean, data.standardDeviation)
        val sampleFunction = DatasetUtils.sampleFunction2D(function, min, max, data.count.toInt(), datasetName)
        xyPlot.setDataset(plotId, sampleFunction)

        val renderer = XYLineAndShapeRenderer()
        renderer.setSeriesPaint(0, color)
        renderer.setSeriesShapesVisible(0, false)
        xyPlot.setRenderer(plotId, renderer)
    }
}

class BoxPlotChartImageMaker(private val name: String, units: String) : ChartImageMaker<BoxPlotChartImageMaker>(name, units) {
    private val categoryAxis = CategoryAxis(null)
    private val numberAxis = NumberAxis(units)
    private val dataset = DefaultBoxAndWhiskerCategoryDataset()
    private val renderer = BoxAndWhiskerRenderer()

    private val plotMap = HashMap<Int, Color>()
    private var min: Double = Double.POSITIVE_INFINITY
    private var max: Double = Double.NEGATIVE_INFINITY

    init {
        numberAxis.isAutoRange = false
        chart.categoryPlot.dataset = dataset
        chart.categoryPlot.domainAxis = categoryAxis
        chart.categoryPlot.rangeAxis = numberAxis
        chart.categoryPlot.renderer = renderer
        renderer.fillBox = false
        renderer.maximumBarWidth = 0.15
        renderer.defaultToolTipGenerator = BoxAndWhiskerToolTipGenerator()
        renderer.isMeanVisible = false
        renderer.defaultFillPaint = Color.white
    }

    override fun addDataset(datasetName: String, color: Color, plotId: Int, data: Any) = when (data) {
        is DoubleArray -> addDataset(datasetName, color, plotId, BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(data.toMutableList()))
        is List<*> -> addDataset(datasetName, color, plotId, BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(data.filterIsInstance(Number::class.java).map { it.toDouble() }.toMutableList()))
        is BoxAndWhiskerItem -> addDataset(datasetName, color, plotId, data)
        else -> throw IllegalArgumentException("Unable to create raw data chart for type ${data.javaClass}")
    }

    private fun addDataset(datasetName: String, color: Color, plotId: Int, data: BoxAndWhiskerItem) = apply {
        dataset.add(data, Key(plotId, datasetName), name)
        plotMap[plotId] = color

        min = min(min, data.minRegularValue.toDouble())
        min = min(min, data.minOutlier.toDouble())
        max = max(max, data.maxRegularValue.toDouble())
        max = max(max, data.maxOutlier.toDouble())
    }

    override fun toImageBuilder(width: Int, height: Int): Image.Builder {
        plotMap.entries.sortedBy { it.key }.forEachIndexed { i, e ->
            val color = e.value
            renderer.setSeriesPaint(i, color)
            renderer.setSeriesFillPaint(i, color)
            renderer.setSeriesOutlinePaint(i, color)
        }
        val mult = (max - min) * 0.1
        numberAxis.range = Range(min - mult, max + mult)
        return super.toImageBuilder(width, height)
    }

    override fun createChart(xAxisName: String, yAxisName: String) = JFreeChart(
            null,
            JFreeChart.DEFAULT_TITLE_FONT,
            CategoryPlot(),
            true
    )

    private data class Key(val k: Int, val v: String) : Comparable<Key> {
        override fun compareTo(other: Key) = k - other.k
        override fun toString() = v
    }
}

