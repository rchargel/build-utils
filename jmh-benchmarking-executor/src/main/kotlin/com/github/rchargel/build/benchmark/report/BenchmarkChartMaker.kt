package com.github.rchargel.build.benchmark.report

import com.github.rchargel.build.benchmark.results.BenchmarkTestResult
import com.github.rchargel.build.common.DistributionStatistics
import com.github.rchargel.build.report.chart.XYChartImageMaker
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.function.NormalDistributionFunction2D
import org.jfree.data.general.DatasetUtils
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.Color

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