/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
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
                .withGeneratorType(GeneratorFortescue.GeneratorType.ROTATING_MACHINE)
                .withRo(0.1d)
                .withXo(2d)
                .withRi(0.2d)
                .withXi(2.4d)
                .withToGround(true)
                .withGroundingR(0.02d)
                .withGroundingX(0.3d)
                .add();
        assertSame(GeneratorFortescue.GeneratorType.ROTATING_MACHINE, fortescue.getGeneratorType());
        assertEquals(0.1d, fortescue.getRo());
        assertEquals(2d, fortescue.getXo());
        assertEquals(0.2d, fortescue.getRi());
        assertEquals(2.4d, fortescue.getXi());
        assertTrue(fortescue.isToGround());
        assertEquals(0.02d, fortescue.getGroundingR());
        assertEquals(0.3d, fortescue.getGroundingX());

        fortescue.setGeneratorType(GeneratorFortescue.GeneratorType.FEEDER);
        fortescue.setRo(0.11d);
        fortescue.setXo(2.03d);
        fortescue.setRi(0.1d);
        fortescue.setXi(2.3d);
        fortescue.setToGround(false);
        fortescue.setGroundingR(0.0001d);
        fortescue.setGroundingX(0.35d);

        assertSame(GeneratorFortescue.GeneratorType.FEEDER, fortescue.getGeneratorType());
        assertEquals(0.11d, fortescue.getRo());
        assertEquals(2.03d, fortescue.getXo());
        assertEquals(0.1d, fortescue.getRi());
        assertEquals(2.3d, fortescue.getXi());
        assertFalse(fortescue.isToGround());
        assertEquals(0.0001d, fortescue.getGroundingR());
        assertEquals(0.35d, fortescue.getGroundingX());
    }

    @Test
    void testLine() {
        var network = EurostagTutorialExample1Factory.create();
        var l = network.getLine("NHV1_NHV2_1");
        LineFortescue fortescue = l.newExtension(LineFortescueAdder.class)
                .withRo(0.1d)
                .withXo(2d)
                .add();

        assertEquals(0.1d, fortescue.getRo());
        assertEquals(2d, fortescue.getXo());

        fortescue.setRo(0.11d);
        fortescue.setXo(2.03d);

        assertEquals(0.11d, fortescue.getRo());
        assertEquals(2.03d, fortescue.getXo());
    }

    @Test
    void testTwoWindingsTransformer() {
        var network = EurostagTutorialExample1Factory.create();
        var twt = network.getTwoWindingsTransformer("NGEN_NHV1");
        TwoWindingsTransformerFortescue fortescue = twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
                .withPartOfGeneratingUnit(false)
                .withRo(0.1d)
                .withXo(2d)
                .withFreeFluxes(true)
                .withLeg1ConnectionType(LegConnectionType.Y_GROUNDED)
                .withLeg2ConnectionType(LegConnectionType.DELTA)
                .withGroundingR1(0.02d)
                .withGroundingX1(0.3d)
                .withGroundingR2(0.04d)
                .withGroundingX2(0.95d)
                .add();
        assertFalse(fortescue.isPartOfGeneratingUnit());
        assertEquals(0.1d, fortescue.getRo());
        assertEquals(2d, fortescue.getXo());
        assertTrue(fortescue.isFreeFluxes());
        assertSame(LegConnectionType.Y_GROUNDED, fortescue.getLeg1ConnectionType());
        assertSame(LegConnectionType.DELTA, fortescue.getLeg2ConnectionType());
        assertEquals(0.02d, fortescue.getGroundingR1());
        assertEquals(0.3d, fortescue.getGroundingX1());
        assertEquals(0.04d, fortescue.getGroundingR2());
        assertEquals(0.95d, fortescue.getGroundingX2());

        fortescue.setPartOfGeneratingUnit(true);
        fortescue.setRo(0.11d);
        fortescue.setXo(2.03d);
        fortescue.setFreeFluxes(false);
        fortescue.setLeg1ConnectionType(LegConnectionType.Y);
        fortescue.setLeg2ConnectionType(LegConnectionType.Y_GROUNDED);
        fortescue.setGroundingR1(0.03d);
        fortescue.setGroundingX1(0.33d);
        fortescue.setGroundingR2(0.045d);
        fortescue.setGroundingX2(0.0001d);

        assertTrue(fortescue.isPartOfGeneratingUnit());
        assertEquals(0.11d, fortescue.getRo());
        assertEquals(2.03d, fortescue.getXo());
        assertFalse(fortescue.isFreeFluxes());
        assertSame(LegConnectionType.Y, fortescue.getLeg1ConnectionType());
        assertSame(LegConnectionType.Y_GROUNDED, fortescue.getLeg2ConnectionType());
        assertEquals(0.03d, fortescue.getGroundingR1());
        assertEquals(0.33d, fortescue.getGroundingX1());
        assertEquals(0.045d, fortescue.getGroundingR2());
        assertEquals(0.0001d, fortescue.getGroundingX2());
    }
}
