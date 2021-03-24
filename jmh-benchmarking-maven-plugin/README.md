![Maven-Central](https://img.shields.io/maven-central/v/com.github.rchargel.maven/jmh-benchmarking-maven-plugin.svg)

# JMH Benchmarking Plugin

This plugin is a maven wrapper around the 
[Java Microbenchmarking Harness (JMH)](https://openjdk.java.net/projects/code-tools/jmh/). It 
allows the developer to annotate unit tests with basic annotations, and perform a 
statistical analysis of the performance of their implementation. 

**Note:** JMH evaluations are not intended to be run against end-to-end systems. Rather, its
purpose is to evaluate the performance of individual system calls, in order to aid 
developers in making implementation decisions.

## Creating Benchmark Evaluations

#### Maven Dependencies

The benchmark evaluation code itself will require their own dependencies. 
If the evaluations are written into `src/test/java`, then the following dependencies
can be scoped to `test`. The JMH Benchmarking Plugin will run benchmark evaluations
whether they are in `src/test/java` or `src/main/java`. 

    <dependency>
        <groupId>org.openjdk.jmh</groupId>
        <artifactId>jmh-core</artifactId>
        <version>1.28</version>
    </dependency>
    <dependency>
        <groupId>org.openjdk.jmh</groupId>
        <artifactId>jmh-generator-annprocess</artifactId>
        <version>1.28</version>
    </dependency>

#### Writing Evaluations

Benchmarks will only run from public classes, and the JMH Maven Plugin will search
for any classes annotated with the `@Benchmark` annotation.

    @Fork(value = 1, warmups = 2) // number of times to run benchmark and warmups
    @BenchmarkMode(Mode.AverageTime) // sets the mode to return time per operation
    public class MyBenchmark {
        
        @Benchmark
        public void timeVoidMethod(BlackHole bh) {
            // using the black hole helps prevent the likelihood of 
            // JIT Optimization skipping the execution of this method
            bh.consume(...)
        }
        
        @Benchmark
        @BenchmarkMode(Mode.Throughput) // changes mode to number of ops in unit of time
        public String timeReturningMethod() {
            return ...
        }
    }
    
The code replacing the `...` is the code which is being evaluated. There are many more
options and complex configurations available for benchmarking through JMH. The full 
scope of which is too large for this document. More information on writing more complex 
benchmark evaluations can be found at the 
[Open JDK](https://openjdk.java.net/projects/code-tools/jmh/) site. You can also find 
additional examples in the [integration tests](src/it) section of this build.

## Using the plugin

To run your benchmark evaluations as part of your build, add the plugin into your maven pom.

    <build>
        ...
        <plugins>
            ...
            <plugin>
                <groupId>com.github.rchargel.maven</groupId>
                <artifactId>jmh-benchmarking-maven-plugin</artifactId>
                <version>0.9</version>
                <executions>
                    <execution>
                        <id>run</id>
                        <goals>
                            <goal>run</goal>
                        </goals>
                        <configuration>
                            ...
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
        ...
    </build>
    
### Goals

* `run`: Runs the benchmark tests and produces a report in json and html format.
    
#### Run Goal Configuration Options

| Option                  | Description                                                                                                                                                                                   |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| outputDirectory         | The directory to output files into. Defaults to ${project.build.directory}/benchmark-results                                                                                                  |
| baselineRun             | The directory of an execution JSON file used to compare prior runs                                                                                                                            |
| maxAbsZScore            | The maximum number of standard deviations from the baseline execution's mean value before the test is considered a failure. Defaults to 1.5 (only used if `baselineRun` is provided)          |
| ignoreHardwareChanges   | Normally it won't be valid to compare benchmark runs if the hardware profile has changed. Set to `true` to ignore those changes. Defaults to `false` (only used if `baselineRun` is provided) |
| numberOfTestRepetitions | The number of times to repeat tests. This is in addition to setting the number of Iterations. Defaults to 1.                                                                                  |
| failBuildOnErrors       | Only used with `baselineRun`, this will fail the build if the absolute value of the Z-Score is greater than the `maxAbsZScore` value.                                                         |
| userLanguage            | Overrides the `user.language` system property                                                                                                                                                 |
| userCountry             | Overrides the `user.country` system property                                                                                                                                                  |

**Note**: It's worth stating that if comparing the current run to a baseline run, an increase in performance above the z-score will also
be considered a failure. This is because the developer should be able to explain changes in performance of any system. If you are intentionally 
improving the performance of your code, then you will be notified of success by the build being _in error_. A bit counter-intuitive, I know, 
but at that point, it would be up to the developers to reset the baseline run used to a new JSON file.

## Including the output in maven-site

To include benchmark results output into the normal maven site goal, add a reporting section to your pom.

    <reporting>
        <plugins>
            <plugin>
                <groupId>com.github.rchargel.maven</groupId>
                <artifactId>jmh-benchmarking-maven-plugin</artifactId>
                <version>0.9</version>
            </plugin>
        </plugins>
    </reporting>
    

