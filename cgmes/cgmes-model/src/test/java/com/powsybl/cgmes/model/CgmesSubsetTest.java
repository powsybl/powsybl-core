/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class CgmesSubsetTest {

    @Test
    void isValidSubsetName() {
        // Valid EQ name
        assertTrue(CgmesSubset.EQUIPMENT.isValidName("20210325T1530Z_1D_BE_EQ_001.xml"));
        assertTrue(CgmesSubset.EQUIPMENT.isValidName("IGM_EQ.xml"));

        // Valid EQ_BD name
        assertTrue(CgmesSubset.EQUIPMENT_BOUNDARY.isValidName("20171002T0930Z_ENTSO-E_EQ_BD_2.xml"));
        assertTrue(CgmesSubset.EQUIPMENT_BOUNDARY.isValidName("BOUNDARY_EQ.xml"));

        // Valid TP name
        assertTrue(CgmesSubset.TOPOLOGY.isValidName("20210325T1530Z_1D_ASSEMBLED_TP_001.xml"));
        assertTrue(CgmesSubset.TOPOLOGY.isValidName("IGM_TP.xml"));

        // Valid TP_BD name
        assertTrue(CgmesSubset.TOPOLOGY_BOUNDARY.isValidName("20171002T0930Z_ENTSO-E_TP_BD_2.xml"));
        assertTrue(CgmesSubset.TOPOLOGY_BOUNDARY.isValidName("BOUNDARY_TP.xml"));
    }
}
