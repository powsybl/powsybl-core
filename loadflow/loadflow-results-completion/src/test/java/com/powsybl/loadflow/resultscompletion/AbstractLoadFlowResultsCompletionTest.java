/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Terminal.BusView;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractLoadFlowResultsCompletionTest {

    protected String lineId = "line";
    protected double lineP1 = -42.051187;
    protected double lineQ1 = -19.516002;
    protected double lineP2 = 42.0684589;
    protected double lineQ2 = 18.8650876;
    protected Terminal lineTerminal1;
    protected Terminal lineTerminal2;
    protected Line line;

    protected String twtId = "twt";
    protected double twtP1 = 436.548434;
    protected double twtQ1 = 43.472170;
    protected double twtP2 = -436.4087165;
    protected double twtQ2 = 11.837290;
    protected Terminal twtTerminal1;
    protected Terminal twtTerminal2;
    protected TwoWindingsTransformer transformer;

    protected String shuntId = "shunt";
    protected double shuntQ = -21.2566;
    protected Terminal shuntTerminal;
    protected ShuntCompensator shunt;

    protected Network network;

    @Before
    public void setUp() {
        Bus lineBus1 = Mockito.mock(Bus.class);
        Mockito.when(lineBus1.getV()).thenReturn(225.105);
        Mockito.when(lineBus1.getAngle()).thenReturn(Math.toDegrees(0.0765428));
        Mockito.when(lineBus1.isInMainConnectedComponent()).thenReturn(true);

        Bus lineBus2 = Mockito.mock(Bus.class);
        Mockito.when(lineBus2.getV()).thenReturn(225.392);
        Mockito.when(lineBus2.getAngle()).thenReturn(Math.toDegrees(0.0784353));
        Mockito.when(lineBus2.isInMainConnectedComponent()).thenReturn(true);

        BusView lineBusView1 = Mockito.mock(BusView.class);
        Mockito.when(lineBusView1.getBus()).thenReturn(lineBus1);

        BusView lineBusView2 = Mockito.mock(BusView.class);
        Mockito.when(lineBusView2.getBus()).thenReturn(lineBus2);

        lineTerminal1 = Mockito.mock(Terminal.class);
        Mockito.when(lineTerminal1.isConnected()).thenReturn(true);
        Mockito.when(lineTerminal1.getP()).thenReturn(lineP1);
        Mockito.when(lineTerminal1.getQ()).thenReturn(lineQ1);
        Mockito.when(lineTerminal1.getBusView()).thenReturn(lineBusView1);

        lineTerminal2 = Mockito.mock(Terminal.class);
        Mockito.when(lineTerminal2.isConnected()).thenReturn(true);
        Mockito.when(lineTerminal2.getP()).thenReturn(lineP2);
        Mockito.when(lineTerminal2.getQ()).thenReturn(lineQ2);
        Mockito.when(lineTerminal2.getBusView()).thenReturn(lineBusView2);

        line = Mockito.mock(Line.class);
        Mockito.when(line.getId()).thenReturn(lineId);
        Mockito.when(line.getTerminal1()).thenReturn(lineTerminal1);
        Mockito.when(line.getTerminal2()).thenReturn(lineTerminal2);
        Mockito.when(line.getTerminal(Side.ONE)).thenReturn(lineTerminal1);
        Mockito.when(line.getTerminal(Side.TWO)).thenReturn(lineTerminal2);
        Mockito.when(line.getR()).thenReturn(0.409999);
        Mockito.when(line.getX()).thenReturn(2.47000);
        Mockito.when(line.getG1()).thenReturn(0.0);
        Mockito.when(line.getG2()).thenReturn(0.0);
        Mockito.when(line.getB1()).thenReturn(7.44000e-06);
        Mockito.when(line.getB2()).thenReturn(7.44000e-06);


        Bus twtBus1 = Mockito.mock(Bus.class);
        Mockito.when(twtBus1.getV()).thenReturn(408.266);
        Mockito.when(twtBus1.getAngle()).thenReturn(Math.toDegrees(-0.1662));
        Mockito.when(twtBus1.isInMainConnectedComponent()).thenReturn(true);

        Bus twtBus2 = Mockito.mock(Bus.class);
        Mockito.when(twtBus2.getV()).thenReturn(406.276);
        Mockito.when(twtBus2.getAngle()).thenReturn(Math.toDegrees(-0.292572));
        Mockito.when(twtBus2.isInMainConnectedComponent()).thenReturn(true);

        BusView twtBusView1 = Mockito.mock(BusView.class);
        Mockito.when(twtBusView1.getBus()).thenReturn(twtBus1);

        BusView twtBusView2 = Mockito.mock(BusView.class);
        Mockito.when(twtBusView2.getBus()).thenReturn(twtBus2);

        twtTerminal1 = Mockito.mock(Terminal.class);
        Mockito.when(twtTerminal1.isConnected()).thenReturn(true);
        Mockito.when(twtTerminal1.getP()).thenReturn(twtP1);
        Mockito.when(twtTerminal1.getQ()).thenReturn(twtQ1);
        Mockito.when(twtTerminal1.getBusView()).thenReturn(twtBusView1);

        twtTerminal2 = Mockito.mock(Terminal.class);
        Mockito.when(twtTerminal2.isConnected()).thenReturn(true);
        Mockito.when(twtTerminal2.getP()).thenReturn(twtP2);
        Mockito.when(twtTerminal2.getQ()).thenReturn(twtQ2);
        Mockito.when(twtTerminal2.getBusView()).thenReturn(twtBusView2);

        RatioTapChangerStep step = Mockito.mock(RatioTapChangerStep.class);
        Mockito.when(step.getR()).thenReturn(0.0);
        Mockito.when(step.getX()).thenReturn(0.0);
        Mockito.when(step.getG()).thenReturn(0.0);
        Mockito.when(step.getB()).thenReturn(0.0);
        Mockito.when(step.getRho()).thenReturn(1.0);

        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger.getCurrentStep()).thenReturn(step);

        transformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(transformer.getId()).thenReturn(twtId);
        Mockito.when(transformer.getTerminal1()).thenReturn(twtTerminal1);
        Mockito.when(transformer.getTerminal2()).thenReturn(twtTerminal2);
        Mockito.when(transformer.getTerminal(Side.ONE)).thenReturn(twtTerminal1);
        Mockito.when(transformer.getTerminal(Side.TWO)).thenReturn(twtTerminal2);
        Mockito.when(transformer.getR()).thenReturn(0.121);
        Mockito.when(transformer.getX()).thenReturn(47.9);
        Mockito.when(transformer.getG()).thenReturn(0.0);
        Mockito.when(transformer.getB()).thenReturn(0.0);
        Mockito.when(transformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(transformer.getRatedU1()).thenReturn(380.0);
        Mockito.when(transformer.getRatedU2()).thenReturn(380.0);

        Bus shuntBus = Mockito.mock(Bus.class);
        Mockito.when(shuntBus.getV()).thenReturn(14.5965);
        Mockito.when(shuntBus.getAngle()).thenReturn(Math.toDegrees(0));
        Mockito.when(shuntBus.isInMainConnectedComponent()).thenReturn(true);

        BusView shuntBusView = Mockito.mock(BusView.class);
        Mockito.when(shuntBusView.getBus()).thenReturn(shuntBus);

        shuntTerminal = Mockito.mock(Terminal.class);
        Mockito.when(shuntTerminal.isConnected()).thenReturn(true);
        Mockito.when(shuntTerminal.getP()).thenReturn(0.0);
        Mockito.when(shuntTerminal.getQ()).thenReturn(shuntQ);
        Mockito.when(shuntTerminal.getBusView()).thenReturn(shuntBusView);

        shunt = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shunt.getId()).thenReturn(shuntId);
        Mockito.when(shunt.getTerminal()).thenReturn(shuntTerminal);
        Mockito.when(shunt.getCurrentB()).thenReturn(0.099769);

        StateManager stateManager = Mockito.mock(StateManager.class);
        Mockito.when(stateManager.getWorkingStateId()).thenReturn(StateManagerConstants.INITIAL_STATE_ID);

        network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getStateManager()).thenReturn(stateManager);
        Mockito.when(network.getLineStream()).thenAnswer(dummy -> Stream.of(line));
        Mockito.when(network.getTwoWindingsTransformerStream()).thenAnswer(dummy -> Stream.of(transformer));
        Mockito.when(network.getShuntCompensatorStream()).thenAnswer(dummy -> Stream.of(shunt));
    }

    protected void setNanValues() {
        Mockito.when(lineTerminal1.getP()).thenReturn(Double.NaN);
        Mockito.when(lineTerminal1.getQ()).thenReturn(Double.NaN);
        Mockito.when(twtTerminal1.getP()).thenReturn(Double.NaN);
        Mockito.when(twtTerminal1.getQ()).thenReturn(Double.NaN);
        Mockito.when(shuntTerminal.getQ()).thenReturn(Double.NaN);
    }

    protected void checkResultsCompletion() {
        ArgumentCaptor<Double> setterCaptor = ArgumentCaptor.forClass(Double.class);
        Mockito.verify(lineTerminal1, Mockito.times(1)).setP(setterCaptor.capture());
        assertEquals(lineP1, setterCaptor.getValue(), 0.0001);
        Mockito.verify(lineTerminal1, Mockito.times(1)).setQ(setterCaptor.capture());
        assertEquals(lineQ1, setterCaptor.getValue(), 0.0001);
        Mockito.verify(lineTerminal2, Mockito.times(0)).setP(Matchers.anyDouble());
        Mockito.verify(lineTerminal2, Mockito.times(0)).setQ(Matchers.anyDouble());
        Mockito.verify(twtTerminal1, Mockito.times(1)).setP(setterCaptor.capture());
        assertEquals(twtP1, setterCaptor.getValue(), 0.0001);
        Mockito.verify(twtTerminal1, Mockito.times(1)).setQ(setterCaptor.capture());
        assertEquals(twtQ1, setterCaptor.getValue(), 0.0001);
        Mockito.verify(twtTerminal2, Mockito.times(0)).setP(Matchers.anyDouble());
        Mockito.verify(twtTerminal2, Mockito.times(0)).setQ(Matchers.anyDouble());
        Mockito.verify(shuntTerminal, Mockito.times(1)).setQ(setterCaptor.capture());
        assertEquals(shuntQ, setterCaptor.getValue(), 0.0001);
    }
}
