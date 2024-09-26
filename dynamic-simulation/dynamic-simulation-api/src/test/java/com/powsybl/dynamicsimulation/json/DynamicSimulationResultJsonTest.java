/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.dynamicsimulation.TimelineEvent;
import com.powsybl.timeseries.DoubleTimeSeries;
import com.powsybl.timeseries.RegularTimeSeriesIndex;
import com.powsybl.timeseries.TimeSeries;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
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
            public Status getStatus() {
                return Status.SUCCESS;
            }

            @Override
            public String getStatusText() {
                return "";
            }

            @Override
            public Map<String, DoubleTimeSeries> getCurves() {
                return Collections.singletonMap("curve1", TimeSeries.createDouble("curve1", new RegularTimeSeriesIndex(0, 5, 1), 0.0, 0.1, 0.1, 0.2, 0.1, 0.0));
            }

            @Override
            public Map<String, Double> getFinalStateValues() {
                return Collections.singletonMap("fsv1", 20.0);
            }

            @Override
            public List<TimelineEvent> getTimeLine() {
                return List.of(
                        new TimelineEvent(0.1, "CLA_2_5", "order to change topology"),
                        new TimelineEvent(1.2, "_BUS____2-BUS____5-1_AC", "opening both sides"),
                        new TimelineEvent(2.4, "CLA_2_4", "arming by over-current constraint"));
            }
        };
    }

    private static DynamicSimulationResult createFailedDynamicSimulation() {
        return new DynamicSimulationResult() {

            @Override
            public Status getStatus() {
                return Status.FAILURE;
            }

            @Override
            public String getStatusText() {
                return "Error test";
            }

            @Override
            public Map<String, DoubleTimeSeries> getCurves() {
                return Collections.emptyMap();
            }

            @Override
            public Map<String, Double> getFinalStateValues() {
                return Collections.emptyMap();
            }

            @Override
            public List<TimelineEvent> getTimeLine() {
                return DynamicSimulationResult.emptyTimeLine();
            }
        };
    }

    @Test
    void roundTripTest() throws IOException {
        roundTripTest(create(), DynamicSimulationResultSerializer::write, DynamicSimulationResultDeserializer::read, "/DynamicSimulationResult.json");
    }

    @Test
    void roundTripFailedSimulationTest() throws IOException {
        roundTripTest(createFailedDynamicSimulation(), DynamicSimulationResultSerializer::write, DynamicSimulationResultDeserializer::read, "/DynamicSimulationFailedResult.json");
    }

    @Test
    void handleErrorTest() throws IOException {
        try (var is = getClass().getResourceAsStream("/DynamicSimulationResultError.json")) {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> DynamicSimulationResultDeserializer.read(is));
            assertEquals("Unexpected field: metrics", e.getMessage());
        }
    }

}
