/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl;

import com.powsybl.iidm.modification.PhaseShifterShiftTap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import groovy.lang.GroovyCodeSource;
import groovy.lang.MissingMethodException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ActionDslLoaderTest {

    private Network network;
    private Supplier<ActionDslLoader> loaderSupplier1;
    private Supplier<ActionDslLoader> loaderSupplier2;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.create();
        loaderSupplier1 = () -> new ActionDslLoader(new GroovyCodeSource(Objects.requireNonNull(ActionDslLoaderTest.class.getResource("/actions.groovy"))));
        loaderSupplier2 = () -> new ActionDslLoader(new GroovyCodeSource(Objects.requireNonNull(ActionDslLoaderTest.class.getResource("/actions2.groovy"))));
    }

    @Test
    void test() {
        ActionDb actionDb = loaderSupplier1.get().load(network);

        assertEquals(2, actionDb.getContingencies().size());
        Contingency contingency = actionDb.getContingency("contingency1");
        ContingencyElement element = contingency.getElements().iterator().next();
        assertEquals("NHV1_NHV2_1", element.getId());

        contingency = actionDb.getContingency("contingency2");
        element = contingency.getElements().iterator().next();
        assertEquals("GEN", element.getId());

        assertEquals(1, actionDb.getRules().size());
        Rule rule = actionDb.getRules().iterator().next();
        assertEquals("rule", rule.getId());
        assertEquals("rule description", rule.getDescription());
        assertTrue(rule.getActions().contains("action"));
        assertEquals(2, rule.getLife());

        Action action = actionDb.getAction("action");
        assertEquals("action", action.getId());
        assertEquals("action description", action.getDescription());
        assertEquals(0, action.getModifications().size());
    }

    @Test
    void testBackwardCompatibility() {
        ActionDb actionDb = loaderSupplier2.get().load(network);
        Action fixedTapAction = actionDb.getAction("backwardCompatibility");
        assertNotNull(fixedTapAction);
        addPhaseShifter(0);
        PhaseTapChanger phaseTapChanger = network.getTwoWindingsTransformer("NGEN_NHV1").getPhaseTapChanger();
        assertEquals(0, phaseTapChanger.getTapPosition());
        assertTrue(phaseTapChanger.isRegulating());
        assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, phaseTapChanger.getRegulationMode());
        fixedTapAction.run(network);
        assertEquals(2, phaseTapChanger.getTapPosition());
        assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, phaseTapChanger.getRegulationMode());
        assertFalse(phaseTapChanger.isRegulating());
    }

    @Test
    void testDslExtension() {
        ActionDb actionDb = loaderSupplier2.get().load(network);
        Action another = actionDb.getAction("anotherAction");
        RuntimeException e = assertThrows(RuntimeException.class, () -> another.run(network));
        assertTrue(e.getMessage().contains("Switch 'switchId' not found"));
    }

    @Test
    void testFixTapDslExtension() {
        ActionDb actionDb = loaderSupplier2.get().load(network);
        Action fixedTapAction = actionDb.getAction("fixedTap");
        assertNotNull(fixedTapAction);
        addPhaseShifter(0);
        PhaseTapChanger phaseTapChanger = network.getTwoWindingsTransformer("NGEN_NHV1").getPhaseTapChanger();
        assertEquals(0, phaseTapChanger.getTapPosition());
        assertTrue(phaseTapChanger.isRegulating());
        assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, phaseTapChanger.getRegulationMode());
        fixedTapAction.run(network);
        assertEquals(1, phaseTapChanger.getTapPosition());
        assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, phaseTapChanger.getRegulationMode());
        assertFalse(phaseTapChanger.isRegulating());
    }

    private static List<DeltaTapData> provideParams() {
        return Arrays.asList(
                new DeltaTapData(1, 1, 0, "deltaTap0"),
                new DeltaTapData(1, 2, 1, "deltaTap1"),
                new DeltaTapData(1, 3, 2, "deltaTap2"),
                new DeltaTapData(1, 3, 3, "deltaTap3"),
                new DeltaTapData(1, 3, 10, "deltaTap10"),
                new DeltaTapData(1, 0, -1, "deltaTapMinus1"),
                new DeltaTapData(1, 0, -2, "deltaTapMinus2"),
                new DeltaTapData(1, 0, -10, "deltaTapMinus10")
        );
    }

    @Test
    void testDeltaTapDslExtension() {
        for (DeltaTapData data : provideParams()) {
            ActionDb actionDb = loaderSupplier2.get().load(network);
            Action deltaTapAction = actionDb.getAction(data.getTestName());
            assertNotNull(deltaTapAction);
            assertEquals(data.getDeltaTap(), ((PhaseShifterShiftTap) deltaTapAction.getModifications().get(0)).getTapDelta());
            addPhaseShifter(data.getInitTapPosition());
            PhaseTapChanger phaseTapChanger = network.getTwoWindingsTransformer("NGEN_NHV1").getPhaseTapChanger();
            assertEquals(1, phaseTapChanger.getTapPosition());
            assertTrue(phaseTapChanger.isRegulating());
            assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, phaseTapChanger.getRegulationMode());
            deltaTapAction.run(network);
            assertEquals(data.getExpectedTapPosition(), phaseTapChanger.getTapPosition());
            assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, phaseTapChanger.getRegulationMode());
            assertFalse(phaseTapChanger.isRegulating());
        }
    }

    @Test
    void testInvalidTransformerId() {
        ActionDb actionDb = loaderSupplier2.get().load(network);
        Action deltaTapAction = actionDb.getAction("InvalidTransformerId");
        assertNotNull(deltaTapAction);
        assertEquals(-10, ((PhaseShifterShiftTap) deltaTapAction.getModifications().get(0)).getTapDelta());
        PowsyblException e = assertThrows(PowsyblException.class, () -> deltaTapAction.run(network));
        assertTrue(e.getMessage().contains("Transformer 'NHV1_NHV2_1' not found"));
    }

    @Test
    void testTransformerWithoutPhaseShifter() {
        ActionDb actionDb = loaderSupplier2.get().load(network);
        Action deltaTapAction = actionDb.getAction("TransformerWithoutPhaseShifter");
        assertNotNull(deltaTapAction);
        assertEquals(-10, ((PhaseShifterShiftTap) deltaTapAction.getModifications().get(0)).getTapDelta());
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> deltaTapAction.run(network));
        assertTrue(e1.getMessage().contains("Transformer 'NGEN_NHV1' is not a phase shifter"));
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> deltaTapAction.run(network, true));
        assertTrue(e2.getMessage().contains("Transformer 'NGEN_NHV1' is not a phase shifter"));
    }

    @Test
    void testUnvalidate() {
        ActionDb actionDb = loaderSupplier2.get().load(network);
        Action someAction = actionDb.getAction("someAction");
        ActionDslException e = assertThrows(ActionDslException.class, () -> someAction.run(network));
        assertTrue(e.getMessage().contains("Dsl extension task(closeSwitch) is forbidden in task script"));
    }

    @Test
    void testUnKnownMethodInScript() {
        ActionDb actionDb = loaderSupplier2.get().load(network);
        Action someAction = actionDb.getAction("missingMethod");
        assertThrows(MissingMethodException.class, () -> someAction.run(network));
    }

    private static <T> ArgumentMatcher<T> matches(Function<T, Boolean> predicate) {
        return new ArgumentMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return predicate.apply((T) o);
            }
        };
    }

    @Test
    void testHandler() {
        ActionDslHandler handler = mock(ActionDslHandler.class);
        loaderSupplier1.get().load(network, handler, null);

        verify(handler, times(1)).addAction(argThat(matches(a -> a.getId().equals("action"))));
        verify(handler, times(2)).addContingency(any());
        verify(handler, times(1)).addContingency(argThat(matches(c -> c.getId().equals("contingency1"))));
        verify(handler, times(1)).addContingency(argThat(matches(c -> c.getId().equals("contingency2"))));
        verify(handler, times(1)).addRule(argThat(matches(c -> c.getId().equals("rule"))));
    }

    private void addPhaseShifter(int initTapPosition) {
        network.getTwoWindingsTransformer("NGEN_NHV1").newPhaseTapChanger()
                .setTapPosition(initTapPosition)
                .setLowTapPosition(0)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationTerminal(network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2())
                .setRegulationValue(1.0)
                .setTargetDeadband(0)
                .beginStep()
                    .setR(1.0)
                    .setX(2.0)
                    .setG(3.0)
                    .setB(4.0)
                    .setAlpha(5.0)
                    .setRho(6.0)
                .endStep()
                .beginStep()
                    .setR(1.0)
                    .setX(2.0)
                    .setG(3.0)
                    .setB(4.0)
                    .setAlpha(5.0)
                    .setRho(6.0)
                .endStep()
                .beginStep()
                    .setR(1.0)
                    .setX(2.0)
                    .setG(3.0)
                    .setB(4.0)
                    .setAlpha(5.0)
                    .setRho(6.0)
                .endStep()
                .beginStep()
                    .setR(1.0)
                    .setX(2.0)
                    .setG(3.0)
                    .setB(4.0)
                    .setAlpha(5.0)
                    .setRho(6.0)
                .endStep()
                .add();
    }

    private static final class DeltaTapData {

        private final int iniTapPosition;
        private final int expectedTapPosition;
        private final int deltaTap;
        private final String testName;

        private DeltaTapData(int iniTapPosition, int expectedTapPosition, int deltaTap, String testName) {
            this.iniTapPosition = iniTapPosition;
            this.expectedTapPosition = expectedTapPosition;
            this.deltaTap = deltaTap;
            this.testName = testName;
        }

        private int getInitTapPosition() {
            return iniTapPosition;
        }

        private int getExpectedTapPosition() {
            return expectedTapPosition;
        }

        private int getDeltaTap() {
            return deltaTap;
        }

        private String getTestName() {
            return testName;
        }
    }

}
