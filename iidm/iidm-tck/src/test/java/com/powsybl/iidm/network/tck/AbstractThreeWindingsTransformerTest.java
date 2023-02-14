/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.tck.internal.AbstractTransformerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractThreeWindingsTransformerTest extends AbstractTransformerTest {

    private static final String TWT_NAME = "twtName";

    private static final String ERROR_RATED_U_IS_INVALID = "rated U is invalid";

    private static final String ERROR_B_IS_NOT_SET = "b is not set";

    private static final String ERROR_G_IS_NOT_SET = "g is not set";

    private static final String ERROR_X_IS_NOT_SET = "x is not set";

    private static final String ERROR_R_IS_NOT_SET = "r is not set";

    private static final String ERROR_LEG1_IS_NOT_SET = "Leg1 is not set";

    private static final String ERROR_LEG2_IS_NOT_SET = "Leg2 is not set";

    private static final String ERROR_LEG3_IS_NOT_SET = "Leg3 is not set";

    private static final String ERROR_TRANSFORMER_LEG3_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED = "3 windings transformer leg3 'twt': Only one regulating control enabled is allowed";

    private static final String ERROR_LEG1_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED = "3 windings transformer leg1 'twt': Only one regulating control enabled is allowed";

    private static final String ERROR_LEG2_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED = "3 windings transformer leg2 'twt': Only one regulating control enabled is allowed";

    @Test
    public void baseTests() {
        ThreeWindingsTransformerAdder transformerAdder = createThreeWindingsTransformerAdder();
        ThreeWindingsTransformer transformer = transformerAdder.add();

        assertEquals("twt", transformer.getId());
        assertEquals(TWT_NAME, transformer.getOptionalName().orElse(null));
        assertEquals(TWT_NAME, transformer.getNameOrId());
        assertEquals(substation, transformer.getSubstation().orElse(null));
        assertEquals(IdentifiableType.THREE_WINDINGS_TRANSFORMER, transformer.getType());

        assertEquals(substation.getThreeWindingsTransformerStream().count(), substation.getThreeWindingsTransformerCount());
        VoltageLevel vl1 = network.getVoltageLevel("vl1");
        assertEquals(1, Iterables.size(vl1.getThreeWindingsTransformers()));
        assertEquals(1, vl1.getThreeWindingsTransformerStream().count());
        assertEquals(1, vl1.getThreeWindingsTransformerCount());
        assertSame(transformer, vl1.getThreeWindingsTransformers().iterator().next());
        assertSame(transformer, vl1.getThreeWindingsTransformerStream().findFirst().get());

        // leg1 adder
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        assertEquals(1.3, leg1.getR(), 0.0);
        assertEquals(1.4, leg1.getX(), 0.0);
        assertEquals(1.1, leg1.getRatedU(), 0.0);
        assertEquals(1.6, leg1.getG(), 0.0);
        assertEquals(1.7, leg1.getB(), 0.0);
        assertSame(transformer.getTerminal(ThreeWindingsTransformer.Side.ONE), leg1.getTerminal());
        // leg1 setter getter
        leg1.setR(2.0);
        assertEquals(2.0, leg1.getR(), 0.0);
        leg1.setX(2.1);
        assertEquals(2.1, leg1.getX(), 0.0);
        leg1.setG(2.2);
        assertEquals(2.2, leg1.getG(), 0.0);
        leg1.setB(2.3);
        assertEquals(2.3, leg1.getB(), 0.0);
        leg1.setRatedS(2.4);
        assertEquals(2.4, leg1.getRatedS(), 0.0);

        // leg2/3 adder
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        assertEquals(2.03, leg2.getR(), 0.0);
        assertEquals(2.04, leg2.getX(), 0.0);
        assertEquals(2.05, leg2.getRatedU(), 0.0);
        assertEquals(2.06, leg2.getRatedS(), 0.0);
        assertEquals(3.3, leg3.getR(), 0.0);
        assertEquals(3.4, leg3.getX(), 0.0);
        assertEquals(3.5, leg3.getRatedU(), 0.0);
        assertEquals(3.6, leg3.getRatedS(), 0.0);
        assertSame(transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), leg2.getTerminal());
        assertSame(transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), leg3.getTerminal());
        // leg2/3 setter getter
        leg2.setR(1.0);
        assertEquals(1.0, leg2.getR(), 0.0);
        leg2.setX(1.1);
        assertEquals(1.1, leg2.getX(), 0.0);
        leg2.setRatedU(1.2);
        assertEquals(1.2, leg2.getRatedU(), 0.0);
        leg2.setRatedS(1.3);
        assertEquals(1.3, leg2.getRatedS(), 0.0);
        leg3.setR(1.0);
        assertEquals(1.0, leg3.getR(), 0.0);
        leg3.setX(1.1);
        assertEquals(1.1, leg3.getX(), 0.0);
        leg3.setRatedU(1.2);
        assertEquals(1.2, leg3.getRatedU(), 0.0);
        leg3.setRatedS(1.3);
        assertEquals(1.3, leg3.getRatedS(), 0.0);

        RatioTapChanger ratioTapChangerInLeg1 = createRatioTapChanger(leg1,
            transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));

        assertTrue(leg1.getOptionalRatioTapChanger().isPresent());
        assertSame(ratioTapChangerInLeg1, leg1.getRatioTapChanger());
        CurrentLimits currentLimitsInLeg1 = leg1.newCurrentLimits()
            .setPermanentLimit(100)
            .beginTemporaryLimit()
            .setName("20'")
            .setAcceptableDuration(20 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .add();
        assertSame(currentLimitsInLeg1, leg1.getCurrentLimits().orElse(null));
        ActivePowerLimits activePowerLimits1 = leg1.newActivePowerLimits()
                .setPermanentLimit(400)
                .add();
        assertSame(activePowerLimits1, leg1.getActivePowerLimits().orElse(null));
        ApparentPowerLimits apparentPowerLimits1 = leg1.newApparentPowerLimits()
                .setPermanentLimit(2.4)
                .add();
        assertSame(apparentPowerLimits1, leg1.getApparentPowerLimits().orElse(null));
        assertEquals(3, leg1.getOperationalLimits().size());

        RatioTapChanger ratioTapChangerInLeg2 = createRatioTapChanger(leg2,
            transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));

        assertTrue(leg2.hasRatioTapChanger());
        assertSame(ratioTapChangerInLeg2, leg2.getRatioTapChanger());
        CurrentLimits currentLimitsInLeg2 = leg2.newCurrentLimits()
            .setPermanentLimit(100)
            .beginTemporaryLimit()
            .setName("20'")
            .setAcceptableDuration(20 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .add();
        assertSame(currentLimitsInLeg2, leg2.getCurrentLimits().orElse(null));

        RatioTapChanger ratioTapChangerInLeg3 = createRatioTapChanger(leg3,
            transformer.getTerminal(ThreeWindingsTransformer.Side.THREE));

        assertTrue(leg3.getOptionalRatioTapChanger().isPresent());
        assertSame(ratioTapChangerInLeg3, leg3.getRatioTapChanger());
        CurrentLimits currentLimitsInLeg3 = leg3.newCurrentLimits()
            .setPermanentLimit(100)
            .beginTemporaryLimit()
            .setName("20'")
            .setAcceptableDuration(20 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .add();
        assertSame(currentLimitsInLeg3, leg3.getCurrentLimits().orElse(null));

        PhaseTapChanger phaseTapChangerInLeg1 = createPhaseTapChanger(leg1,
            transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));
        assertTrue(leg1.getOptionalPhaseTapChanger().isPresent());
        assertSame(phaseTapChangerInLeg1, leg1.getPhaseTapChanger());

        PhaseTapChanger phaseTapChangerInLeg2 = createPhaseTapChanger(leg2,
            transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));
        assertTrue(leg2.hasPhaseTapChanger());
        assertSame(phaseTapChangerInLeg2, leg2.getPhaseTapChanger());

        PhaseTapChanger phaseTapChangerInLeg3 = createPhaseTapChanger(leg3,
            transformer.getTerminal(ThreeWindingsTransformer.Side.THREE));
        assertTrue(leg3.getOptionalPhaseTapChanger().isPresent());
        assertSame(phaseTapChangerInLeg3, leg3.getPhaseTapChanger());

        // Reuse adder
        ThreeWindingsTransformer transformer2 = transformerAdder.setId(transformer.getId() + "_2").add();
        assertNotSame(transformer.getLeg1(), transformer2.getLeg1());
        assertNotSame(transformer.getLeg2(), transformer2.getLeg2());
        assertNotSame(transformer.getLeg3(), transformer2.getLeg3());

        int count = network.getThreeWindingsTransformerCount();
        transformer.remove();
        assertNull(network.getThreeWindingsTransformer("twt"));
        assertNotNull(transformer);
        assertEquals(count - 1L, network.getThreeWindingsTransformerCount());
    }

    @Test
    public void testDefaultValuesThreeWindingTransformer() {
        ThreeWindingsTransformerAdder transformerAdder = substation.newThreeWindingsTransformer()
                            .setId("twt")
                            .setName(TWT_NAME)
                            .newLeg1()
                                .setR(1.3)
                                .setX(1.4)
                                .setRatedU(1.1)
                                .setRatedS(1.2)
                                .setBus("busA")
                            .add()
                            .newLeg2()
                                .setR(2.03)
                                .setX(2.04)
                                .setRatedU(2.05)
                                .setRatedS(2.06)
                                .setConnectableBus("busB")
                            .add()
                            .newLeg3()
                                .setR(3.3)
                                .setX(3.4)
                                .setRatedU(3.5)
                                .setRatedS(3.6)
                                .setConnectableBus("busB")
                            .add();
        ThreeWindingsTransformer transformer = transformerAdder.add();

        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        assertEquals(0.0, leg1.getG(), 0.0);
        assertEquals(0.0, leg1.getB(), 0.0);

        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        assertEquals(0.0, leg2.getG(), 0.0);
        assertEquals(0.0, leg2.getB(), 0.0);

        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg1();
        assertEquals(0.0, leg3.getG(), 0.0);
        assertEquals(0.0, leg3.getB(), 0.0);

        VoltageLevel vl1 = network.getVoltageLevel("vl1");
        VoltageLevel vl2 = network.getVoltageLevel("vl2");
        assertSame(vl1, leg1.getTerminal().getVoltageLevel());
        assertSame(vl2, leg2.getTerminal().getVoltageLevel());
        assertSame(vl1, leg3.getTerminal().getVoltageLevel());
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
        ValidationException e = assertThrows(ValidationException.class, () -> substation.newThreeWindingsTransformer()
                .setId("twt")
                .setName(TWT_NAME)
                .newLeg1()
                .setR(1.3)
                .setX(1.4)
                .setG(1.6)
                .setB(1.7)
                .setRatedU(1.1)
                .setRatedS(1.2)
                .setVoltageLevel("no_substation")
                .setBus("no_substation_bus")
                .add()
                .newLeg2()
                .setR(2.03)
                .setX(2.04)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(2.05)
                .setRatedS(2.06)
                .setVoltageLevel("vl2")
                .setBus("busB")
                .add()
                .newLeg3()
                .setR(3.3)
                .setX(3.4)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(3.5)
                .setRatedS(3.6)
                .setVoltageLevel("vl2")
                .setBus("busB")
                .add()
                .add());
        assertTrue(e.getMessage().contains("3 windings transformer 'twt': the 3 windings of the transformer shall belong to the substation 'sub'"));
    }

    @Test
    public void missingSubstationContainer() {
        ValidationException e = assertThrows(ValidationException.class, () -> network.newThreeWindingsTransformer()
                .setId("twt")
                .setName(TWT_NAME)
                .newLeg1()
                .setR(1.3)
                .setX(1.4)
                .setG(1.6)
                .setB(1.7)
                .setRatedU(1.1)
                .setRatedS(1.2)
                .setVoltageLevel("vl1")
                .setBus("busA")
                .add()
                .newLeg2()
                .setR(2.03)
                .setX(2.04)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(2.05)
                .setRatedS(2.06)
                .setVoltageLevel("vl2")
                .setBus("busB")
                .add()
                .newLeg3()
                .setR(3.3)
                .setX(3.4)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(3.5)
                .setRatedS(3.6)
                .setVoltageLevel("vl2")
                .setBus("busB")
                .add()
                .add());
        assertTrue(e.getMessage().contains("3 windings transformer 'twt': the 3 windings of the transformer shall belong to a substation since there are located in voltage levels with substations"));
    }

    @Test
    public void leg1SetTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createRatioTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));
        createPhaseTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));

        leg1.getRatioTapChanger().setRegulating(true);
        ValidationException e = assertThrows(ValidationException.class, () -> leg1.getPhaseTapChanger().setRegulating(true));
        assertTrue(e.getMessage().contains(ERROR_LEG1_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED));
    }

    @Test
    public void leg2SetTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        createRatioTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));
        createPhaseTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));

        leg2.getRatioTapChanger().setRegulating(true);
        ValidationException e = assertThrows(ValidationException.class, () -> leg2.getPhaseTapChanger().setRegulating(true));
        assertTrue(e.getMessage().contains(ERROR_LEG2_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED));
    }

    @Test
    public void leg3SetTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        // First create phase
        createPhaseTapChanger(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE));
        createRatioTapChanger(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE));

        leg3.getRatioTapChanger().setRegulating(true);
        ValidationException e = assertThrows(ValidationException.class, () -> leg3.getPhaseTapChanger().setRegulating(true));
        assertTrue(e.getMessage().contains(ERROR_TRANSFORMER_LEG3_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED));
    }

    @Test
    public void ratioSetTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createRatioTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        createRatioTapChanger(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE));

        leg1.getRatioTapChanger().setRegulating(true);
        ValidationException e = assertThrows(ValidationException.class, () -> leg3.getRatioTapChanger().setRegulating(true));
        assertTrue(e.getMessage().contains(ERROR_TRANSFORMER_LEG3_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED));
    }

    @Test
    public void phaseSetTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createPhaseTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        createPhaseTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));

        leg1.getPhaseTapChanger().setRegulating(true);
        ValidationException e = assertThrows(ValidationException.class, () -> leg2.getPhaseTapChanger().setRegulating(true));
        assertTrue(e.getMessage().contains(ERROR_LEG2_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED));
    }

    @Test
    public void ratioAddTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createRatioTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE), true);

        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChanger(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), true));
        assertTrue(e.getMessage().contains(ERROR_TRANSFORMER_LEG3_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED));
    }

    @Test
    public void phaseAddTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createPhaseTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE), true);

        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), true));
        assertTrue(e.getMessage().contains(ERROR_LEG2_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED));
    }

    @Test
    public void phaseRatioAddTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        createPhaseTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), true);

        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE), true));
        assertTrue(e.getMessage().contains(ERROR_LEG1_ONLY_ONE_REGULATING_CONTROL_ENABLED_IS_ALLOWED));
    }

    @Test
    public void ratioIncorrectTapPosition() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        RatioTapChanger rtc = createRatioTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));

        ValidationException e = assertThrows(ValidationException.class, () -> rtc.setTapPosition(1000));
        assertTrue(e.getMessage().contains("3 windings transformer leg1 'twt': incorrect tap position 1000 [0, 2]"));
    }

    @Test
    public void phaseIncorrectTapPosition() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        PhaseTapChanger ptc = createPhaseTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));

        ValidationException e = assertThrows(ValidationException.class, () -> ptc.setTapPosition(100));
        assertTrue(e.getMessage().contains("3 windings transformer leg2 'twt': incorrect tap position 100 [0, 2]"));
    }

    @Test
    public void invalidRatioStepArgumentRho() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), Double.NaN, 0.0, 0.0, 0.0, 0.0));
        assertTrue(e.getMessage().contains("rho is not set"));
    }

    @Test
    public void invalidRatioStepArgumentR() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), 0.0, Double.NaN, 0.0, 0.0, 0.0));
        assertTrue(e.getMessage().contains(ERROR_R_IS_NOT_SET));
    }

    @Test
    public void invalidRatioStepArgumentX() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), 0.0, 0.0, Double.NaN, 0.0, 0.0));
        assertTrue(e.getMessage().contains(ERROR_X_IS_NOT_SET));
    }

    @Test
    public void invalidRatioStepArgumentG() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), 0.0, 0.0, 0.0, Double.NaN, 0.0));
        assertTrue(e.getMessage().contains(ERROR_G_IS_NOT_SET));
    }

    @Test
    public void invalidRatioStepArgumentB() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), 0.0, 0.0, 0.0, 0.0, Double.NaN));
        assertTrue(e.getMessage().contains(ERROR_B_IS_NOT_SET));
    }

    @Test
    public void validRatioStepArguments() {
        // Verify that other invalidPhaseStepArgument* tests are not throwing when arguments are ok
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), 0.0, 0.0, 0.0,
                0.0, 0.0);
    }

    @Test
    public void invalidPhaseStepArgumentRho() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), Double.NaN, 0.0, 0.0, 0.0, 0.0, 0.0));
        assertTrue(e.getMessage().contains("rho is not set"));
    }

    @Test
    public void invalidPhaseStepArgumentAlpha() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, Double.NaN, 0.0, 0.0, 0.0, 0.0));
        assertTrue(e.getMessage().contains("alpha is not set"));
    }

    @Test
    public void invalidPhaseStepArgumentR() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, 0.0, Double.NaN, 0.0, 0.0, 0.0));
        assertTrue(e.getMessage().contains(ERROR_R_IS_NOT_SET));
    }

    @Test
    public void invalidPhaseStepArgumentX() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, 0.0, 0.0,
            Double.NaN, 0.0, 0.0));
        assertTrue(e.getMessage().contains(ERROR_X_IS_NOT_SET));
    }

    @Test
    public void invalidPhaseStepArgumentG() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, 0.0, 0.0,
            0.0, Double.NaN, 0.0));
        assertTrue(e.getMessage().contains(ERROR_G_IS_NOT_SET));
    }

    @Test
    public void validPhaseStepArguments() {
        // Verify that other invalidPhaseStepArgument* tests are not throwing when arguments are ok
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0);
    }

    @Test
    public void invalidPhaseStepArgumentB() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, 0.0, 0.0,
            0.0, 0.0, Double.NaN));
        assertTrue(e.getMessage().contains(ERROR_B_IS_NOT_SET));
    }

    @Test
    public void invalidRatedS() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();

        ValidationException e = assertThrows(ValidationException.class, () -> leg1.setRatedS(0.0));
        assertTrue(e.getMessage().contains("Invalid value of rated S 0.0"));
    }

    @Test
    public void invalidLeg1NotSet() {
        ValidationException e = assertThrows(ValidationException.class, () -> substation.newThreeWindingsTransformer()
                .setId("twt")
                .setName(TWT_NAME)
                .add());
        assertTrue(e.getMessage().contains(ERROR_LEG1_IS_NOT_SET));
    }

    @Test
    public void invalidLeg1ArgumentVoltageLevelNotSet() {
        VoltageLevel voltageLevelNode = substation.newVoltageLevel()
                .setId("vln")
                .setName("vln")
                .setNominalV(440.0)
                .setHighVoltageLimit(400.0)
                .setLowVoltageLimit(200.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevelNode.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();

        ValidationException e = assertThrows(ValidationException.class, () -> substation.newThreeWindingsTransformer()
                .setId("twt")
                .setName(TWT_NAME)
                .newLeg1()
                .setR(1.3)
                .setX(1.4)
                .setG(1.6)
                .setB(1.7)
                .setRatedU(1.1)
                .setRatedS(1.2)
                .setNode(0)
                .add()
                .add());
        assertTrue(e.getMessage().contains("3 windings transformer leg1 in substation sub: voltage level is not set"));
    }

    @Test
    public void invalidLeg1ArgumentVoltageLevelNotFound() {
        ValidationException e = assertThrows(ValidationException.class, () -> substation.newThreeWindingsTransformer()
                .setId("twt")
                .setName(TWT_NAME)
                .newLeg1()
                .setR(1.3)
                .setX(1.4)
                .setG(1.6)
                .setB(1.7)
                .setRatedU(1.1)
                .setRatedS(1.2)
                .setVoltageLevel("invalid")
                .setConnectableBus("busA")
                .setBus("busA")
                .add()
                .add());
        assertTrue(e.getMessage().contains("3 windings transformer leg1 in substation sub: voltage level 'invalid' not found"));
    }

    @Test
    public void invalidLeg1ArgumentConnectableBusNotSet() {
        ValidationException e = assertThrows(ValidationException.class, () -> substation.newThreeWindingsTransformer()
                .setId("twt")
                .setName(TWT_NAME)
                .newLeg1()
                .setR(1.3)
                .setX(1.4)
                .setG(1.6)
                .setB(1.7)
                .setRatedU(1.1)
                .setRatedS(1.2)
                .setVoltageLevel("vl1")
                .add()
                .add());
        assertTrue(e.getMessage().contains("connectable bus is not set"));
    }

    @Test
    public void invalidLeg2NotSet() {
        ValidationException e = assertThrows(ValidationException.class, () -> substation.newThreeWindingsTransformer()
                .setId("twt")
                .setName(TWT_NAME)
                .newLeg1()
                .setR(1.3)
                .setX(1.4)
                .setG(1.6)
                .setB(1.7)
                .setRatedU(1.1)
                .setRatedS(1.2)
                .setVoltageLevel("vl1")
                .setConnectableBus("busA")
                .setBus("busA")
                .add()
                .add());
        assertTrue(e.getMessage().contains(ERROR_LEG2_IS_NOT_SET));
    }

    @Test
    public void invalidLeg3NotSet() {
        ValidationException e = assertThrows(ValidationException.class, () -> substation.newThreeWindingsTransformer()
                .setId("twt")
                .setName(TWT_NAME)
                .newLeg1()
                .setR(1.3)
                .setX(1.4)
                .setG(1.6)
                .setB(1.7)
                .setRatedU(1.1)
                .setRatedS(1.2)
                .setVoltageLevel("vl1")
                .setConnectableBus("busA")
                .setBus("busA")
                .add()
                .newLeg2()
                .setR(2.03)
                .setX(2.04)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(2.05)
                .setRatedS(2.06)
                .setVoltageLevel("vl2")
                .setConnectableBus("busB")
                .add()
                .add());
        assertTrue(e.getMessage().contains(ERROR_LEG3_IS_NOT_SET));
    }

    private ThreeWindingsTransformer createThreeWindingsTransformer() {
        return createThreeWindingsTransformerAdder().add();
    }

    private ThreeWindingsTransformerAdder createThreeWindingsTransformerAdder() {
        return substation.newThreeWindingsTransformer()
            .setId("twt")
            .setName(TWT_NAME)
            .newLeg1()
            .setR(1.3)
            .setX(1.4)
            .setG(1.6)
            .setB(1.7)
            .setRatedU(1.1)
            .setRatedS(1.2)
            .setVoltageLevel("vl1")
            .setConnectableBus("busA")
            .setBus("busA")
            .add()
            .newLeg2()
            .setR(2.03)
            .setX(2.04)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(2.05)
            .setRatedS(2.06)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .newLeg3()
            .setR(3.3)
            .setX(3.4)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(3.5)
            .setRatedS(3.6)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add();
    }

    private RatioTapChanger createRatioTapChanger(Leg leg, Terminal terminal) {
        return createRatioTapChanger(leg, terminal, false);
    }

    private RatioTapChanger createRatioTapChanger(Leg leg, Terminal terminal, boolean regulating) {
        return leg.newRatioTapChanger()
            .setTargetV(200.0)
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

    private RatioTapChanger createRatioTapChangerOneStep(Leg leg, Terminal terminal, double rho, double r, double x, double g, double b) {
        return leg.newRatioTapChanger()
            .setTargetV(200.0)
            .setLoadTapChangingCapabilities(false)
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(false)
            .setRegulationTerminal(terminal)
            .setTargetDeadband(0.5)
            .beginStep()
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setRho(rho)
            .endStep()
            .add();
    }

    private PhaseTapChanger createPhaseTapChanger(Leg leg, Terminal terminal) {
        return createPhaseTapChanger(leg, terminal, false);
    }

    private PhaseTapChanger createPhaseTapChanger(Leg leg, Terminal terminal, boolean regulating) {
        return leg.newPhaseTapChanger()
            .setRegulationValue(200.0)
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(regulating)
            .setRegulationTerminal(terminal)
            .setRegulationMode(RegulationMode.ACTIVE_POWER_CONTROL)
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

    private PhaseTapChanger createPhaseTapChangerOneStep(Leg leg, Terminal terminal, double rho, double alpha, double r, double x, double g, double b) {
        return leg.newPhaseTapChanger()
            .setRegulationValue(200.0)
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(false)
            .setRegulationTerminal(terminal)
            .setRegulationMode(RegulationMode.ACTIVE_POWER_CONTROL)
            .setTargetDeadband(0.5)
            .beginStep()
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setRho(rho)
                .setAlpha(alpha)
            .endStep()
            .add();
    }

    @Test
    public void invalidLeg1ArgumentsR() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg1(Double.NaN, 2.0, 3.0, 4.0, 5.0));
        assertTrue(e.getMessage().contains(ERROR_R_IS_NOT_SET));
    }

    @Test
    public void invalidLeg1ArgumentsX() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg1(1.0, Double.NaN, 3.0, 4.0, 5.0));
        assertTrue(e.getMessage().contains(ERROR_X_IS_NOT_SET));
    }

    @Test
    public void validLeg1Arguments() {
        //Verify that other invalidLeg1Arguments* tests are not throwing when arguments are ok
        createThreeWindingsTransformerWithLeg1(1.0, 2.0, 3.0, 4.0, 5.0);
    }

    @Test
    public void invalidLeg1ArgumentsG() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg1(1.0, 2.0, Double.NaN, 4.0, 5.0));
        assertTrue(e.getMessage().contains(ERROR_G_IS_NOT_SET));
    }

    @Test
    public void invalidLeg1ArgumentsB() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg1(1.0, 2.0, 3.0, Double.NaN, 5.0));
        assertTrue(e.getMessage().contains(ERROR_B_IS_NOT_SET));
    }

    @Test
    public void invalidLeg1ArgumentsRatedU() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg1(1.0, 2.0, 3.0, 4.0, Double.NaN));
        assertTrue(e.getMessage().contains(ERROR_RATED_U_IS_INVALID));
    }

    private void createThreeWindingsTransformerWithLeg1(double r, double x, double g, double b, double ratedU) {
        substation.newThreeWindingsTransformer()
                    .setId("twt")
                    .setName(TWT_NAME)
                    .newLeg1()
                        .setR(r)
                        .setX(x)
                        .setG(g)
                        .setB(b)
                        .setRatedU(ratedU)
                        .setVoltageLevel("vl1")
                        .setConnectableBus("busA")
                    .add()
                    .newLeg2()
                        .setR(2.03)
                        .setX(2.04)
                        .setRatedU(2.05)
                        .setVoltageLevel("vl2")
                        .setConnectableBus("busB")
                    .add()
                    .newLeg3()
                        .setR(3.3)
                        .setX(3.4)
                        .setRatedU(3.5)
                        .setVoltageLevel("vl2")
                        .setConnectableBus("busB")
                    .add()
                .add();
    }

    @Test
    public void invalidLeg2ArgumentsR() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg2(Double.NaN, 2.2, 3.2, 4.2, 5.2));
        assertTrue(e.getMessage().contains(ERROR_R_IS_NOT_SET));
    }

    @Test
    public void invalidLeg2ArgumentsX() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg2(1.2, Double.NaN, 3.2, 4.2, 5.2));
        assertTrue(e.getMessage().contains(ERROR_X_IS_NOT_SET));
    }

    @Test
    public void invalidLeg2ArgumentsG() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg2(1.2, 2.2, Double.NaN, 4.2, 5.2));
        assertTrue(e.getMessage().contains(ERROR_G_IS_NOT_SET));
    }

    @Test
    public void invalidLeg2ArgumentsB() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg2(1.2, 2.2, 3.2, Double.NaN, 5.2));
        assertTrue(e.getMessage().contains(ERROR_B_IS_NOT_SET));
    }

    @Test
    public void invalidLeg2ArgumentsRatedU() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg2(1.2, 2.2, 3.2, 4.2, Double.NaN));
        assertTrue(e.getMessage().contains(ERROR_RATED_U_IS_INVALID));
    }

    @Test
    public void validLeg2Arguments() {
        //Verify that other invalidLeg2Arguments* tests are not throwing when arguments are ok
        createThreeWindingsTransformerWithLeg2(1.2, 2.2, 3.2, 4.2, 5.2);
    }

    private void createThreeWindingsTransformerWithLeg2(double r, double x, double g, double b, double ratedU) {
        substation.newThreeWindingsTransformer()
            .setId("twt")
            .setName(TWT_NAME)
            .newLeg1()
            .setR(2.03)
            .setX(2.04)
            .setG(0.05)
            .setB(0.03)
            .setRatedU(2.05)
            .setVoltageLevel("vl1")
            .setConnectableBus("busA")
            .add()
            .newLeg2()
            .setR(r)
            .setX(x)
            .setG(g)
            .setB(b)
            .setRatedU(ratedU)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .newLeg3()
            .setR(3.3)
            .setX(3.4)
            .setRatedU(3.5)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .add();
    }

    @Test
    public void invalidLeg3ArgumentsR() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg3(Double.NaN, 2.3, 3.3, 4.3, 5.3));
        assertTrue(e.getMessage().contains(ERROR_R_IS_NOT_SET));
    }

    @Test
    public void invalidLeg3ArgumentsX() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg3(1.3, Double.NaN, 3.3, 4.3, 5.3));
        assertTrue(e.getMessage().contains(ERROR_X_IS_NOT_SET));
    }

    @Test
    public void invalidLeg3ArgumentsG() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg3(1.3, 2.3, Double.NaN, 4.3, 5.3));
        assertTrue(e.getMessage().contains(ERROR_G_IS_NOT_SET));
    }

    @Test
    public void invalidLeg3ArgumentsB() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg3(1.3, 2.3, 3.3, Double.NaN, 5.3));
        assertTrue(e.getMessage().contains(ERROR_B_IS_NOT_SET));
    }

    @Test
    public void invalidLeg3ArgumentsRatedU() {
        ValidationException e = assertThrows(ValidationException.class, () -> createThreeWindingsTransformerWithLeg3(1.3, 2.3, 3.3, 4.3, Double.NaN));
        assertTrue(e.getMessage().contains(ERROR_RATED_U_IS_INVALID));
    }

    @Test
    public void validLeg3Arguments() {
        //Verify that other invalidLeg3Arguments* tests are not throwing when arguments are ok
        createThreeWindingsTransformerWithLeg3(1.3, 2.3, 3.3, 4.3, 5.3);
    }

    private void createThreeWindingsTransformerWithLeg3(double r, double x, double g, double b, double ratedU) {
        substation.newThreeWindingsTransformer()
            .setId("twt")
            .setName(TWT_NAME)
            .newLeg1()
            .setR(2.03)
            .setX(2.04)
            .setG(0.05)
            .setB(0.03)
            .setRatedU(2.05)
            .setVoltageLevel("vl1")
            .setConnectableBus("busA")
            .add()
            .newLeg2()
            .setR(3.3)
            .setX(3.4)
            .setG(0.025)
            .setB(0.015)
            .setRatedU(3.5)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .newLeg3()
            .setR(r)
            .setX(x)
            .setG(g)
            .setB(b)
            .setRatedU(ratedU)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .add();
    }
}
