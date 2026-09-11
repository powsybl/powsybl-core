/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
class RegulationModeTest {

    @ParameterizedTest
    @EnumSource(RegulationMode.class)
    void fromIndexValidTest(RegulationMode regulationMode) {
        assertEquals(regulationMode, RegulationMode.fromIndex(regulationMode.getIndex()));
    }

    @Test
    void fromIndexInvalidTest() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> RegulationMode.fromIndex(-1));
        assertEquals("Unknown or unsupported regulation mode index: -1. Allowed values are: [1, 2, 3]", exception.getMessage());
    }

    @Test
    void getIndexTest() {
        assertEquals(1, RegulationMode.getIndexFromMode(RegulationMode.VOLTAGE));
        assertNull(RegulationMode.getIndexFromMode(null));
    }

}
