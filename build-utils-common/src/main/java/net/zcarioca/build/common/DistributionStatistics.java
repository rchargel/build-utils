package net.zcarioca.build.common;

import java.io.Serializable;

public class DistributionStatistics implements Serializable {
    private static final long serialVersionUID = 1L;

    public final long count;
    public final double sum;
    public final double sumOfSquares;
    public final double mean;
    public final double variance;
    public final double standardDeviation;
    public final double skewness;
    public final double kurtosis;
    public final double minimum;
    public final double maximum;

    private final double m2;
    private final double m3;
    private final double m4;

    public DistributionStatistics() {
        this.count = 0;
        this.sum = 0;
        this.sumOfSquares = 0;
        this.mean = 0;
        this.variance = 0;
        this.standardDeviation = 0;
        this.skewness = 0;
        this.kurtosis = 0;
        this.m2 = 0;
        this.m3 = 0;
        this.m4 = 0;
        this.minimum = Double.POSITIVE_INFINITY;
        this.maximum = Double.NEGATIVE_INFINITY;
    }

    private DistributionStatistics(final long count, final double sum, final double sumOfSquares, final double mean, final double variance, final double skewness,
            final double kurtosis, final double minimum, final double maximum, final double m2, final double m3, final double m4) {
        super();
        this.count = count;
        this.sum = sum;
        this.sumOfSquares = sumOfSquares;
        this.mean = mean;
        this.variance = variance;
        this.standardDeviation = Math.sqrt(variance);
        this.skewness = skewness;
        this.kurtosis = kurtosis;
        this.minimum = minimum;
        this.maximum = maximum;
        this.m2 = m2;
        this.m3 = m3;
        this.m4 = m4;
    }

    public static DistributionStatistics aggregate(final DistributionStatistics moments, final double x) {
        final long count = moments.count + 1;
        final double sum = moments.sum + x;
        final double min = Math.min(moments.minimum, x);
        final double max = Math.max(moments.maximum, x);
        final double sumOfSquares = moments.sumOfSquares + x * x;
        final double delta = x - moments.mean;
        final double deltaOverCount = delta / count;
        final double deltaOverCountSquared = deltaOverCount * deltaOverCount;
        final double deltaSquaredOverCountTimesCountMinusOne = delta * deltaOverCount * moments.count;

        final double mean = sum / count;
        final double variance = (count * sumOfSquares - sum * sum) / (count * (count - 1));

        final double m4 = moments.m4 + deltaSquaredOverCountTimesCountMinusOne * deltaOverCountSquared * (count * count - 3 * count + 3)
                + 6 * deltaOverCountSquared * moments.m2 - 4 * deltaOverCount * moments.m3;
        final double m3 = moments.m3 + deltaSquaredOverCountTimesCountMinusOne * deltaOverCount * (count - 2) - 3 * deltaOverCount * moments.m2;
        final double m2 = moments.m2 + deltaSquaredOverCountTimesCountMinusOne;

        final double skewness = Math.sqrt(count) * m3 / Math.pow(m2, 3.0 / 2.0);
        final double kurtosis = count * m4 / (m2 * m2);

        return new DistributionStatistics(count, sum, sumOfSquares, mean, variance, skewness, kurtosis, min, max, m2, m3, m4);
    }

    public static DistributionStatistics merge(final DistributionStatistics r1, final DistributionStatistics r2) {
        final long count = r1.count + r2.count;

        // if both partitions are empty, return r1 without performing anymore
        // calculations
        if (count == 0)
            return r1;

        final double sum = r1.sum + r2.sum;
        final double sumOfSquares = r1.sumOfSquares + r2.sumOfSquares;

        final double delta = r2.mean - r1.mean;
        final double delta2 = delta * delta;
        final double delta3 = delta * delta2;
        final double delta4 = delta2 * delta2;

        final double mean = sum / count;

        final double variance = (count * sumOfSquares - sum * sum) / (count * (count - 1));

        final double m2 = r1.m2 + r2.m2 + delta2 * r1.count * r2.count / count;

        final double m3 = r1.m3 + r2.m3 + delta3 * r1.count * r2.count * (r1.count - r2.count) / (count * count)
                + 3.0 * delta * (r1.count * r2.m2 - r2.count * r1.m2) / count;

        final double m4 = r1.m4 + r2.m4
                + delta4 * r1.count * r2.count * (r1.count * r1.count - r1.count * r2.count + r2.count * r2.count)
                        / (count * count * count)
                + (6.0 * delta2 * (r1.count * r1.count * r2.m2 + r2.count * r2.count * r1.m2) / (count * count)
                        + 4.0 * delta * (r1.count * r2.m3 - r2.count * r1.m3) / count);

        final double skewness = Math.sqrt(count) * m3 / Math.pow(m2, 3.0 / 2.0);
        final double kurtosis = count * m4 / (m2 * m2);

        return new DistributionStatistics(count, sum, sumOfSquares, mean, variance, skewness, kurtosis,
                Math.min(r1.minimum, r2.minimum), Math.max(r1.maximum, r2.maximum), m2, m3, m4);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Double.hashCode(count);
        result = prime * result + Double.hashCode(sum);
        result = prime * result + Double.hashCode(sumOfSquares);
        result = prime * result + Double.hashCode(mean);
        result = prime * result + Double.hashCode(variance);
        result = prime * result + Double.hashCode(standardDeviation);
        result = prime * result + Double.hashCode(skewness);
        result = prime * result + Double.hashCode(kurtosis);
        result = prime * result + Double.hashCode(minimum);
        result = prime * result + Double.hashCode(maximum);
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
        final DistributionStatistics other = (DistributionStatistics) obj;
        if (count != other.count)
            return false;
        if (sum != other.sum)
            return false;
        if (sumOfSquares != other.sumOfSquares)
            return false;
        if (mean != other.mean)
            return false;
        if (variance != other.variance)
            return false;
        if (standardDeviation != other.standardDeviation)
            return false;
        if (skewness != other.skewness)
            return false;
        if (kurtosis != other.kurtosis)
            return false;
        if (minimum != other.minimum)
            return false;
        if (maximum != other.maximum)
            return false;
        return true;
    }

}
