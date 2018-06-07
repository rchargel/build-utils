package net.zcarioca.build.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DistributionStatisticsTest {
    private static final double EPSILON = 0.000001;

    @Parameters(name = "List {0} -> mean {1} -> variance {2} -> skewness {3} -> kurtosis {4}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                { Arrays.asList(-1.0, 0.0, 1.0, 1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 4.0, 5.0, 5.0, 5.0, 6.0, 7.0, 9.0, 9.0, 9.0, 100.0), 8.75,
                        470.1973684, 4.0008104, 17.3822174128 },
                { Arrays.asList(-1.0, 0.0, 1.0, 1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 4.0, 5.0, 5.0, 5.0, 6.0, 7.0), 3.0, 5.2, 0, 2.03550295858 },
                { Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0), 4.5, 9.16666667, 0, 1.77575757576 },
                { Arrays.asList(70.0, 68.0, 12.0, 59.0, 16.0, 66.0, 47.0, 52.0, 64.0, 74.0), 52.8, 485.288889, -1.050393, 2.619318657 }
        });
    }

    @Parameter(0)
    public List<Double> values;

    @Parameter(1)
    public double expectedMean;

    @Parameter(2)
    public double expectedVariance;

    @Parameter(3)
    public double expectedSkewness;

    @Parameter(4)
    public double expectedKurtosis;

    @Test
    public void testMean() {
        final DistributionStatistics moments = values.stream().reduce(new DistributionStatistics(), DistributionStatistics::aggregate, DistributionStatistics::merge);
        assertEquals(expectedMean, moments.mean, EPSILON);
    }

    @Test
    public void testParallelMean() {
        final DistributionStatistics moments = values.parallelStream().reduce(new DistributionStatistics(), DistributionStatistics::aggregate, DistributionStatistics::merge);
        assertEquals(expectedMean, moments.mean, EPSILON);
    }

    @Test
    public void testVariance() {
        final DistributionStatistics moments = values.stream().reduce(new DistributionStatistics(), DistributionStatistics::aggregate, DistributionStatistics::merge);
        assertEquals(expectedVariance, moments.variance, EPSILON);
    }

    @Test
    public void testParallelVariance() {
        final DistributionStatistics moments = values.parallelStream().reduce(new DistributionStatistics(), DistributionStatistics::aggregate, DistributionStatistics::merge);
        assertEquals(expectedVariance, moments.variance, EPSILON);
    }

    @Test
    public void testSkewness() {
        final DistributionStatistics moments = values.stream().reduce(new DistributionStatistics(), DistributionStatistics::aggregate, DistributionStatistics::merge);
        assertEquals(expectedSkewness, moments.skewness, EPSILON);
    }

    @Test
    public void testParallelSkewness() {
        final DistributionStatistics moments = values.parallelStream().reduce(new DistributionStatistics(), DistributionStatistics::aggregate, DistributionStatistics::merge);
        assertEquals(expectedSkewness, moments.skewness, EPSILON);
    }

    @Test
    public void testKurtosis() {
        final DistributionStatistics moments = values.stream().reduce(new DistributionStatistics(), DistributionStatistics::aggregate, DistributionStatistics::merge);
        assertEquals(expectedKurtosis, moments.kurtosis, EPSILON);
    }

    @Test
    public void testParallelKurtosis() {
        final DistributionStatistics moments = values.parallelStream().reduce(new DistributionStatistics(), DistributionStatistics::aggregate, DistributionStatistics::merge);
        assertEquals(expectedKurtosis, moments.kurtosis, EPSILON);
    }

}
