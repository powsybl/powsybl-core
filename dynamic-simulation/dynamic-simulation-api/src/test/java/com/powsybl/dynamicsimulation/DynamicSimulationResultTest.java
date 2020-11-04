/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.powsybl.timeseries.DoubleTimeSeries;
import com.powsybl.timeseries.IrregularTimeSeriesIndex;
import com.powsybl.timeseries.StringTimeSeries;
import com.powsybl.timeseries.TimeSeries;
import com.powsybl.timeseries.TimeSeriesDataType;
import com.powsybl.timeseries.TimeSeriesIndex;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationResultTest {

    @Test
    public void test() {
        Map <String, TimeSeries> curves = new HashMap<>();
        TimeSeriesIndex index = new IrregularTimeSeriesIndex(new long[] {32, 64, 128, 256});
        curves.put("NETWORK__BUS____2-BUS____5-1_AC_iSide2", TimeSeries.createDouble("NETWORK__BUS____2-BUS____5-1_AC_iSide2", index, 333.847331, 333.847321, 333.847300, 333.847259));
        curves.put("NETWORK__BUS____1_TN_Upu_value", TimeSeries.createDouble("NETWORK__BUS____1_TN_Upu_value", index, 1.059970, 1.059970, 1.059970, 1.059970));

        Map <String, TimeSeries> timeLine = new HashMap<>();
        index = new IrregularTimeSeriesIndex(new long[] {102479, 102479, 102479, 104396});
        timeLine.put("modelName", TimeSeries.createString("modelName", index, "CLA_2_5", "_BUS____2-BUS____5-1_AC", "CLA_2_5", "CLA_2_4"));
        timeLine.put("message", TimeSeries.createString("message", index, "CLA : order to change topology", "LINE : opening both sides", "CLA : order to change topology", "CLA : arming by over-current constraint"));
        DynamicSimulationResult result = new DynamicSimulationResultImpl(true, null, curves, timeLine);

        assertTrue(result.isOk());

        assertEquals(2, result.getCurves().size());
        assertEquals(TimeSeriesDataType.DOUBLE, result.getCurve("NETWORK__BUS____2-BUS____5-1_AC_iSide2").getMetadata().getDataType());
        assertArrayEquals(new double[] {333.847331, 333.847321, 333.847300, 333.847259}, ((DoubleTimeSeries) result.getCurve("NETWORK__BUS____2-BUS____5-1_AC_iSide2")).toArray(), 0);
        assertEquals(TimeSeriesDataType.DOUBLE, result.getCurve("NETWORK__BUS____1_TN_Upu_value").getMetadata().getDataType());
        assertArrayEquals(new double[] {1.059970, 1.059970, 1.059970, 1.059970}, ((DoubleTimeSeries) result.getCurve("NETWORK__BUS____1_TN_Upu_value")).toArray(), 0);

        assertEquals(2, result.getTimeLine().size());
        assertEquals(TimeSeriesDataType.STRING, result.getTimeLine("modelName").getMetadata().getDataType());
        assertArrayEquals(new String[] {"CLA_2_5", "_BUS____2-BUS____5-1_AC", "CLA_2_5", "CLA_2_4"}, ((StringTimeSeries) result.getTimeLine("modelName")).toArray());
        assertEquals(TimeSeriesDataType.STRING, result.getTimeLine("message").getMetadata().getDataType());
        assertArrayEquals(new String[] {"CLA : order to change topology", "LINE : opening both sides", "CLA : order to change topology", "CLA : arming by over-current constraint"}, ((StringTimeSeries) result.getTimeLine("message")).toArray());
    }
}
