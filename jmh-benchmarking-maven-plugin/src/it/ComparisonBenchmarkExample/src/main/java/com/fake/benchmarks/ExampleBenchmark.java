package com.fake.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 50, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ExampleBenchmark {

    @Benchmark
    public void benchmarkDistance(final Data data, final Blackhole bh) {
        final double dx = Math.abs(data.x2 - data.x1);
        final double dy = Math.abs(data.y2 - data.y1);
        bh.consume(Math.sqrt((dx * dx) + (dy * dy)));
    }

    @State(Scope.Thread)
    public static class Data {

        double x1 = 0.0;
        double y1 = 0.0;
        double x2 = 0.0;
        double y2 = 0.0;

        @Setup(Level.Iteration)
        public void prepare() {
            final Random random = new Random();
            x1 = random.nextDouble();
            y1 = random.nextDouble();
            x2 = random.nextDouble();
            y2 = random.nextDouble();
        }

        @TearDown(Level.Iteration)
        public void shutdown() {
            // useless in this benchmark...
        }
    }
}
