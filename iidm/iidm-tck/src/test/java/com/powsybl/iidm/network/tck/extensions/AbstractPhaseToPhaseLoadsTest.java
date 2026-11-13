/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public abstract class AbstractPhaseToPhaseLoadsTest {

    private static PhaseToPhaseLoads createExtension(Network network) {
        Load load = network.getLoad("LOAD");
        assertNotNull(load);
        return load.newExtension(PhaseToPhaseLoadsAdder.class).add();
    }

    @Test
    public void testCreation() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        assertNull(load.getExtension(PhaseToPhaseLoads.class));

        PhaseToPhaseLoads ext = createExtension(network);
        assertSame(ext, load.getExtension(PhaseToPhaseLoads.class));
        assertEquals("phaseToPhaseLoads", ext.getName());
        assertTrue(ext.getPhaseToPhaseLoads().isEmpty());

        PhaseToPhaseLoad ab = ext.newPhaseToPhaseLoad().withPhase1(Phase.A).withPhase2(Phase.B).withP(10).withQ(5).add();
        PhaseToPhaseLoad bc = ext.newPhaseToPhaseLoad().withPhase1(Phase.B).withPhase2(Phase.C).withP(3).withQ(1).add();

        assertEquals(2, ext.getPhaseToPhaseLoads().size());
        assertEquals(Phase.A, ab.getPhase1());
        assertEquals(Phase.B, ab.getPhase2());
        assertEquals(10, ab.getP(), 0);
        assertEquals(5, ab.getQ(), 0);
        assertEquals(3, bc.getP(), 0);
        assertEquals(1, bc.getQ(), 0);
        assertThrows(UnsupportedOperationException.class, () -> ext.getPhaseToPhaseLoads().clear());
    }

    @Test
    public void testFindSetAndRemove() {
        Network network = EurostagTutorialExample1Factory.create();
        PhaseToPhaseLoads ext = createExtension(network);
        PhaseToPhaseLoad ab = ext.newPhaseToPhaseLoad().withPhase1(Phase.A).withPhase2(Phase.B).withP(10).withQ(5).add();
        ext.newPhaseToPhaseLoad().withPhase1(Phase.B).withPhase2(Phase.C).withP(3).withQ(1).add();

        assertSame(ab, ext.findPhaseToPhaseLoad(Phase.A, Phase.B).orElseThrow());
        assertSame(ab, ext.findPhaseToPhaseLoad(Phase.B, Phase.A).orElseThrow());
        assertTrue(ext.findPhaseToPhaseLoad(Phase.A, Phase.C).isEmpty());

        ab.setP(20).setQ(8);
        assertEquals(20, ab.getP(), 0);
        assertEquals(8, ab.getQ(), 0);
        assertThrows(PowsyblException.class, () -> ab.setP(Double.NaN));
        assertThrows(PowsyblException.class, () -> ab.setQ(Double.NaN));
        assertEquals(20, ab.getP(), 0);

        assertTrue(ext.removePhaseToPhaseLoad(Phase.C, Phase.B));
        assertFalse(ext.removePhaseToPhaseLoad(Phase.C, Phase.B));
        assertEquals(1, ext.getPhaseToPhaseLoads().size());
        assertTrue(ext.removePhaseToPhaseLoad(ab));
        assertFalse(ext.removePhaseToPhaseLoad(ab));
        assertTrue(ext.getPhaseToPhaseLoads().isEmpty());
    }

    @Test
    public void testValidation() {
        Network network = EurostagTutorialExample1Factory.create();
        PhaseToPhaseLoads ext = createExtension(network);

        PhaseToPhaseLoadAdder missingPhase1 = ext.newPhaseToPhaseLoad().withPhase2(Phase.B).withP(1).withQ(1);
        assertThrows(PowsyblException.class, missingPhase1::add);

        PhaseToPhaseLoadAdder missingPhase2 = ext.newPhaseToPhaseLoad().withPhase1(Phase.A).withP(1).withQ(1);
        assertThrows(PowsyblException.class, missingPhase2::add);

        PhaseToPhaseLoadAdder samePhase = ext.newPhaseToPhaseLoad().withPhase1(Phase.A).withPhase2(Phase.A).withP(1).withQ(1);
        PowsyblException e = assertThrows(PowsyblException.class, samePhase::add);
        assertTrue(e.getMessage().contains("two different phases"));

        PhaseToPhaseLoadAdder nanP = ext.newPhaseToPhaseLoad().withPhase1(Phase.A).withPhase2(Phase.B).withP(Double.NaN).withQ(1);
        assertThrows(PowsyblException.class, nanP::add);

        PhaseToPhaseLoadAdder nanQ = ext.newPhaseToPhaseLoad().withPhase1(Phase.A).withPhase2(Phase.B).withP(1).withQ(Double.NaN);
        assertThrows(PowsyblException.class, nanQ::add);

        PhaseToPhaseLoadAdder noPower = ext.newPhaseToPhaseLoad().withPhase1(Phase.A).withPhase2(Phase.B);
        assertThrows(PowsyblException.class, noPower::add);

        assertTrue(ext.getPhaseToPhaseLoads().isEmpty());
    }

    @Test
    public void testDuplicate() {
        Network network = EurostagTutorialExample1Factory.create();
        PhaseToPhaseLoads ext = createExtension(network);
        ext.newPhaseToPhaseLoad().withPhase1(Phase.A).withPhase2(Phase.B).withP(1).withQ(1).add();

        PhaseToPhaseLoadAdder sameOrder = ext.newPhaseToPhaseLoad().withPhase1(Phase.A).withPhase2(Phase.B).withP(2).withQ(2);
        PowsyblException e = assertThrows(PowsyblException.class, sameOrder::add);
        assertTrue(e.getMessage().contains("already exists"));

        PhaseToPhaseLoadAdder reversedOrder = ext.newPhaseToPhaseLoad().withPhase1(Phase.B).withPhase2(Phase.A).withP(2).withQ(2);
        assertThrows(PowsyblException.class, reversedOrder::add);

        assertEquals(1, ext.getPhaseToPhaseLoads().size());
    }

    @Test
    public void variantsCloneTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";

        Network network = EurostagTutorialExample1Factory.create();
        PhaseToPhaseLoads ext = createExtension(network);
        PhaseToPhaseLoad ab = ext.newPhaseToPhaseLoad().withPhase1(Phase.A).withPhase2(Phase.B).withP(10).withQ(5).add();

        VariantManager variantManager = network.getVariantManager();
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variant1);
        variantManager.cloneVariant(variant1, variant2);
        variantManager.setWorkingVariant(variant1);
        assertEquals(10, ab.getP(), 0);
        assertEquals(5, ab.getQ(), 0);

        // A component added after variants have been created is available in all variants
        PhaseToPhaseLoad bc = ext.newPhaseToPhaseLoad().withPhase1(Phase.B).withPhase2(Phase.C).withP(3).withQ(1).add();
        variantManager.setWorkingVariant(variant2);
        assertEquals(3, bc.getP(), 0);

        // Different values in a cloned variant do not affect the initial variant
        variantManager.setWorkingVariant(variant1);
        ab.setP(20).setQ(8);
        assertEquals(20, ab.getP(), 0);
        assertEquals(8, ab.getQ(), 0);
        variantManager.setWorkingVariant(variant2);
        assertEquals(10, ab.getP(), 0);
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(10, ab.getP(), 0);
        assertEquals(5, ab.getQ(), 0);
    }
}
