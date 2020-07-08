/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import org.junit.Test;

import java.io.IOException;

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
