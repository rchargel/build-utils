package net.zcarioca.maven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.util.ListStatistics;
import org.openjdk.jmh.util.Statistics;

public class BenchmarkTestResult implements StatisticalSummary {

    private static final int MEDIAN_PERCENTILE = 50;
    private static final double CONFIDENCE_INTERVAL = 0.999;

    private String packageName;
    private String className;
    private String methodName;
    private String mode;
    private int numberOfTestThreads;
    private int numberOfTestRepetitions;
    private int numberOfWarmupIterations;
    private int numberOfMeasurementIterations;
    private long measurementTimeInMilliseconds;
    private long warmupTimeInMilliseconds;
    private String scoreUnits;
    private double min;
    private double max;
    private double mean;
    private double median;
    private double meanErrorAt999;
    private double standardDeviation;
    private double variance;
    private double sum;
    private List<Double> rawMeasurements;

    private BenchmarkTestResult() {

    }

    private BenchmarkTestResult(final Builder builder) {
        this.numberOfTestThreads = builder.numberOfTestThreads;
        this.numberOfTestRepetitions = builder.numberOfTestRepetitions;
        this.methodName = builder.methodName;
        this.className = builder.className;
        this.packageName = builder.packageName;
        this.mode = builder.mode;
        this.numberOfWarmupIterations = builder.numberOfWarmupIterations;
        this.numberOfMeasurementIterations = builder.numberOfMeasurementIterations;
        this.measurementTimeInMilliseconds = builder.measurementTimeInMilliseconds;
        this.warmupTimeInMilliseconds = builder.warmupTimeInMilliseconds;
        this.scoreUnits = builder.scoreUnits;
        this.min = builder.minimumMeasurement;
        this.max = builder.maximumMeasurement;
        this.mean = builder.meanMeasurement;
        this.median = builder.medianMeasurement;
        this.meanErrorAt999 = builder.meanErrorAt999;
        this.standardDeviation = builder.standardDeviation;
        this.variance = builder.variance;
        this.sum = builder.sum;
        this.rawMeasurements = builder.rawMeasurements;
    }

    private BenchmarkTestResult(final RunResult runResult) {
        this.numberOfTestThreads = runResult.getParams().getThreads();
        final String[] nameParts = runResult.getParams().getBenchmark().split("\\.");
        final Map<String, String> params = runResult.getParams().getParamsKeys().stream().collect(Collectors.toMap(Function.identity(), paramKey -> {
            return runResult.getParams().getParam(paramKey);
        }));
        this.numberOfTestRepetitions = 1;
        this.methodName = nameParts[nameParts.length - 1] + stringifyParams(params.entrySet());
        this.className = nameParts[nameParts.length - 2];
        this.packageName = StringUtils.join(Arrays.asList(nameParts).subList(0, nameParts.length - 2), ".");
        numberOfWarmupIterations = runResult.getParams().getWarmup().getCount();
        numberOfMeasurementIterations = runResult.getParams().getMeasurement().getCount();
        measurementTimeInMilliseconds = runResult.getParams().getMeasurement().getTime().convertTo(TimeUnit.MILLISECONDS);
        warmupTimeInMilliseconds = runResult.getParams().getWarmup().getTime().convertTo(TimeUnit.MILLISECONDS);

        mode = runResult.getParams().getMode().toString();
        scoreUnits = runResult.getAggregatedResult().getScoreUnit();
        min = runResult.getAggregatedResult().getPrimaryResult().getStatistics().getMin();
        max = runResult.getAggregatedResult().getPrimaryResult().getStatistics().getMax();
        mean = runResult.getAggregatedResult().getPrimaryResult().getStatistics().getMean();
        median = runResult.getAggregatedResult().getPrimaryResult().getStatistics().getPercentile(50);
        meanErrorAt999 = runResult.getAggregatedResult().getPrimaryResult().getStatistics().getMeanErrorAt(.999);
        standardDeviation = runResult.getAggregatedResult().getPrimaryResult().getStatistics().getStandardDeviation();
        variance = runResult.getAggregatedResult().getPrimaryResult().getStatistics().getVariance();
        sum = runResult.getAggregatedResult().getPrimaryResult().getStatistics().getSum();

        rawMeasurements = Collections.unmodifiableList(runResult.getAggregatedResult()
                .getIterationResults()
                .stream()
                .map(iterationResult -> Double.valueOf(iterationResult.getPrimaryResult().getScore()))
                .collect(Collectors.toList()));
    }

