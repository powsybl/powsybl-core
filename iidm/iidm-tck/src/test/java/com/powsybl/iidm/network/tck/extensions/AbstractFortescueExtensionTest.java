/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractFortescueExtensionTest {

    @Test
    public void testGenerator() {
        var network = EurostagTutorialExample1Factory.create();
        var gen = network.getGenerator("GEN");
        GeneratorFortescue fortescue = gen.newExtension(GeneratorFortescueAdder.class)
                .withRz(0.1d)
                .withXz(2d)
                .withRn(0.2d)
                .withXn(2.4d)
                .withGrounded(true)
                .withGroundingR(0.02d)
                .withGroundingX(0.3d)
                .add();
        assertEquals(0.1d, fortescue.getRz());
        assertEquals(2d, fortescue.getXz());
        assertEquals(0.2d, fortescue.getRn());
        assertEquals(2.4d, fortescue.getXn());
        assertTrue(fortescue.isGrounded());
        assertEquals(0.02d, fortescue.getGroundingR());
        assertEquals(0.3d, fortescue.getGroundingX());

        fortescue.setRz(0.11d);
        fortescue.setXz(2.03d);
        fortescue.setRn(0.1d);
        fortescue.setXn(2.3d);
        fortescue.setGrounded(false);
        fortescue.setGroundingR(0.0001d);
        fortescue.setGroundingX(0.35d);

        assertEquals(0.11d, fortescue.getRz());
        assertEquals(2.03d, fortescue.getXz());
        assertEquals(0.1d, fortescue.getRn());
        assertEquals(2.3d, fortescue.getXn());
        assertFalse(fortescue.isGrounded());
        assertEquals(0.0001d, fortescue.getGroundingR());
        assertEquals(0.35d, fortescue.getGroundingX());
    }

    @Test
    public void testLine() {
        var network = EurostagTutorialExample1Factory.create();
        var l = network.getLine("NHV1_NHV2_1");
        LineFortescue fortescue = l.newExtension(LineFortescueAdder.class)
                .withRz(0.1d)
                .withXz(2d)
                .withOpenPhaseA(true)
                .withOpenPhaseC(true)
                .add();

        assertEquals(0.1d, fortescue.getRz());
        assertEquals(2d, fortescue.getXz());
        assertTrue(fortescue.isOpenPhaseA());
        assertFalse(fortescue.isOpenPhaseB());
        assertTrue(fortescue.isOpenPhaseC());

        fortescue.setRz(0.11d);
        fortescue.setXz(2.03d);
        fortescue.setOpenPhaseA(false);

        assertEquals(0.11d, fortescue.getRz());
        assertEquals(2.03d, fortescue.getXz());
        assertFalse(fortescue.isOpenPhaseA());
    }

    @Test
    public void testTwoWindingsTransformer() {
        var network = EurostagTutorialExample1Factory.create();
        var twt = network.getTwoWindingsTransformer("NGEN_NHV1");
        TwoWindingsTransformerFortescue fortescue = twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
                .withRz(0.1d)
                .withXz(2d)
                .withFreeFluxes(true)
                .withConnectionType1(WindingConnectionType.Y_GROUNDED)
                .withConnectionType2(WindingConnectionType.DELTA)
                .withGroundingR1(0.02d)
                .withGroundingX1(0.3d)
                .withGroundingR2(0.04d)
                .withGroundingX2(0.95d)
                .add();
        assertEquals(0.1d, fortescue.getRz());
        assertEquals(2d, fortescue.getXz());
        assertTrue(fortescue.isFreeFluxes());
        assertSame(WindingConnectionType.Y_GROUNDED, fortescue.getConnectionType1());
        assertSame(WindingConnectionType.DELTA, fortescue.getConnectionType2());
        assertEquals(0.02d, fortescue.getGroundingR1());
        assertEquals(0.3d, fortescue.getGroundingX1());
        assertEquals(0.04d, fortescue.getGroundingR2());
        assertEquals(0.95d, fortescue.getGroundingX2());

        fortescue.setRz(0.11d);
        fortescue.setXz(2.03d);
        fortescue.setFreeFluxes(false);
        fortescue.setConnectionType1(WindingConnectionType.Y);
        fortescue.setConnectionType2(WindingConnectionType.Y_GROUNDED);
        fortescue.setGroundingR1(0.03d);
        fortescue.setGroundingX1(0.33d);
        fortescue.setGroundingR2(0.045d);
        fortescue.setGroundingX2(0.0001d);

        assertEquals(0.11d, fortescue.getRz());
        assertEquals(2.03d, fortescue.getXz());
        assertFalse(fortescue.isFreeFluxes());
        assertSame(WindingConnectionType.Y, fortescue.getConnectionType1());
        assertSame(WindingConnectionType.Y_GROUNDED, fortescue.getConnectionType2());
        assertEquals(0.03d, fortescue.getGroundingR1());
        assertEquals(0.33d, fortescue.getGroundingX1());
        assertEquals(0.045d, fortescue.getGroundingR2());
        assertEquals(0.0001d, fortescue.getGroundingX2());
    }

    @Test
    public void testThreeWindingsTransformer() {
        var network = ThreeWindingsTransformerNetworkFactory.create();
        var twt = network.getThreeWindingsTransformer("3WT");
        ThreeWindingsTransformerFortescue fortescue = twt.newExtension(ThreeWindingsTransformerFortescueAdder.class)
                .leg1()
                    .withRz(0.1d)
                    .withXz(2d)
                    .withFreeFluxes(true)
                    .withConnectionType(WindingConnectionType.Y_GROUNDED)
                    .withGroundingR(0.02d)
                    .withGroundingX(0.3d)
                .leg2()
                    .withRz(0.2d)
                    .withXz(2.1d)
                    .withFreeFluxes(false)
                    .withConnectionType(WindingConnectionType.Y)
                    .withGroundingR(0.12d)
                    .withGroundingX(0.4d)
                .leg3()
                    .withRz(0.3d)
                    .withXz(2.2d)
                    .withFreeFluxes(true)
                    .withConnectionType(WindingConnectionType.DELTA)
                    .withGroundingR(0.22d)
                    .withGroundingX(0.5d)
                .add();
        assertEquals(0.1d, fortescue.getLeg1().getRz());
        assertEquals(2d, fortescue.getLeg1().getXz());
        assertTrue(fortescue.getLeg1().isFreeFluxes());
        assertSame(WindingConnectionType.Y_GROUNDED, fortescue.getLeg1().getConnectionType());
        assertEquals(0.02d, fortescue.getLeg1().getGroundingR());
        assertEquals(0.3d, fortescue.getLeg1().getGroundingX());
        assertEquals(0.2d, fortescue.getLeg2().getRz());
        assertEquals(2.1d, fortescue.getLeg2().getXz());
        assertFalse(fortescue.getLeg2().isFreeFluxes());
        assertSame(WindingConnectionType.Y, fortescue.getLeg2().getConnectionType());
        assertEquals(0.12d, fortescue.getLeg2().getGroundingR());
        assertEquals(0.4d, fortescue.getLeg2().getGroundingX());
        assertEquals(0.3d, fortescue.getLeg3().getRz());
        assertEquals(2.2d, fortescue.getLeg3().getXz());
        assertTrue(fortescue.getLeg3().isFreeFluxes());
        assertSame(WindingConnectionType.DELTA, fortescue.getLeg3().getConnectionType());
        assertEquals(0.22d, fortescue.getLeg3().getGroundingR());
        assertEquals(0.5d, fortescue.getLeg3().getGroundingX());

        fortescue.getLeg1().setRz(1.1d);
        fortescue.getLeg1().setXz(3d);
        fortescue.getLeg1().setFreeFluxes(false);
        fortescue.getLeg1().setConnectionType(WindingConnectionType.DELTA);
        fortescue.getLeg1().setGroundingR(1.2d);
        fortescue.getLeg1().setGroundingX(3.1d);

        assertEquals(1.1d, fortescue.getLeg1().getRz());
        assertEquals(3d, fortescue.getLeg1().getXz());
        assertFalse(fortescue.getLeg1().isFreeFluxes());
        assertSame(WindingConnectionType.DELTA, fortescue.getLeg1().getConnectionType());
        assertEquals(1.2d, fortescue.getLeg1().getGroundingR());
        assertEquals(3.1d, fortescue.getLeg1().getGroundingX());
    }

    @Test
    public void testLoad() {
        var network = EurostagTutorialExample1Factory.create();
        var load = network.getLoad("LOAD");
        LoadAsymmetrical asym = load.newExtension(LoadAsymmetricalAdder.class)
                .withConnectionType(LoadConnectionType.DELTA)
                .withDeltaPa(-1)
                .withDeltaQa(1)
                .withDeltaPc(-2)
                .withDeltaQc(2)
                .add();
        assertSame(LoadConnectionType.DELTA, asym.getConnectionType());
        assertEquals(-1, asym.getDeltaPa(), 0);
        assertEquals(1, asym.getDeltaQa(), 0);
        assertEquals(0, asym.getDeltaPb(), 0);
        assertEquals(0, asym.getDeltaQb(), 0);
        assertEquals(-2, asym.getDeltaPc(), 0);
        assertEquals(2, asym.getDeltaQc(), 0);

        asym.setConnectionType(LoadConnectionType.Y);
        asym.setDeltaPa(-1.5);
        asym.setDeltaQa(1.5);
        asym.setDeltaPb(-0.5);
        asym.setDeltaQb(0.5);
        asym.setDeltaPc(-2.5);
        asym.setDeltaQc(2.5);

        assertSame(LoadConnectionType.Y, asym.getConnectionType());
        assertEquals(-1.5, asym.getDeltaPa(), 0);
        assertEquals(1.5, asym.getDeltaQa(), 0);
        assertEquals(-0.5, asym.getDeltaPb(), 0);
        assertEquals(0.5, asym.getDeltaQb(), 0);
        assertEquals(-2.5, asym.getDeltaPc(), 0);
        assertEquals(2.5, asym.getDeltaQc(), 0);
    }
}
