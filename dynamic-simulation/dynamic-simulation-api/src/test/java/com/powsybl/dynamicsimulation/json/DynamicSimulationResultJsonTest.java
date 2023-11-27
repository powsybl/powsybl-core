/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.json;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.timeseries.RegularTimeSeriesIndex;
import com.powsybl.timeseries.StringTimeSeries;
import com.powsybl.timeseries.TimeSeries;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class DynamicSimulationResultJsonTest extends AbstractSerDeTest {

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
    void roundTripTest() throws IOException {
        roundTripTest(create(), DynamicSimulationResultSerializer::write, DynamicSimulationResultDeserializer::read, "/DynamicSimulationResult.json");
    }

    @Test
    void handleErrorTest() throws IOException {
        try (var is = getClass().getResourceAsStream("/DynamicSimulationResultError.json")) {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> DynamicSimulationResultDeserializer.read(is));
            assertEquals("Unexpected field: metrics", e.getMessage());
        }
    }

}
