/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
class NcVersionTest {

    @Test
    void parsesProfileVersions() {
        assertEquals(NcVersion.V2_2, NcVersion.fromProfileUri("http://entsoe.eu/ns/CIM/Contingency-EU/2.2"));
        assertEquals(NcVersion.V2_4, NcVersion.fromProfileUri("http://entsoe.eu/ns/CIM/RemedialAction-EU/2.4"));
        assertEquals(NcVersion.UNKNOWN, NcVersion.fromProfileUri("http://entsoe.eu/CIM/EquipmentCore/3/1"));
        assertEquals(NcVersion.UNKNOWN, NcVersion.fromProfileUri(null));
    }

    @Test
    void exposesSupportAndOrdering() {
        assertTrue(NcVersion.V2_2.isSupported());
        assertTrue(NcVersion.V2_3.isSupported());
        assertTrue(NcVersion.V2_4.isSupported());
        assertFalse(NcVersion.UNKNOWN.isSupported());
        assertTrue(NcVersion.V2_2.compareTo(NcVersion.V2_4) < 0);
        assertEquals("unknown", NcVersion.UNKNOWN.toString());
    }
}
