/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.modification.NetworkModificationImpact.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Benoît Chiquet {@literal <benoit.chiquet at rte-france.com>}
 */
class PercentChangeLoadModificationTest {

    @Test
    void shouldIncreaseP0() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        assertEquals(600.0, load.getP0());
        PercentChangeLoadModification modification = new PercentChangeLoadModification("LOAD", 3.51, 0);
        modification.apply(network);
        assertEquals(621.06, load.getP0());
    }

    @Test
    void shouldDecreaseP0() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        assertEquals(600.0, load.getP0());
        PercentChangeLoadModification modification = new PercentChangeLoadModification("LOAD", -3.5, 0);
        modification.apply(network);
        assertEquals(579.0, load.getP0());
    }

    @Test
    void shouldIncreaseQ0() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        assertEquals(200.0, load.getQ0());
        PercentChangeLoadModification modification = new PercentChangeLoadModification("LOAD", 0, 2.5);
        modification.apply(network);
        assertEquals(205.0, load.getQ0());
    }

    @Test
    void shouldDecreaseQ0() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        assertEquals(200.0, load.getQ0());
        PercentChangeLoadModification modification = new PercentChangeLoadModification("LOAD", 0, -2.5);
        modification.apply(network);
        assertEquals(195.0, load.getQ0());
    }

    @Test
    void shouldThrowWhenLoadDecreasesTooMuch() {
        assertThrows(PowsyblException.class, () -> new PercentChangeLoadModification("LOAD", -101, 0));
        assertThrows(PowsyblException.class, () -> new PercentChangeLoadModification("LOAD", 0, -101));
    }

    @Test
    void shouldThrowWhenLoadNotFound() {
        Network network = EurostagTutorialExample1Factory.create();
        PercentChangeLoadModification modification = new PercentChangeLoadModification("LoadNotFound", 2.5, 2.5);
        assertThrows(PowsyblException.class, () -> modification.apply(network));
    }

    @Test
    void impactOnNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        assertEquals(NO_IMPACT_ON_NETWORK, new PercentChangeLoadModification("LOAD", 0, 0).hasImpactOnNetwork(network));
        assertEquals(HAS_IMPACT_ON_NETWORK, new PercentChangeLoadModification("LOAD", 3, 0).hasImpactOnNetwork(network));
        assertEquals(HAS_IMPACT_ON_NETWORK, new PercentChangeLoadModification("LOAD", 0, 3).hasImpactOnNetwork(network));
        assertEquals(CANNOT_BE_APPLIED, new PercentChangeLoadModification("LoadNotFound", 3, 2).hasImpactOnNetwork(network));
    }

    @Test
    void pctLoadModificationName() {
        PercentChangeLoadModification modification = new PercentChangeLoadModification("LOAD", 3.5, 0);
        assertEquals("pctLoadModification", modification.getName());
    }

}
