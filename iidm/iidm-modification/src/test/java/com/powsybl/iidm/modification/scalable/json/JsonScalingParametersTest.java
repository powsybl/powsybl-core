/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable.json;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.iidm.modification.scalable.ScalingParameters;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class JsonScalingParametersTest extends AbstractConverterTest {

    @Test
    void roundTrip() throws IOException {
        ScalingParameters parameters = new ScalingParameters()
                .setScalingConvention(Scalable.ScalingConvention.LOAD)
                .setReconnect(true);
        roundTripTest(parameters, JsonScalingParameters::write, JsonScalingParameters::read, "/json/ScalingParameters.json");
    }

    @Test
    @Disabled("TODO")
    void testDeserializerV1dot1() throws IOException {
        // TODO
        assertTrue(true);
//        ScalingParameters scalingParameters = deserialize("/json/ScalingParameters_v1.0.json")
    }

    @Test
    void error() throws IOException {
        try (var is = getClass().getResourceAsStream("/json/ScalingParametersError.json")) {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> JsonScalingParameters.read(is));
            assertEquals("Unexpected field: error", e.getMessage());
        }
    }
}
