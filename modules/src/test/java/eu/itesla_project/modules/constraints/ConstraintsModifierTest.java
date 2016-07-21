/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.modules.security.LimitViolation;
import eu.itesla_project.modules.security.LimitViolationType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ConstraintsModifierTest {

    private Network network;
    private List<LimitViolation> violations;
    private ConstraintsModifierConfig config;

    @Before
    public void setUp() throws Exception {
        network = ConstraintsModifierTestUtils.getNetwork();
        violations = ConstraintsModifierTestUtils.getViolations(network);
        List<LimitViolationType> violationTypes = Arrays.asList(LimitViolationType.CURRENT, 
                LimitViolationType.HIGH_VOLTAGE,
                LimitViolationType.LOW_VOLTAGE);
        config = new ConstraintsModifierConfig(ConstraintsModifierConfig.DEFAULT_COUNTRY, violationTypes);
    }

    private void checkOriginalNetworkLimits() {
        Line line = network.getLine(ConstraintsModifierTestUtils.LINE_ID);
        assertEquals(line.getCurrentLimits1().getPermanentLimit(), ConstraintsModifierTestUtils.CURRENT_LIMIT, 0);
        VoltageLevel voltageLevel1 = network.getVoltageLevel(ConstraintsModifierTestUtils.VOLTAGE_LEVEL_1_ID);
        assertEquals(voltageLevel1.getHighVoltageLimit(), ConstraintsModifierTestUtils.HIGH_VOLTAGE_LIMIT, 0);
        VoltageLevel voltageLevel2 = network.getVoltageLevel(ConstraintsModifierTestUtils.VOLTAGE_LEVEL_2_ID);
        assertEquals(voltageLevel2.getLowVoltageLimit(), ConstraintsModifierTestUtils.LOW_VOLTAGE_LIMIT, 0);
    }

    private void checkModifiedNetworkLimits(int margin) {
        Line line = network.getLine(ConstraintsModifierTestUtils.LINE_ID);
        float newCurrentLimit = ConstraintsModifierTestUtils.NEW_CURRENT_LIMIT + (ConstraintsModifierTestUtils.CURRENT_LIMIT * margin / 100);
        assertEquals(newCurrentLimit, line.getCurrentLimits1().getPermanentLimit(), 0);
        VoltageLevel voltageLevel1 = network.getVoltageLevel(ConstraintsModifierTestUtils.VOLTAGE_LEVEL_1_ID);
        float newHighVoltageLimit = ConstraintsModifierTestUtils.NEW_HIGH_VOLTAGE_LIMIT + (ConstraintsModifierTestUtils.HIGH_VOLTAGE_LIMIT * margin / 100);
        assertEquals(newHighVoltageLimit, voltageLevel1.getHighVoltageLimit(), 0);
        VoltageLevel voltageLevel2 = network.getVoltageLevel(ConstraintsModifierTestUtils.VOLTAGE_LEVEL_2_ID);
        float newLowVoltageLimit = ConstraintsModifierTestUtils.NEW_LOW_VOLTAGE_LIMIT - (ConstraintsModifierTestUtils.LOW_VOLTAGE_LIMIT * margin / 100);
        assertEquals(newLowVoltageLimit, voltageLevel2.getLowVoltageLimit(), 0);
    }

    @Test
    public void testNoMargin() throws Exception {
        checkOriginalNetworkLimits();

        ConstraintsModifier constraintsModifier = new ConstraintsModifier(network, config);
        constraintsModifier.looseConstraints(StateManager.INITIAL_STATE_ID);

        checkModifiedNetworkLimits(0);
    }

    @Test
    public void testWithMargin() throws Exception {
        int margin = 3;

        checkOriginalNetworkLimits();

        ConstraintsModifier constraintsModifier = new ConstraintsModifier(network, config);
        constraintsModifier.looseConstraints(StateManager.INITIAL_STATE_ID, margin);

        checkModifiedNetworkLimits(margin);
    }

    @Test
    public void testWithViolationsNoMargin() throws Exception {
        checkOriginalNetworkLimits();

        ConstraintsModifier constraintsModifier = new ConstraintsModifier(network, config);
        constraintsModifier.looseConstraints(StateManager.INITIAL_STATE_ID, violations);

        checkModifiedNetworkLimits(0);
    }

    @Test
    public void testWithViolationsAndMargin() throws Exception {
        int margin = 3;

        checkOriginalNetworkLimits();

        ConstraintsModifier constraintsModifier = new ConstraintsModifier(network, config);
        constraintsModifier.looseConstraints(StateManager.INITIAL_STATE_ID, violations, margin);

        checkModifiedNetworkLimits(margin);
    }

    @Test
    public void testWithViolationsAndMarginApplyBasecase() throws Exception {
        int margin = 3;

        checkOriginalNetworkLimits();

        String stateId = "0";
        network.getStateManager().cloneState(StateManager.INITIAL_STATE_ID, stateId);
        network.getStateManager().setWorkingState(stateId);
        checkOriginalNetworkLimits();

        ConstraintsModifier constraintsModifier = new ConstraintsModifier(network, config);
        constraintsModifier.looseConstraints(stateId, violations, margin, true);

        checkModifiedNetworkLimits(margin);

        network.getStateManager().setWorkingState(StateManager.INITIAL_STATE_ID);
        checkModifiedNetworkLimits(margin);

        network.getStateManager().removeState(stateId);
    }

    @Test
    public void testWithMarginApplyBasecase() throws Exception {
        int margin = 3;

        checkOriginalNetworkLimits();

        String stateId = "0";
        network.getStateManager().cloneState(StateManager.INITIAL_STATE_ID, stateId);
        network.getStateManager().setWorkingState(stateId);
        checkOriginalNetworkLimits();

        ConstraintsModifier constraintsModifier = new ConstraintsModifier(network, config);
        constraintsModifier.looseConstraints(stateId, margin, true);

        checkModifiedNetworkLimits(margin);

        network.getStateManager().setWorkingState(StateManager.INITIAL_STATE_ID);
        checkModifiedNetworkLimits(margin);

        network.getStateManager().removeState(stateId);
    }

    @Test
    public void testWithNullValues() throws Exception {
        ConstraintsModifier constraintsModifier = new ConstraintsModifier(network, config);
        try {
            constraintsModifier.looseConstraints(null, violations);
            fail();
        } catch (Throwable e) {

        }
        try {
            constraintsModifier.looseConstraints(StateManager.INITIAL_STATE_ID, null);
            fail();
        } catch (Throwable e) {
        }
    }

    @Test
    public void testWithWrongState() throws Exception {
        ConstraintsModifier constraintsModifier = new ConstraintsModifier(network, config);
        try {
            constraintsModifier.looseConstraints("wrongState", violations);
            fail();
        } catch (Throwable e) {
        }
    }

}
