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

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractTwoWindingsTransformerTest extends AbstractTransformerTest {

    private static final String INVALID = "invalid";

    private static final String TWT_NAME = "twt_name";

    public static boolean areTwoWindingsTransformersIdentical(TwoWindingsTransformer transformer1, TwoWindingsTransformer transformer2) {
        boolean areIdentical = false;

        if (transformer1 != null && transformer2 != null) {
            areIdentical = transformer1.getR() == transformer2.getR()
                    && transformer1.getX() == transformer2.getX()
                    && transformer1.getB() == transformer2.getB()
                    && transformer1.getG() == transformer2.getG()
                    && transformer1.getRatedU1() == transformer2.getRatedU1()
                    && transformer1.getRatedU2() == transformer2.getRatedU2()
                    && Objects.equals(transformer1.getTerminal1().getVoltageLevel().getId(), transformer2.getTerminal1().getVoltageLevel().getId())
                    && Objects.equals(transformer1.getTerminal2().getVoltageLevel().getId(), transformer2.getTerminal2().getVoltageLevel().getId());
        }
        return areIdentical;
    }

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
        assertTrue(ratioTapChangerInLeg1.findSolvedTapPosition().isEmpty());

        PhaseTapChanger phaseTapChangerInLeg1 = createPhaseTapChanger(twoWindingsTransformer, twoWindingsTransformer.getTerminal(TwoSides.TWO));
        assertTrue(twoWindingsTransformer.getOptionalPhaseTapChanger().isPresent());
        phaseTapChangerInLeg1.setTapPosition(2).setTargetDeadband(1);
        assertEquals(phaseTapChangerInLeg1.getTargetDeadband(), twoWindingsTransformer.getPhaseTapChanger().getTargetDeadband(), 0.0);
        assertEquals(phaseTapChangerInLeg1.getTapPosition(), twoWindingsTransformer.getPhaseTapChanger().getTapPosition());
        assertTrue(phaseTapChangerInLeg1.findSolvedTapPosition().isEmpty());
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

    @Test
    public void testTwoWindingsTransformersCopier() {
        // Transformers creation 1
        TwoWindingsTransformer transformer1 = substation.newTwoWindingsTransformer()
                .setId("twt1")
                .setName(TWT_NAME)
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setRatedU1(5.0)
                .setRatedU2(6.0)
                .setRatedS(7.0)
                .setBus1("busA")
                .setBus2("busB")
                .add();

        // Group and limits creation 1
        transformer1.newOperationalLimitsGroup1("group1").newCurrentLimits().setPermanentLimit(220.0).add();
        transformer1.setSelectedOperationalLimitsGroup1("group1");
        Optional<CurrentLimits> optionalLimits1 = transformer1.getCurrentLimits1();
        assertTrue(optionalLimits1.isPresent());
        CurrentLimits limits1 = optionalLimits1.get();
        assertNotNull(limits1);

        transformer1.getOperationalLimitsGroup1("group1").get().newActivePowerLimits().setPermanentLimit(220.0).add();
        Optional<ActivePowerLimits> optionalActivePowerLimits1 = transformer1.getActivePowerLimits1();
        assertTrue(optionalActivePowerLimits1.isPresent());
        ActivePowerLimits activePowerLimits1 = optionalActivePowerLimits1.get();
        assertNotNull(activePowerLimits1);

        transformer1.getOperationalLimitsGroup1("group1").get().newApparentPowerLimits().setPermanentLimit(220.0).add();
        Optional<ApparentPowerLimits> optionalApparentPowerLimits1 = transformer1.getApparentPowerLimits1();
        assertTrue(optionalApparentPowerLimits1.isPresent());
        ApparentPowerLimits apparentPowerLimits1 = optionalApparentPowerLimits1.get();
        assertNotNull(apparentPowerLimits1);

        // Group and limit creation 2
        transformer1.newOperationalLimitsGroup2("group2").newCurrentLimits().setPermanentLimit(80.0).add();
        transformer1.setSelectedOperationalLimitsGroup2("group2");
        Optional<CurrentLimits> optionalLimits2 = transformer1.getCurrentLimits2();
        assertTrue(optionalLimits2.isPresent());
        CurrentLimits limits2 = optionalLimits2.get();
        assertNotNull(limits2);

        // Transformers creation 2
        TwoWindingsTransformer transformer3 = substation.newTwoWindingsTransformer()
                .setId("twt3")
                .setName(TWT_NAME)
                .setR(2.0)
                .setX(3.0)
                .setG(5.0)
                .setB(5.0)
                .setRatedU1(6.0)
                .setRatedU2(7.0)
                .setRatedS(8.0)
                .setBus1("busA")
                .setBus2("busB")
                .add();

        // Transformers creation by copy
        TwoWindingsTransformer transformer2 = substation.newTwoWindingsTransformer(transformer1)
                .setId("twt2")
                .setRatedS(7.0)
                .setBus1("busA")
                .setBus2("busB")
                .add();

        // Group and limit creation 3
        Optional<CurrentLimits> optionalLimits3 = transformer2.getCurrentLimits1();
        assertTrue(optionalLimits3.isPresent());
        CurrentLimits limits3 = optionalLimits3.get();

        // Tests
        assertNotNull(transformer2);
        assertNotNull(transformer1);
        assertEquals(transformer1.getR(), transformer2.getR());
        assertEquals(transformer1.getX(), transformer2.getX());
        assertTrue(areTwoWindingsTransformersIdentical(transformer1, transformer2));
        assertFalse(areTwoWindingsTransformersIdentical(transformer1, transformer3));
        assertFalse(areTwoWindingsTransformersIdentical(transformer2, transformer3));
        assertTrue(areLimitsIdentical(limits1, limits3));
    }

    @Test
    void createTwoWindingsTransformerWithSolvedTapPosition() {
        createTwoWindingTransformer("twt", TWT_NAME, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0);

        TwoWindingsTransformer twoWindingsTransformer = network.getTwoWindingsTransformer("twt");

        RatioTapChanger ratioTapChanger = createRatioTapChanger(twoWindingsTransformer, twoWindingsTransformer.getTerminal(TwoSides.ONE), false, 1);
        assertTrue(twoWindingsTransformer.getOptionalRatioTapChanger().isPresent());
        assertEquals(1, twoWindingsTransformer.getRatioTapChanger().getSolvedTapPosition());
        ratioTapChanger.setSolvedTapPosition(2);
        assertEquals(2, ratioTapChanger.getSolvedTapPosition());

        PhaseTapChanger phaseTapChanger = createPhaseTapChanger(twoWindingsTransformer, twoWindingsTransformer.getTerminal(TwoSides.TWO), false, 1);
        assertTrue(twoWindingsTransformer.getOptionalPhaseTapChanger().isPresent());
        assertEquals(1, twoWindingsTransformer.getPhaseTapChanger().getSolvedTapPosition());
        phaseTapChanger.setSolvedTapPosition(2);
        assertEquals(2, phaseTapChanger.getSolvedTapPosition());

        // Check throws exception if invalid solved tap position
        assertThrows(ValidationException.class, () -> ratioTapChanger.setSolvedTapPosition(50));
        assertThrows(ValidationException.class, () -> phaseTapChanger.setSolvedTapPosition(50));

        // Unset solved tap position
        ratioTapChanger.unsetSolvedTapPosition();
        assertNull(ratioTapChanger.getSolvedTapPosition());
        phaseTapChanger.unsetSolvedTapPosition();
        assertNull(phaseTapChanger.getSolvedTapPosition());
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
        return createRatioTapChanger(transformer, terminal, false, null);
    }

    private RatioTapChanger createRatioTapChanger(TwoWindingsTransformer transformer, Terminal terminal, boolean regulating, Integer solvedTapPosition) {
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
                .setSolvedTapPosition(solvedTapPosition)
                .add();
    }

    private PhaseTapChanger createPhaseTapChanger(TwoWindingsTransformer transformer, Terminal terminal) {
        return createPhaseTapChanger(transformer, terminal, false, null);
    }

    private PhaseTapChanger createPhaseTapChanger(TwoWindingsTransformer transformer, Terminal terminal, boolean regulating, Integer solvedTapPosition) {
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
                .setSolvedTapPosition(solvedTapPosition)
                .add();
    }

}
