package com.fake.benchmarks;

import java.util.concurrent.TimeUnit;

import org.mindrot.jbcrypt.BCrypt;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value = 1, warmups = 1)
@BenchmarkMode({ Mode.AverageTime })
@Warmup(iterations = 1, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ExampleBenchmark {

    @State(Scope.Benchmark)
    public static class ExecutionPlan {
        @Param({ "10", "12" })
        public int iterations;

        public String password = "testingPassword";

        public String hashedPassword;

        @Setup(Level.Trial)
        public void init() {
            hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(iterations));
        }
    }

    @Benchmark
    public void benchmarkGenerateSalt(final ExecutionPlan plan, final Blackhole bh) {
        bh.consume(BCrypt.gensalt(plan.iterations));
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkCheckPassword(final ExecutionPlan plan, final Blackhole bh) {
        bh.consume(BCrypt.checkpw(plan.password, plan.hashedPassword));
    }
}
