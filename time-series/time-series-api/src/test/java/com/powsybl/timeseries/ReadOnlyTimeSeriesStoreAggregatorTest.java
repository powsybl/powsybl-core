package com.powsybl.timeseries;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ReadOnlyTimeSeriesStoreAggregatorTest {

    @Test
    public void test() {
        TestTimeSeriesIndex index = new TestTimeSeriesIndex(10000, 2);
        DoubleTimeSeries ts1 = StoredDoubleTimeSeries.create("ts1", index, new double[] {1d, 2d});
        DoubleTimeSeries ts2 = StoredDoubleTimeSeries.create("ts2", index, new double[] {3d, 4d});
        ReadOnlyTimeSeriesStore store1 = new ReadOnlyTimeSeriesStoreCache(ts1);
        ReadOnlyTimeSeriesStore store2 = new ReadOnlyTimeSeriesStoreCache(ts2);
        ReadOnlyTimeSeriesStore store12 = new ReadOnlyTimeSeriesStoreAggregator(store1, store2);
        assertEquals(Sets.newHashSet("ts2", "ts1"), store12.getTimeSeriesNames(null));
        assertTrue(store12.timeSeriesExists("ts1"));
        assertFalse(store12.timeSeriesExists("ts3"));
        assertEquals(new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index), store12.getTimeSeriesMetadata("ts1"));
        assertEquals(Arrays.asList(new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index),
                                   new TimeSeriesMetadata("ts2", TimeSeriesDataType.DOUBLE, index)),
                     store12.getTimeSeriesMetadata(Sets.newHashSet("ts1", "ts2")));
        assertEquals(Collections.emptySet(), store12.getTimeSeriesDataVersions());
        assertEquals(Collections.emptySet(), store12.getTimeSeriesDataVersions("ts1"));
        assertSame(ts1, store12.getDoubleTimeSeries("ts1", 1));
        assertNull(store12.getDoubleTimeSeries("ts3", 1));
        assertEquals(Arrays.asList(ts1, ts2), store12.getDoubleTimeSeries(Sets.newHashSet("ts1", "ts2"), 1));
        assertNull(store12.getStringTimeSeries("ts3", 1));
        assertTrue(store12.getStringTimeSeries(Collections.singleton("ts3"), 1).isEmpty());
    }
}
