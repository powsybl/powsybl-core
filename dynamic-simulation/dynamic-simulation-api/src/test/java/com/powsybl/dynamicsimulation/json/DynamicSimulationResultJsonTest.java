/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.timeseries.RegularTimeSeriesIndex;
import com.powsybl.timeseries.StringTimeSeries;
import com.powsybl.timeseries.TimeSeries;

import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationResultJsonTest extends AbstractConverterTest {

    private static DynamicSimulationResult create() {
        return new DynamicSimulationResult() {
            @Override
            public boolean isOk() {
                return true;
            }

            @Override
            public String getLogs() {
                return "";
            }

            @Override
            public Map<String, TimeSeries> getCurves() {
                return Collections.singletonMap("curve1", TimeSeries.createDouble("curve1", new RegularTimeSeriesIndex(0, 5, 1), 0.0, 0.1, 0.1, 0.2, 0.1, 0.0));
            }

            @Override
            public TimeSeries getCurve(String curve) {
                return null;
            }

            @Override
            public StringTimeSeries getTimeLine() {
                return DynamicSimulationResult.emptyTimeLine();
            }
        };
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), DynamicSimulationResultSerializer::write, DynamicSimulationResultDeserializer::read, "/DynamicSimulationResult.json");
    }

    @Test
    public void handleErrorTest() throws IOException {
        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: metrics");
        DynamicSimulationResultDeserializer.read(getClass().getResourceAsStream("/DynamicSimulationResultError.json"));
    }

}
