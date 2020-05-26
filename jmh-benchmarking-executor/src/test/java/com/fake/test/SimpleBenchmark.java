package com.fake.test;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class SimpleBenchmark {
    @Benchmark
    public int benchmarkSum(final Data data) {
        return data.x + data.y;
    }

    @State(Scope.Thread)
    public static class Data {
        int x = 0;
        int y = 0;

        @Setup(Level.Iteration)
        public void setup() {
            final Random r = new Random(2342826596719L);
            x = r.nextInt(100);
            y = r.nextInt(100);
        }
    }
}
