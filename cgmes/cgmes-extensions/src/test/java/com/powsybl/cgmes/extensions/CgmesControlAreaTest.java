/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class CgmesControlAreaTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(CgmesControlAreasAdder.class).add();
        CgmesControlAreas extension = network.getExtension(CgmesControlAreas.class);
        extension.newCgmesControlArea()
                .setId("cgmesControlAreaId")
                .setName("cgmesControlAreaName")
                .setEnergyIdentificationCodeEic("energyIdentCodeEic")
                .setNetInterchange(100.0)
                .add()
                .add(network.getLine("NHV1_NHV2_1").getTerminal1());

        assertNotNull(extension);
        assertTrue(extension.containsCgmesControlAreaId("cgmesControlAreaId"));
        CgmesControlArea controlArea = extension.getCgmesControlArea("cgmesControlAreaId");
        assertNotNull(controlArea);
        assertEquals("cgmesControlAreaId", controlArea.getId());
        assertEquals("cgmesControlAreaName", controlArea.getName());
        assertEquals("energyIdentCodeEic", controlArea.getEnergyIdentificationCodeEIC());
        assertEquals(100.0, controlArea.getNetInterchange(), 0.0);
        assertEquals(1, controlArea.getTerminals().size());
        controlArea.getTerminals().forEach(t -> assertSame(network.getLine("NHV1_NHV2_1").getTerminal1(), t));
    }

    @Test
    void invalid() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(CgmesControlAreasAdder.class).add();

        try {
            network.getExtension(CgmesControlAreas.class)
                    .newCgmesControlArea()
                    .setName("cgmesControlAreaName")
                    .setEnergyIdentificationCodeEic("energyIdentCodeEic")
                    .setNetInterchange(100.0)
                    .add()
                    .add(network.getLine("NHV1_NHV2_1").getTerminal1());
            fail();
        } catch (PowsyblException e) {
            assertEquals("Undefined ID for CGMES control area", e.getMessage());
        }
    }
}