    public BenchmarkTestResult merge(final BenchmarkTestResult other) {
        final List<Double> rawData = new ArrayList<>(this.rawMeasurements);
        rawData.addAll(other.rawMeasurements);

        final Statistics stats = new ListStatistics(rawData.stream().mapToDouble(Double::doubleValue).toArray());

        return new Builder()
                .numberOfTestThreads(this.numberOfTestThreads)
                .numberOfTestRepetitions(this.numberOfTestRepetitions + other.numberOfTestRepetitions)
                .methodName(this.methodName)
                .className(this.className)
                .packageName(this.packageName)
                .numberOfWarmupIterations(this.numberOfWarmupIterations)
                .numberOfMeasurementIterations(this.numberOfMeasurementIterations)
                .measurementTimeInMilliseconds(this.measurementTimeInMilliseconds)
                .warmupTimeInMilliseconds(this.warmupTimeInMilliseconds)
                .mode(this.mode)
                .scoreUnits(this.scoreUnits)
                .maximumMeasurement(Math.max(this.max, other.max))
                .minimumMeasurement(Math.min(this.min, other.min))
                .meanMeasurement(stats.getMean())
                .medianMeasurement(stats.getPercentile(MEDIAN_PERCENTILE))
                .meanErrorAt999(stats.getMeanErrorAt(CONFIDENCE_INTERVAL))
                .standardDeviation(stats.getStandardDeviation())
                .variance(stats.getVariance())
                .sum(stats.getSum())
                .rawMeasurements(Collections.unmodifiableList(rawData))
                .build();
    }

