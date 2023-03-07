/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractFortescueExtensionTest {

    @Test
    void testGenerator() {
        var network = EurostagTutorialExample1Factory.create();
        var gen = network.getGenerator("GEN");
        GeneratorFortescue fortescue = gen.newExtension(GeneratorFortescueAdder.class)
                .withR0(0.1d)
                .withX0(2d)
                .withR2(0.2d)
                .withX2(2.4d)
                .withGrounded(true)
                .withGroundingR(0.02d)
                .withGroundingX(0.3d)
                .add();
        assertEquals(0.1d, fortescue.getR0());
        assertEquals(2d, fortescue.getX0());
        assertEquals(0.2d, fortescue.getR2());
        assertEquals(2.4d, fortescue.getX2());
        assertTrue(fortescue.isGrounded());
        assertEquals(0.02d, fortescue.getGroundingR());
        assertEquals(0.3d, fortescue.getGroundingX());

        fortescue.setR0(0.11d);
        fortescue.setX0(2.03d);
        fortescue.setR2(0.1d);
        fortescue.setX2(2.3d);
        fortescue.setGrounded(false);
        fortescue.setGroundingR(0.0001d);
        fortescue.setGroundingX(0.35d);

        assertEquals(0.11d, fortescue.getR0());
        assertEquals(2.03d, fortescue.getX0());
        assertEquals(0.1d, fortescue.getR2());
        assertEquals(2.3d, fortescue.getX2());
        assertFalse(fortescue.isGrounded());
        assertEquals(0.0001d, fortescue.getGroundingR());
        assertEquals(0.35d, fortescue.getGroundingX());
    }

    @Test
    void testLine() {
        var network = EurostagTutorialExample1Factory.create();
        var l = network.getLine("NHV1_NHV2_1");
        LineFortescue fortescue = l.newExtension(LineFortescueAdder.class)
                .withR0(0.1d)
                .withX0(2d)
                .add();

        assertEquals(0.1d, fortescue.getR0());
        assertEquals(2d, fortescue.getX0());

        fortescue.setR0(0.11d);
        fortescue.setX0(2.03d);

        assertEquals(0.11d, fortescue.getR0());
        assertEquals(2.03d, fortescue.getX0());
    }

    @Test
    void testTwoWindingsTransformer() {
        var network = EurostagTutorialExample1Factory.create();
        var twt = network.getTwoWindingsTransformer("NGEN_NHV1");
        TwoWindingsTransformerFortescue fortescue = twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
                .withR0(0.1d)
                .withX0(2d)
                .withFreeFluxes(true)
                .withConnectionType1(WindingConnectionType.Y_GROUNDED)
                .withConnectionType2(WindingConnectionType.DELTA)
                .withGroundingR1(0.02d)
                .withGroundingX1(0.3d)
                .withGroundingR2(0.04d)
                .withGroundingX2(0.95d)
                .add();
        assertEquals(0.1d, fortescue.getR0());
        assertEquals(2d, fortescue.getX0());
        assertTrue(fortescue.isFreeFluxes());
        assertSame(WindingConnectionType.Y_GROUNDED, fortescue.getConnectionType1());
        assertSame(WindingConnectionType.DELTA, fortescue.getConnectionType2());
        assertEquals(0.02d, fortescue.getGroundingR1());
        assertEquals(0.3d, fortescue.getGroundingX1());
        assertEquals(0.04d, fortescue.getGroundingR2());
        assertEquals(0.95d, fortescue.getGroundingX2());

        fortescue.setR0(0.11d);
        fortescue.setX0(2.03d);
        fortescue.setFreeFluxes(false);
        fortescue.setConnectionType1(WindingConnectionType.Y);
        fortescue.setConnectionType2(WindingConnectionType.Y_GROUNDED);
        fortescue.setGroundingR1(0.03d);
        fortescue.setGroundingX1(0.33d);
        fortescue.setGroundingR2(0.045d);
        fortescue.setGroundingX2(0.0001d);

        assertEquals(0.11d, fortescue.getR0());
        assertEquals(2.03d, fortescue.getX0());
        assertFalse(fortescue.isFreeFluxes());
        assertSame(WindingConnectionType.Y, fortescue.getConnectionType1());
        assertSame(WindingConnectionType.Y_GROUNDED, fortescue.getConnectionType2());
        assertEquals(0.03d, fortescue.getGroundingR1());
        assertEquals(0.33d, fortescue.getGroundingX1());
        assertEquals(0.045d, fortescue.getGroundingR2());
        assertEquals(0.0001d, fortescue.getGroundingX2());
    }

    @Test
    void testThreeWindingsTransformer() {
        var network = ThreeWindingsTransformerNetworkFactory.create();
        var twt = network.getThreeWindingsTransformer("3WT");
        ThreeWindingsTransformerFortescue fortescue = twt.newExtension(ThreeWindingsTransformerFortescueAdder.class)
                .leg1()
                    .withR0(0.1d)
                    .withX0(2d)
                    .withFreeFluxes(true)
                    .withConnectionType(WindingConnectionType.Y_GROUNDED)
                    .withGroundingR(0.02d)
                    .withGroundingX(0.3d)
                .leg2()
                    .withR0(0.2d)
                    .withX0(2.1d)
                    .withFreeFluxes(false)
                    .withConnectionType(WindingConnectionType.Y)
                    .withGroundingR(0.12d)
                    .withGroundingX(0.4d)
                .leg3()
                    .withR0(0.3d)
                    .withX0(2.2d)
                    .withFreeFluxes(true)
                    .withConnectionType(WindingConnectionType.DELTA)
                    .withGroundingR(0.22d)
                    .withGroundingX(0.5d)
                .add();
        assertEquals(0.1d, fortescue.getLeg1().getR0());
        assertEquals(2d, fortescue.getLeg1().getX0());
        assertTrue(fortescue.getLeg1().isFreeFluxes());
        assertSame(WindingConnectionType.Y_GROUNDED, fortescue.getLeg1().getConnectionType());
        assertEquals(0.02d, fortescue.getLeg1().getGroundingR());
        assertEquals(0.3d, fortescue.getLeg1().getGroundingX());
        assertEquals(0.2d, fortescue.getLeg2().getR0());
        assertEquals(2.1d, fortescue.getLeg2().getX0());
        assertFalse(fortescue.getLeg2().isFreeFluxes());
        assertSame(WindingConnectionType.Y, fortescue.getLeg2().getConnectionType());
        assertEquals(0.12d, fortescue.getLeg2().getGroundingR());
        assertEquals(0.4d, fortescue.getLeg2().getGroundingX());
        assertEquals(0.3d, fortescue.getLeg3().getR0());
        assertEquals(2.2d, fortescue.getLeg3().getX0());
        assertTrue(fortescue.getLeg3().isFreeFluxes());
        assertSame(WindingConnectionType.DELTA, fortescue.getLeg3().getConnectionType());
        assertEquals(0.22d, fortescue.getLeg3().getGroundingR());
        assertEquals(0.5d, fortescue.getLeg3().getGroundingX());

        fortescue.getLeg1().setR0(1.1d);
        fortescue.getLeg1().setX0(3d);
        fortescue.getLeg1().setFreeFluxes(false);
        fortescue.getLeg1().setConnectionType(WindingConnectionType.DELTA);
        fortescue.getLeg1().setGroundingR(1.2d);
        fortescue.getLeg1().setGroundingX(3.1d);

        assertEquals(1.1d, fortescue.getLeg1().getR0());
        assertEquals(3d, fortescue.getLeg1().getX0());
        assertFalse(fortescue.getLeg1().isFreeFluxes());
        assertSame(WindingConnectionType.DELTA, fortescue.getLeg1().getConnectionType());
        assertEquals(1.2d, fortescue.getLeg1().getGroundingR());
        assertEquals(3.1d, fortescue.getLeg1().getGroundingX());
    }
}
