/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GeneratorModificationTest {

    private Network network;
    private Generator generator;

    @BeforeEach
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
        generator = network.getGenerator("GEN");
    }

    @Test
    void testHasImpact() {
        GeneratorModification.Modifs modifs = new GeneratorModification.Modifs();
        NetworkModification modification1 = new GeneratorModification("WRONG_ID", modifs);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        NetworkModification modification2 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulatorOn(true);
        NetworkModification modification3 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification3.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setConnected(true);
        NetworkModification modification4 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification4.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setDeltaTargetP(5.0);
        NetworkModification modification5 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification5.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setTargetP(225.0);
        NetworkModification modification6 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification6.hasImpactOnNetwork(network));

        generator.setVoltageRegulatorOn(false);
        modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulatorOn(true);
        NetworkModification modification7 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification7.hasImpactOnNetwork(network));

        generator.setVoltageRegulatorOn(true);
        modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulatorOn(false);
        NetworkModification modification8 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification8.hasImpactOnNetwork(network));

        generator.setVoltageRegulatorOn(false);
        generator.setTargetV(Double.NaN);
        modifs = new GeneratorModification.Modifs();
        modifs.setVoltageRegulatorOn(true);
        NetworkModification modification9 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification9.hasImpactOnNetwork(network));

        generator.setTargetV(24.5);
        generator.setVoltageRegulatorOn(true);
        modifs = new GeneratorModification.Modifs();
        modifs.setConnected(false);
        NetworkModification modification10 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification10.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setTargetQ(12.0);
        NetworkModification modification11 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification11.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setTargetV(12.0);
        NetworkModification modification12 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification12.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setMaxP(12.0);
        NetworkModification modification13 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification13.hasImpactOnNetwork(network));

        modifs = new GeneratorModification.Modifs();
        modifs.setMinP(12.0);
        NetworkModification modification14 = new GeneratorModification("GEN", modifs);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification14.hasImpactOnNetwork(network));
    }
}