    public String getKey() {
        return String.format("%s.%s.%s - %s", packageName, className, methodName, mode);
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMode() {
        return mode;
    }

    public int getNumberOfWarmupIterations() {
        return numberOfWarmupIterations;
    }

    public int getNumberOfMeasurementIterations() {
        return numberOfMeasurementIterations;
    }

    public int getNumberOfTestThreads() {
        return numberOfTestThreads;
    }

    public int getNumberOfTestRepetitions() {
        return numberOfTestRepetitions;
    }

    @Override
    public long getN() {
        return getRawMeasurements().size();
    }

    public long getMeasurementTimeInMilliseconds() {
        return measurementTimeInMilliseconds;
    }

    public long getWarmupTimeInMilliseconds() {
        return warmupTimeInMilliseconds;
    }

    public String getScoreUnits() {
        return scoreUnits;
    }

    @Override
    public double getMin() {
        return min;
    }

    @Override
    public double getMax() {
        return max;
    }

    @Override
    public double getMean() {
        return mean;
    }

    public double getMedian() {
        return median;
    }

    public double getMeanErrorAt999() {
        return meanErrorAt999;
    }

    @Override
    public double getStandardDeviation() {
        return standardDeviation;
    }

    @Override
    public double getVariance() {
        return variance;
    }

    @Override
    public double getSum() {
        return sum;
    }

    public List<Double> getRawMeasurements() {
        return rawMeasurements;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(max);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(meanErrorAt999);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(mean);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(median);
        result = prime * result + (int) (temp ^ temp >>> 32);
        result = prime * result + (int) (measurementTimeInMilliseconds ^ measurementTimeInMilliseconds >>> 32);
        result = prime * result + (int) (warmupTimeInMilliseconds ^ warmupTimeInMilliseconds >>> 32);
        temp = Double.doubleToLongBits(min);
        result = prime * result + (int) (temp ^ temp >>> 32);
        result = prime * result + (mode == null ? 0 : mode.hashCode());
        result = prime * result + (packageName == null ? 0 : packageName.hashCode());
        result = prime * result + (className == null ? 0 : className.hashCode());
        result = prime * result + (methodName == null ? 0 : methodName.hashCode());
        result = prime * result + numberOfTestThreads;
        result = prime * result + numberOfTestRepetitions;
        result = prime * result + numberOfMeasurementIterations;
        result = prime * result + numberOfWarmupIterations;
        result = prime * result + (rawMeasurements == null ? 0 : rawMeasurements.hashCode());
        result = prime * result + (scoreUnits == null ? 0 : scoreUnits.hashCode());
        temp = Double.doubleToLongBits(standardDeviation);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(sum);
        result = prime * result + (int) (temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(variance);
        result = prime * result + (int) (temp ^ temp >>> 32);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BenchmarkTestResult other = (BenchmarkTestResult) obj;
        if (!Objects.equals(packageName, other.packageName))
            return false;
        if (!Objects.equals(className, other.className))
            return false;
        if (!Objects.equals(methodName, other.methodName))
            return false;
        if (!Objects.equals(mode, other.mode))
            return false;
        if (numberOfTestThreads != other.numberOfTestThreads)
            return false;
        if (numberOfTestRepetitions != other.numberOfTestRepetitions)
            return false;
        if (numberOfWarmupIterations != other.numberOfWarmupIterations)
            return false;
        if (numberOfMeasurementIterations != other.numberOfMeasurementIterations)
            return false;
        if (measurementTimeInMilliseconds != other.measurementTimeInMilliseconds)
            return false;
        if (warmupTimeInMilliseconds != other.warmupTimeInMilliseconds)
            return false;
        if (!Objects.equals(scoreUnits, other.scoreUnits))
            return false;
        if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min))
            return false;
        if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max))
            return false;
        if (Double.doubleToLongBits(mean) != Double.doubleToLongBits(other.mean))
            return false;
        if (Double.doubleToLongBits(median) != Double.doubleToLongBits(other.median))
            return false;
        if (Double.doubleToLongBits(meanErrorAt999) != Double.doubleToLongBits(other.meanErrorAt999))
            return false;
        if (Double.doubleToLongBits(standardDeviation) != Double.doubleToLongBits(other.standardDeviation))
            return false;
        if (Double.doubleToLongBits(variance) != Double.doubleToLongBits(other.variance))
            return false;
        if (Double.doubleToLongBits(sum) != Double.doubleToLongBits(other.sum))
            return false;
        if (!Objects.equals(rawMeasurements, other.rawMeasurements))
            return false;

        return true;
    }

    public static List<BenchmarkTestResult> build(final Collection<RunResult> results) {
        return results.stream()
                .map(BenchmarkTestResult::new)
                .collect(Collectors.toList());
    }

    static String stringifyParams(final Set<Entry<String, String>> paramEntries) {
        if (paramEntries == null || paramEntries.isEmpty()) {
            return "";
        }
        final List<String> params = paramEntries.stream()
                .sorted(Comparator.comparing(Entry::getKey))
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.toList());
        return " [ " + StringUtils.join(params, ", ") + " ]";
    }

    public static class Builder {

        private String name;
        private String methodName;
        private String className;
        private String packageName;
        private String mode;
        private int numberOfTestThreads = 1;
        private int numberOfTestRepetitions = 1;
        private int numberOfWarmupIterations;
        private int numberOfMeasurementIterations;
        private long measurementTimeInMilliseconds;
        private long warmupTimeInMilliseconds;
        private String scoreUnits;
        private double minimumMeasurement;
        private double maximumMeasurement;
        private double meanMeasurement;
        private double medianMeasurement;
        private double meanErrorAt999;
        private double standardDeviation;
        private double variance;
        private double sum;
        private List<Double> rawMeasurements = new ArrayList<>();
        private Map<String, String> params = new HashMap<>();

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder mode(final String mode) {
            this.mode = mode;
            return this;
        }

        public Builder numberOfTestThreads(final int numberOfTestThreads) {
            this.numberOfTestThreads = numberOfTestThreads;
            return this;
        }

        public Builder numberOfTestRepetitions(final int numberOfTestRepetitions) {
            this.numberOfTestRepetitions = numberOfTestRepetitions;
            return this;
        }

        public Builder numberOfWarmupIterations(final int numberOfWarmupIterations) {
            this.numberOfWarmupIterations = numberOfWarmupIterations;
            return this;
        }

        public Builder numberOfMeasurementIterations(final int numberOfMeasurementIterations) {
            this.numberOfMeasurementIterations = numberOfMeasurementIterations;
            return this;
        }

        public Builder measurementTimeInMilliseconds(final long measurementTimeInMilliseconds) {
            this.measurementTimeInMilliseconds = measurementTimeInMilliseconds;
            return this;
        }

        public Builder warmupTimeInMilliseconds(final long warmupTimeInMilliseconds) {
            this.warmupTimeInMilliseconds = warmupTimeInMilliseconds;
            return this;
        }

        public Builder scoreUnits(final String scoreUnits) {
            this.scoreUnits = scoreUnits;
            return this;
        }

        public Builder minimumMeasurement(final double minimumMeasurement) {
            this.minimumMeasurement = minimumMeasurement;
            return this;
        }

        public Builder maximumMeasurement(final double maximumMeasurement) {
            this.maximumMeasurement = maximumMeasurement;
            return this;
        }

        public Builder meanMeasurement(final double meanMeasurement) {
            this.meanMeasurement = meanMeasurement;
            return this;
        }

        public Builder medianMeasurement(final double medianMeasurement) {
            this.medianMeasurement = medianMeasurement;
            return this;
        }

        public Builder meanErrorAt999(final double meanErrorAt999) {
            this.meanErrorAt999 = meanErrorAt999;
            return this;
        }

        public Builder standardDeviation(final double standardDeviation) {
            this.standardDeviation = standardDeviation;
            return this;
        }

        public Builder variance(final double variance) {
            this.variance = variance;
            return this;
        }

        public Builder sum(final double sum) {
            this.sum = sum;
            return this;
        }

        public Builder rawMeasurements(final List<Double> rawMeasurements) {
            this.rawMeasurements = rawMeasurements;
            return this;
        }

        public Builder addRawMeasurement(final double value) {
            this.rawMeasurements.add(value);
            return this;
        }

        public Builder params(final Map<String, String> params) {
            this.params = params;
            return this;
        }

        public Builder addParam(final String key, final String value) {
            this.params.put(key, value);
            return this;
        }

        private Builder methodName(final String methodName) {
            this.methodName = methodName;
            return this;
        }

        private Builder className(final String className) {
            this.className = className;
            return this;
        }

        private Builder packageName(final String packageName) {
            this.packageName = packageName;
            return this;
        }

        public BenchmarkTestResult build() {
            if (methodName == null) {
                final String[] nameParts = name.split("\\.");
                this.methodName = nameParts[nameParts.length - 1] + stringifyParams(params.entrySet());
                this.className = nameParts[nameParts.length - 2];
                this.packageName = StringUtils.join(Arrays.asList(nameParts).subList(0, nameParts.length - 2), ".");
            }
            return new BenchmarkTestResult(this);
        }
    }
}
