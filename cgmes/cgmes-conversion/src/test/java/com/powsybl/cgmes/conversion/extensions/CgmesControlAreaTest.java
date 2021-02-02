/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CgmesControlAreaTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        CgmesControlAreaAdder adder = network.newExtension(CgmesControlAreaAdder.class);
        adder.newCgmesControlArea("cgmesControlAreaId", "cgmesControlAreaName",
                "energyIdentCodeEic", 100.0)
                .addTerminal("equipmentId", 1);
        adder.add();

        CgmesControlAreaMapping extension = network.getExtension(CgmesControlAreaMapping.class);
        assertNotNull(extension);
        assertEquals("cgmesControlAreaId", extension.getCgmesControlArea("cgmesControlAreaId").getId());
        assertEquals("cgmesControlAreaName", extension.getCgmesControlArea("cgmesControlAreaId").getName());
        assertEquals("energyIdentCodeEic", extension.getCgmesControlArea("cgmesControlAreaId").getEnergyIdentCodeEic());
        assertEquals(100.0, extension.getCgmesControlArea("cgmesControlAreaId").getNetInterchange(), 0.0);
        assertEquals(1, extension.getTerminals("cgmesControlAreaId").size());
        extension.getTerminals("cgmesControlAreaId").stream().forEach(eqEnd -> {
            assertEquals("equipmentId", eqEnd.getEquipmentId());
            assertEquals(1, eqEnd.getEnd());
        });
    }
}
