/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

class EquivalentInjectionImportTest extends AbstractSerDeTest {

    @Test
    void equivalentInjectionWithRegulationTargetTest() {
        String basename = "ei_regulation";
        Network network = Network.read(new ResourceDataSource(basename,
                new ResourceSet("/issues",
                        "ei_regulation_EQ.xml",
                        "ei_regulation_with_target_SSH.xml",
                        "ei_regulation_EQ_BD.xml")));
        BoundaryLine bl = network.getBoundaryLine("ACLS1");
        assertEquals(401, bl.getGeneration().getTargetV());
        assertTrue(bl.getGeneration().isVoltageRegulationOn());
    }

    @Test
    void equivalentInjectionMissingRegulationTargetTest() {
        String basename = "ei_regulation";
        Network network = Network.read(new ResourceDataSource(basename,
                new ResourceSet("/issues",
                        "ei_regulation_EQ.xml",
                        "ei_regulation_missing_target_SSH.xml",
                        "ei_regulation_EQ_BD.xml")));
        BoundaryLine bl = network.getBoundaryLine("ACLS1");
        // Generation data has been created for the boundary line but regulation is off
        assertNotNull(bl.getGeneration());
        assertTrue(Double.isNaN(bl.getGeneration().getTargetV()));
        assertFalse(bl.getGeneration().isVoltageRegulationOn());
    }

    @Test
    void equivalentInjectionWithRegulationTargetZeroTest() {
        String basename = "ei_regulation";
        Network network = Network.read(new ResourceDataSource(basename,
                new ResourceSet("/issues",
                        "ei_regulation_EQ.xml",
                        "ei_regulation_with_target_zero_SSH.xml",
                        "ei_regulation_EQ_BD.xml")));
        BoundaryLine bl = network.getBoundaryLine("ACLS1");
        // Zero is an invalid value, generation data has been created for the boundary line but regulation is off
        assertNotNull(bl.getGeneration());
        assertEquals(0.0, bl.getGeneration().getTargetV());
        assertFalse(bl.getGeneration().isVoltageRegulationOn());
    }
}
