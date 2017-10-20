/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import groovy.lang.GroovyCodeSource;
import groovy.lang.MissingMethodException;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ActionDslLoaderTest {

    @org.junit.Rule
    public final ExpectedException exception = ExpectedException.none();

    private Network network;

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
    }

    @Test
    public void test() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/actions.groovy"))).load(network);

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
        assertEquals(0, action.getTasks().size());
    }

    @Test
    public void testDslExtension() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/actions2.groovy"))).load(network);
        Action another = actionDb.getAction("anotherAction");
        exception.expect(RuntimeException.class);
        exception.expectMessage("Switch 'switchId' not found");
        another.run(network, null);
    }

    @Test
    public void testFixTapDslExtension() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/actions2.groovy"))).load(network);
        Action fixedTapAction = actionDb.getAction("fixedTap");
        assertNotNull(fixedTapAction);
        addPhaseShifter();
        PhaseTapChanger phaseTapChanger = network.getTwoWindingsTransformer("NGEN_NHV1").getPhaseTapChanger();
        assertEquals(0, phaseTapChanger.getTapPosition());
        assertTrue(phaseTapChanger.isRegulating());
        assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, phaseTapChanger.getRegulationMode());
        fixedTapAction.run(network, null);
        assertEquals(1, phaseTapChanger.getTapPosition());
        assertEquals(PhaseTapChanger.RegulationMode.FIXED_TAP, phaseTapChanger.getRegulationMode());
        assertFalse(phaseTapChanger.isRegulating());
    }

    @Test
    public void testUnvalidate() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/actions2.groovy"))).load(network);
        Action someAction = actionDb.getAction("someAction");
        exception.expect(ActionDslException.class);
        exception.expectMessage("Dsl extension task(closeSwitch) is forbidden in task script");
        someAction.run(network, null);
    }

    @Test
    public void testUnKnownMethodInScript() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/actions2.groovy"))).load(network);
        Action someAction = actionDb.getAction("missingMethod");
        exception.expect(MissingMethodException.class);
        someAction.run(network, null);
    }

    private void addPhaseShifter() {
        network.getTwoWindingsTransformer("NGEN_NHV1").newPhaseTapChanger()
                .setTapPosition(0)
                .setLowTapPosition(0)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationTerminal(network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2())
                .setRegulationValue(1.0f)
                .beginStep()
                    .setR(1.0f)
                    .setX(2.0f)
                    .setG(3.0f)
                    .setB(4.0f)
                    .setAlpha(5.0f)
                    .setRho(6.0f)
                .endStep()
                .beginStep()
                    .setR(1.0f)
                    .setX(2.0f)
                    .setG(3.0f)
                    .setB(4.0f)
                    .setAlpha(5.0f)
                    .setRho(6.0f)
                .endStep()
                .add();
    }
}
