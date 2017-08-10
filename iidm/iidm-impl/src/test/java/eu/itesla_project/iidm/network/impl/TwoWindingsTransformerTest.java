/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import eu.itesla_project.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class TwoWindingsTransformerTest {
    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        TwoWindingsTransformer twoWindingsTransformer = network.getTwoWindingsTransformer("NGEN_NHV1");
        assertNotNull(twoWindingsTransformer);
        float r = 0.5f;
        twoWindingsTransformer.setR(r);
        assertEquals(r, twoWindingsTransformer.getR(), 0.0f);
        float b = 1.0f;
        twoWindingsTransformer.setB(b);
        assertEquals(b, twoWindingsTransformer.getB(), 0.0f);
        float g = 2.0f;
        twoWindingsTransformer.setG(g);
        assertEquals(g, twoWindingsTransformer.getG(), 0.0f);
        float x = 4.0f;
        twoWindingsTransformer.setX(x);
        assertEquals(x, twoWindingsTransformer.getX(), 0.0f);
        float ratedU1 = 8.0f;
        twoWindingsTransformer.setRatedU1(ratedU1);
        assertEquals(ratedU1, twoWindingsTransformer.getRatedU1(), 0.0f);
        float ratedU2 = 16.0f;
        twoWindingsTransformer.setRatedU2(ratedU2);
        assertEquals(ratedU2, twoWindingsTransformer.getRatedU2(), 0.0f);
        assertEquals(ConnectableType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformer.getType());
        Substation substationP1 = network.getSubstation("P1");
        assertEquals(substationP1, twoWindingsTransformer.getSubstation());

    }

    @Test
    public void testSetterGetterOfTapChanger() {
        Network network = FictitiousSwitchFactory.create();
        TwoWindingsTransformer twoWindingsTransformer = network.getTwoWindingsTransformer("CI");
        PhaseTapChanger phaseTapChanger = twoWindingsTransformer.getPhaseTapChanger();
        assertNotNull(phaseTapChanger);
        PhaseTapChangerStep phaseTapChangerStep = phaseTapChanger.getStep(0);
        phaseTapChangerStep.setAlpha(5.0f);
        assertEquals(5.0f, phaseTapChangerStep.getAlpha(), 0.0f);
        float regulationValue = 1.0f;

        phaseTapChanger.setRegulationValue(regulationValue);
        assertEquals(regulationValue, phaseTapChanger.getRegulationValue(), 0.0f);
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
        assertEquals(PhaseTapChanger.RegulationMode.FIXED_TAP, phaseTapChanger.getRegulationMode());

        Terminal terminal = twoWindingsTransformer.getTerminal(TwoTerminalsConnectable.Side.ONE);
        assertEquals(phaseTapChanger, phaseTapChanger.setRegulationTerminal(terminal));
        assertEquals(terminal, phaseTapChanger.getRegulationTerminal());
        phaseTapChanger.remove();
        assertNull(twoWindingsTransformer.getPhaseTapChanger());

        float targetV = 220.0f;
        RatioTapChanger ratioTapChanger = twoWindingsTransformer.newRatioTapChanger()
                .setTargetV(targetV)
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(0)
                .setTapPosition(0)
                .setRegulating(false)
                .setRegulationTerminal(twoWindingsTransformer.getTerminal(TwoTerminalsConnectable.Side.ONE))
                .beginStep().setR(39.78473f).setX(39.784725f).setG(0.0f).setB(0.0f).setRho(1.0f).endStep()
                .beginStep().setR(39.78474f).setX(39.784726f).setG(0.0f).setB(0.0f).setRho(1.0f).endStep()
                .beginStep().setR(39.78475f).setX(39.784727f).setG(0.0f).setB(0.0f).setRho(1.0f).endStep()
                .add();
        assertEquals(targetV, ratioTapChanger.getTargetV(), 0.0f);
        assertEquals(0, ratioTapChanger.getLowTapPosition());
        ratioTapChanger.setTargetV(110.0f);
        ratioTapChanger.setRegulating(true);
        ratioTapChanger.setRegulationTerminal(terminal);
        assertEquals(terminal, ratioTapChanger.getRegulationTerminal());
        assertEquals(0, ratioTapChanger.getTapPosition());
        assertFalse(ratioTapChanger.hasLoadTapChangingCapabilities());
        ratioTapChanger.remove();
        assertNull(twoWindingsTransformer.getRatioTapChanger());

        assertEquals(3, ratioTapChanger.getStepCount());
        RatioTapChangerStep step = ratioTapChanger.getStep(0);
        float stepR = 10.0f;
        float stepX = 20.0f;
        float stepG = 30.0f;
        float stepB = 40.0f;
        float stepRho = 50.0f;
        step.setR(stepR);
        assertEquals(stepR, step.getR(), 0.0f);
        step.setX(stepX);
        assertEquals(stepX, step.getX(), 0.0f);
        step.setG(stepG);
        assertEquals(stepG, step.getG(), 0.0f);
        step.setB(stepB);
        assertEquals(stepB, step.getB(), 0.0f);
        step.setRho(stepRho);
        assertEquals(stepRho, step.getRho(), 0.0f);
    }
}
