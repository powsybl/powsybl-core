/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.internal.AbstractTransformerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractTwoWindingsTransformerTest extends AbstractTransformerTest {

    private static final String INVALID = "invalid";

    private static final String TWT_NAME = "twt_name";

    @Test
    public void baseTests() {
        // adder
        TwoWindingsTransformer twoWindingsTransformer = substation.newTwoWindingsTransformer()
                                                                    .setId("twt")
                                                                    .setName(TWT_NAME)
                                                                    .setR(1.0)
                                                                    .setX(2.0)
                                                                    .setG(3.0)
                                                                    .setB(4.0)
                                                                    .setRatedU1(5.0)
                                                                    .setRatedU2(6.0)
                                                                    .setRatedS(7.0)
                                                                    .setVoltageLevel1("vl1")
                                                                    .setVoltageLevel2("vl2")
                                                                    .setConnectableBus1("busA")
                                                                    .setConnectableBus2("busB")
                                                                .add();
        assertEquals("twt", twoWindingsTransformer.getId());
        assertEquals(TWT_NAME, twoWindingsTransformer.getOptionalName().orElse(null));
        assertEquals(TWT_NAME, twoWindingsTransformer.getNameOrId());
        assertEquals(1.0, twoWindingsTransformer.getR(), 0.0);
        assertEquals(2.0, twoWindingsTransformer.getX(), 0.0);
        assertEquals(3.0, twoWindingsTransformer.getG(), 0.0);
        assertEquals(4.0, twoWindingsTransformer.getB(), 0.0);
        assertEquals(5.0, twoWindingsTransformer.getRatedU1(), 0.0);
        assertEquals(6.0, twoWindingsTransformer.getRatedU2(), 0.0);
        assertEquals(7.0, twoWindingsTransformer.getRatedS(), 0.0);
        assertEquals(IdentifiableType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformer.getType());
        assertSame(substation, twoWindingsTransformer.getSubstation().orElse(null));

        // setter getter
        double r = 0.5;
        twoWindingsTransformer.setR(r);
        assertEquals(r, twoWindingsTransformer.getR(), 0.0);
        double b = 1.0;
        twoWindingsTransformer.setB(b);
        assertEquals(b, twoWindingsTransformer.getB(), 0.0);
        double g = 2.0;
        twoWindingsTransformer.setG(g);
        assertEquals(g, twoWindingsTransformer.getG(), 0.0);
        double x = 4.0;
        twoWindingsTransformer.setX(x);
        assertEquals(x, twoWindingsTransformer.getX(), 0.0);
        double ratedU1 = 8.0;
        twoWindingsTransformer.setRatedU1(ratedU1);
        assertEquals(ratedU1, twoWindingsTransformer.getRatedU1(), 0.0);
        double ratedU2 = 16.0;
        twoWindingsTransformer.setRatedU2(ratedU2);
        assertEquals(ratedU2, twoWindingsTransformer.getRatedU2(), 0.0);
        double ratedS = 32.0;
        twoWindingsTransformer.setRatedS(ratedS);
        assertEquals(ratedS, twoWindingsTransformer.getRatedS(), 0.0);

        assertEquals(substation.getTwoWindingsTransformerStream().count(), substation.getTwoWindingsTransformerCount());
        VoltageLevel vl1 = network.getVoltageLevel("vl1");
        assertEquals(1, Iterables.size(vl1.getTwoWindingsTransformers()));
        assertEquals(1, vl1.getTwoWindingsTransformerStream().count());
        assertEquals(1, vl1.getTwoWindingsTransformerCount());
        assertSame(twoWindingsTransformer, vl1.getTwoWindingsTransformers().iterator().next());
        assertSame(twoWindingsTransformer, vl1.getTwoWindingsTransformerStream().findFirst().get());

        RatioTapChanger ratioTapChangerInLeg1 = createRatioTapChanger(twoWindingsTransformer, twoWindingsTransformer.getTerminal(TwoSides.ONE));
        assertTrue(twoWindingsTransformer.getOptionalRatioTapChanger().isPresent());
        ratioTapChangerInLeg1.setTargetV(12).setTapPosition(2);
        assertEquals(ratioTapChangerInLeg1.getTargetV(), twoWindingsTransformer.getRatioTapChanger().getTargetV(), 0.0);
        assertEquals(ratioTapChangerInLeg1.getTapPosition(), twoWindingsTransformer.getRatioTapChanger().getTapPosition());

        PhaseTapChanger phaseTapChangerInLeg1 = createPhaseTapChanger(twoWindingsTransformer, twoWindingsTransformer.getTerminal(TwoSides.TWO));
        assertTrue(twoWindingsTransformer.getOptionalPhaseTapChanger().isPresent());
        phaseTapChangerInLeg1.setTapPosition(2).setTargetDeadband(1);
        assertEquals(phaseTapChangerInLeg1.getTargetDeadband(), twoWindingsTransformer.getPhaseTapChanger().getTargetDeadband(), 0.0);
        assertEquals(phaseTapChangerInLeg1.getTapPosition(), twoWindingsTransformer.getPhaseTapChanger().getTapPosition());
    }

    @Test
    public void testDefaultValuesTwoWindingTransformer() {
        TwoWindingsTransformer twoWindingsTransformer = substation.newTwoWindingsTransformer()
                .setId("twt")
                .setName(TWT_NAME)
                .setR(1.0)
                .setX(2.0)
                .setRatedS(7.0)
                .setBus1("busA")
                .setBus2("busB")
                .add();

        assertEquals(0.0, twoWindingsTransformer.getG(), 0.0);
        assertEquals(0.0, twoWindingsTransformer.getB(), 0.0);

        VoltageLevel vl1 = network.getVoltageLevel("vl1");
        VoltageLevel vl2 = network.getVoltageLevel("vl2");
        assertSame(vl1, twoWindingsTransformer.getTerminal1().getVoltageLevel());
        assertSame(vl2, twoWindingsTransformer.getTerminal2().getVoltageLevel());
        assertEquals(vl1.getNominalV(), twoWindingsTransformer.getRatedU1(), 0.0);
        assertEquals(vl2.getNominalV(), twoWindingsTransformer.getRatedU2(), 0.0);
    }

    @Test
    public void invalidSubstationContainer() {
        network.newVoltageLevel()
                .setId("no_substation")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(200.0)
                .setLowVoltageLimit(180.0)
                .setHighVoltageLimit(220.0)
                .add()
                .getBusBreakerView().newBus().setId("no_substation_bus").add();
        ValidationException e = assertThrows(ValidationException.class, () -> substation.newTwoWindingsTransformer()
                .setId("twt")
                .setName(TWT_NAME)
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setRatedU1(5.0)
                .setRatedU2(6.0)
                .setRatedS(7.0)
                .setVoltageLevel1("no_substation")
                .setVoltageLevel2("vl2")
                .setConnectableBus1("no_substation_bus")
                .setConnectableBus2("busB")
                .add());
        assertTrue(e.getMessage().contains("2 windings transformer 'twt': the 2 windings of the transformer shall belong to the substation 'sub'"));
    }

    @Test
    public void testInvalidR() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTwoWindingTransformer(INVALID, INVALID, Double.NaN, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0));
        assertTrue(e.getMessage().contains("r is invalid"));
    }

    @Test
    public void testInvalidX() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTwoWindingTransformer(INVALID, INVALID, 1.0, Double.NaN, 1.0, 1.0, 1.0, 1.0, 1.0));
        assertTrue(e.getMessage().contains("x is invalid"));
    }

    @Test
    public void testInvalidG() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTwoWindingTransformer(INVALID, INVALID, 1.0, 1.0, Double.NaN, 1.0, 1.0, 1.0, 1.0));
        assertTrue(e.getMessage().contains("g is invalid"));
    }

    @Test
    public void testInvalidB() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTwoWindingTransformer(INVALID, INVALID, 1.0, 1.0, 1.0, Double.NaN, 1.0, 1.0, 1.0));
        assertTrue(e.getMessage().contains("b is invalid"));
    }

    @Test
    public void testInvalidRatedS() {
        ValidationException e = assertThrows(ValidationException.class, () -> createTwoWindingTransformer(INVALID, INVALID, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0));
        assertTrue(e.getMessage().contains("Invalid value of rated S 0.0"));
    }

    @Test
    public void transformerNotInSameSubstation() {
        Substation anotherSubstation = network.newSubstation()
                    .setId("subB")
                    .setName("n")
                    .setCountry(Country.FR)
                    .setTso("RTE")
                .add();
        VoltageLevel voltageLevelC = anotherSubstation.newVoltageLevel()
                    .setId("vl3")
                    .setName("vl3")
                    .setNominalV(200.0)
                    .setHighVoltageLimit(400.0)
                    .setLowVoltageLimit(200.0)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevelC.getBusBreakerView().newBus()
                    .setId("busC")
                    .setName("busC")
                .add();
        ValidationException e = assertThrows(ValidationException.class, () -> substation.newTwoWindingsTransformer().setId("invalidTwt")
                        .setName(TWT_NAME)
                        .setR(1.0)
                        .setX(2.0)
                        .setG(3.0)
                        .setB(4.0)
                        .setRatedU1(5.0)
                        .setRatedU2(6.0)
                        .setVoltageLevel1("vl1")
                        .setVoltageLevel2("vl3")
                        .setConnectableBus1("busA")
                        .setConnectableBus2("busC")
                    .add());
        assertTrue(e.getMessage().contains("the 2 windings of the transformer shall belong to the substation"));
    }

    private void createTwoWindingTransformer(String id, String name, double r, double x, double g, double b,
                                             double ratedU1, double ratedU2, double ratedS) {
        substation.newTwoWindingsTransformer()
                    .setId(id)
                    .setName(name)
                    .setR(r)
                    .setX(x)
                    .setG(g)
                    .setB(b)
                    .setRatedU1(ratedU1)
                    .setRatedU2(ratedU2)
                    .setRatedS(ratedS)
                    .setVoltageLevel1("vl1")
                    .setVoltageLevel2("vl2")
                    .setConnectableBus1("busA")
                    .setConnectableBus2("busB")
                .add();
    }

    private RatioTapChanger createRatioTapChanger(TwoWindingsTransformer transformer, Terminal terminal) {
        return createRatioTapChanger(transformer, terminal, false);
    }

    private RatioTapChanger createRatioTapChanger(TwoWindingsTransformer transformer, Terminal terminal, boolean regulating) {
        return transformer.newRatioTapChanger()
                .setRegulationValue(200.0)
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(0)
                .setTapPosition(0)
                .setRegulating(regulating)
                .setRegulationTerminal(terminal)
                .setTargetDeadband(0.5)
                .beginStep()
                .setR(39.78473)
                .setX(39.784725)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(39.78474)
                .setX(39.784726)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(39.78475)
                .setX(39.784727)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .add();
    }

    private PhaseTapChanger createPhaseTapChanger(TwoWindingsTransformer transformer, Terminal terminal) {
        return createPhaseTapChanger(transformer, terminal, false);
    }

    private PhaseTapChanger createPhaseTapChanger(TwoWindingsTransformer transformer, Terminal terminal, boolean regulating) {
        return transformer.newPhaseTapChanger()
                .setRegulationValue(200.0)
                .setLowTapPosition(0)
                .setTapPosition(0)
                .setRegulating(regulating)
                .setRegulationTerminal(terminal)
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setTargetDeadband(0.5)
                .beginStep()
                .setR(39.78473)
                .setX(39.784725)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .setAlpha(-10.0)
                .endStep()
                .beginStep()
                .setR(39.78474)
                .setX(39.784726)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .setAlpha(0.0)
                .endStep()
                .beginStep()
                .setR(39.78475)
                .setX(39.784727)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .setAlpha(10.0)
                .endStep()
                .add();
    }
}
