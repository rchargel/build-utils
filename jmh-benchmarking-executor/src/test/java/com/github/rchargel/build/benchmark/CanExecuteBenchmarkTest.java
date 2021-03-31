package com.github.rchargel.build.benchmark;

import com.github.rchargel.build.benchmark.results.BenchmarkResults;

import com.fake.test.SimpleBenchmark;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class CanExecuteBenchmarkTest {
    @Test
    public void canExecuteBenchmark() throws Exception {
        final BenchmarkResults benchmarkResults = new BenchmarkExecutor()
                .executeBenchmarks(0.2, 2, Stream.of(SimpleBenchmark.class));
        assertEquals(1, benchmarkResults.getResults().size());
        assertEquals(2, benchmarkResults.getResults().iterator().next().getRawMeasurements().size());
        assertEquals(10, benchmarkResults.getResults().iterator().next().getAggregatedMeasurements().size());

        final BenchmarkResults compare = benchmarkResults.compareToBaseline(benchmarkResults, true);

        assertEquals(1, compare.getResults().size());
        assertEquals(10, compare.getResults().iterator().next().getBaselineAggregatedMeasurements().size());
    }
}
