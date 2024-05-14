/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable.json;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.iidm.modification.scalable.ScalingParameters;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.ONESHOT;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.RESPECT_OF_VOLUME_ASKED;
import static com.powsybl.iidm.modification.scalable.json.JsonScalingParameters.read;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class JsonScalingParametersTest extends AbstractSerDeTest {

    @Test
    void roundTrip() throws IOException {
        ScalingParameters parameters = new ScalingParameters()
                .setScalingConvention(Scalable.ScalingConvention.LOAD)
                .setReconnect(true)
                .setIgnoredInjectionIds(Set.of("id1", "id2"));
        roundTripTest(parameters, JsonScalingParameters::write, JsonScalingParameters::read, "/json/ScalingParameters.json");
    }

    @Test
    void testDeserializerV1dot1() {
        ScalingParameters parameters = read(getClass().getResourceAsStream("/json/ScalingParameters_v1.0.json"));
        assertEquals(Scalable.ScalingConvention.LOAD, parameters.getScalingConvention());
        assertFalse(parameters.isConstantPowerFactor());
        assertEquals(ONESHOT, parameters.getPriority());
        assertTrue(parameters.isReconnect());
        assertFalse(parameters.isAllowsGeneratorOutOfActivePowerLimits());
        assertEquals(2, parameters.getIgnoredInjectionIds().size());

        parameters = read(getClass().getResourceAsStream("/json/ScalingParameters_v1.0b.json"));
        assertEquals(RESPECT_OF_VOLUME_ASKED, parameters.getPriority());
    }

    @Test
    void error() throws IOException {
        try (var is = getClass().getResourceAsStream("/json/ScalingParametersError.json")) {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> read(is));
            assertEquals("Unexpected field: error", e.getMessage());
        }
    }
}
