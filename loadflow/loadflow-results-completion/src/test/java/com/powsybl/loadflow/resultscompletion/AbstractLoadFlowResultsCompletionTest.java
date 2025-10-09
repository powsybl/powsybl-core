/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion;

import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Terminal.BusView;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
abstract class AbstractLoadFlowResultsCompletionTest {

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

    protected String twt3wId = "twt3w";
    protected double leg1P = 99.218431;
    protected double leg1Q = 2.747147;
    protected double leg2P = -216.19819;
    protected double leg2Q = -85.368180;
    protected double leg3P = 118;
    protected double leg3Q = 92.612077;
    protected Terminal leg1Terminal;
    protected Terminal leg2Terminal;
    protected Terminal leg3Terminal;
    protected ThreeWindingsTransformer twt3w;

    protected Network network;

    @BeforeEach
    void setUp() {
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
        Mockito.when(line.getTerminal(TwoSides.ONE)).thenReturn(lineTerminal1);
        Mockito.when(line.getTerminal(TwoSides.TWO)).thenReturn(lineTerminal2);
        Mockito.when(line.getR()).thenReturn(0.409999);
        Mockito.when(line.getX()).thenReturn(2.47000);
        Mockito.when(line.getG1()).thenReturn(0.0);
        Mockito.when(line.getG2()).thenReturn(0.0);
        Mockito.when(line.getB1()).thenReturn(7.44000e-06);
        Mockito.when(line.getB2()).thenReturn(7.44000e-06);

        Mockito.when(lineBus1.getLineStream()).thenAnswer(dummy -> Stream.of(line));
        Mockito.when(lineBus2.getLineStream()).thenAnswer(dummy -> Stream.of(line));

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
        Mockito.when(twtBus1.getLineStream()).thenAnswer(dummy -> Stream.empty());

        BusView twtBusView2 = Mockito.mock(BusView.class);
        Mockito.when(twtBusView2.getBus()).thenReturn(twtBus2);
        Mockito.when(twtBus2.getLineStream()).thenAnswer(dummy -> Stream.empty());

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
        Mockito.when(transformer.getTerminal(TwoSides.ONE)).thenReturn(twtTerminal1);
        Mockito.when(transformer.getTerminal(TwoSides.TWO)).thenReturn(twtTerminal2);
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
        Mockito.when(shuntBus.getLineStream()).thenAnswer(dummy -> Stream.empty());

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
        Mockito.when(shunt.getB()).thenReturn(0.099769);

        Bus leg1Bus = Mockito.mock(Bus.class);
        Mockito.when(leg1Bus.getV()).thenReturn(412.989001);
        Mockito.when(leg1Bus.getAngle()).thenReturn(-6.78071);
        Mockito.when(leg1Bus.isInMainConnectedComponent()).thenReturn(true);
        Mockito.when(leg1Bus.getLineStream()).thenAnswer(dummy -> Stream.empty());

        BusView leg1BusView = Mockito.mock(BusView.class);
        Mockito.when(leg1BusView.getBus()).thenReturn(leg1Bus);

        leg1Terminal = Mockito.mock(Terminal.class);
        Mockito.when(leg1Terminal.isConnected()).thenReturn(true);
        Mockito.when(leg1Terminal.getP()).thenReturn(leg1P);
        Mockito.when(leg1Terminal.getQ()).thenReturn(leg1Q);
        Mockito.when(leg1Terminal.getBusView()).thenReturn(leg1BusView);

        Leg leg1 = Mockito.mock(Leg.class);
        Mockito.when(leg1.getR()).thenReturn(0.898462);
        Mockito.when(leg1.getX()).thenReturn(17.204128);
        Mockito.when(leg1.getRatedU()).thenReturn(400.0);
        Mockito.when(leg1.getB()).thenReturn(2.4375E-6);
        Mockito.when(leg1.getG()).thenReturn(0d);
        Mockito.when(leg1.getTerminal()).thenReturn(leg1Terminal);

        Bus leg2Bus = Mockito.mock(Bus.class);
        Mockito.when(leg2Bus.getV()).thenReturn(224.315268);
        Mockito.when(leg2Bus.getAngle()).thenReturn(-8.77012);
        Mockito.when(leg2Bus.isInMainConnectedComponent()).thenReturn(true);
        Mockito.when(leg2Bus.getLineStream()).thenAnswer(dummy -> Stream.empty());

        BusView leg2BusView = Mockito.mock(BusView.class);
        Mockito.when(leg2BusView.getBus()).thenReturn(leg2Bus);

        leg2Terminal = Mockito.mock(Terminal.class);
        Mockito.when(leg2Terminal.isConnected()).thenReturn(true);
        Mockito.when(leg2Terminal.getP()).thenReturn(leg2P);
        Mockito.when(leg2Terminal.getQ()).thenReturn(leg2Q);
        Mockito.when(leg2Terminal.getBusView()).thenReturn(leg2BusView);

        Leg leg2 = Mockito.mock(Leg.class);
        Mockito.when(leg2.getR()).thenReturn(1.070770247933884);
        Mockito.when(leg2.getX()).thenReturn(19.6664);
        Mockito.when(leg2.getRatedU()).thenReturn(220.0);
        Mockito.when(leg2.getTerminal()).thenReturn(leg2Terminal);

        Bus leg3Bus = Mockito.mock(Bus.class);
        Mockito.when(leg3Bus.getV()).thenReturn(21.987);
        Mockito.when(leg3Bus.getAngle()).thenReturn(-6.6508);
        Mockito.when(leg3Bus.isInMainConnectedComponent()).thenReturn(true);
        Mockito.when(leg3Bus.getLineStream()).thenAnswer(dummy -> Stream.empty());

        BusView leg3BusView = Mockito.mock(BusView.class);
        Mockito.when(leg3BusView.getBus()).thenReturn(leg3Bus);

        leg3Terminal = Mockito.mock(Terminal.class);
        Mockito.when(leg3Terminal.isConnected()).thenReturn(true);
        Mockito.when(leg3Terminal.getP()).thenReturn(leg3P);
        Mockito.when(leg3Terminal.getQ()).thenReturn(leg3Q);
        Mockito.when(leg3Terminal.getBusView()).thenReturn(leg3BusView);

        Leg leg3 = Mockito.mock(Leg.class);
        Mockito.when(leg3.getR()).thenReturn(4.837006802721089);
        Mockito.when(leg3.getX()).thenReturn(21.76072562358277);
        Mockito.when(leg3.getRatedU()).thenReturn(21.0);
        Mockito.when(leg3.getTerminal()).thenReturn(leg3Terminal);

        twt3w = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(twt3w.getId()).thenReturn(twt3wId);
        Mockito.when(twt3w.getLeg1()).thenReturn(leg1);
        Mockito.when(twt3w.getLeg2()).thenReturn(leg2);
        Mockito.when(twt3w.getLeg3()).thenReturn(leg3);
        Mockito.when(twt3w.getRatedU0()).thenReturn(400.0);

        Network.BusView busView = Mockito.mock(Network.BusView.class);
        Mockito.when(busView.getBusStream()).thenAnswer(dummy -> Stream.of(lineBus1, lineBus2,
                twtBus1, twtBus2, shuntBus, leg1Bus, leg2Bus, leg3Bus));

        VariantManager variantManager = Mockito.mock(VariantManager.class);
        Mockito.when(variantManager.getWorkingVariantId()).thenReturn(VariantManagerConstants.INITIAL_VARIANT_ID);

        network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getVariantManager()).thenReturn(variantManager);
        Mockito.when(network.getBusView()).thenReturn(busView);
        Mockito.when(network.getLineStream()).thenAnswer(dummy -> Stream.of(line));
        Mockito.when(network.getTwoWindingsTransformerStream())
                .thenAnswer(dummy -> Stream.of(transformer));
        Mockito.when(network.getShuntCompensatorStream()).thenAnswer(dummy -> Stream.of(shunt));
        Mockito.when(network.getThreeWindingsTransformerStream())
                .thenAnswer(dummy -> Stream.of(twt3w));
    }

    protected void setNanValues() {
        Mockito.when(lineTerminal1.getP()).thenReturn(Double.NaN);
        Mockito.when(lineTerminal1.getQ()).thenReturn(Double.NaN);

        Mockito.when(twtTerminal1.getP()).thenReturn(Double.NaN);
        Mockito.when(twtTerminal1.getQ()).thenReturn(Double.NaN);

        Mockito.when(shuntTerminal.getQ()).thenReturn(Double.NaN);

        Mockito.when(leg1Terminal.getP()).thenReturn(Double.NaN);
        Mockito.when(leg1Terminal.getQ()).thenReturn(Double.NaN);
        Mockito.when(leg3Terminal.getP()).thenReturn(Double.NaN);
        Mockito.when(leg3Terminal.getQ()).thenReturn(Double.NaN);
    }

    protected void checkResultsCompletion() {
        ArgumentCaptor<Double> setterCaptor = ArgumentCaptor.forClass(Double.class);

        Mockito.verify(lineTerminal1, Mockito.times(1)).setP(setterCaptor.capture());
        assertEquals(lineP1, setterCaptor.getValue(), 0.0001);
        Mockito.verify(lineTerminal1, Mockito.times(1)).setQ(setterCaptor.capture());
        assertEquals(lineQ1, setterCaptor.getValue(), 0.0001);
        Mockito.verify(lineTerminal2, Mockito.times(0)).setP(Mockito.anyDouble());
        Mockito.verify(lineTerminal2, Mockito.times(0)).setQ(Mockito.anyDouble());

        Mockito.verify(twtTerminal1, Mockito.times(1)).setP(setterCaptor.capture());
        assertEquals(twtP1, setterCaptor.getValue(), 0.0001);
        Mockito.verify(twtTerminal1, Mockito.times(1)).setQ(setterCaptor.capture());
        assertEquals(twtQ1, setterCaptor.getValue(), 0.0001);
        Mockito.verify(twtTerminal2, Mockito.times(0)).setP(Mockito.anyDouble());
        Mockito.verify(twtTerminal2, Mockito.times(0)).setQ(Mockito.anyDouble());

        Mockito.verify(shuntTerminal, Mockito.times(1)).setQ(setterCaptor.capture());
        assertEquals(shuntQ, setterCaptor.getValue(), 0.0001);

        Mockito.verify(leg1Terminal, Mockito.times(1)).setP(setterCaptor.capture());
        assertEquals(leg1P, setterCaptor.getValue(), 0.3);
        Mockito.verify(leg1Terminal, Mockito.times(1)).setQ(setterCaptor.capture());
        assertEquals(leg1Q, setterCaptor.getValue(), 0.3);
        Mockito.verify(leg2Terminal, Mockito.times(0)).setP(Mockito.anyDouble());
        Mockito.verify(leg2Terminal, Mockito.times(0)).setQ(Mockito.anyDouble());
        Mockito.verify(leg3Terminal, Mockito.times(1)).setP(setterCaptor.capture());
        assertEquals(leg3P, setterCaptor.getValue(), 0.3);
        Mockito.verify(leg3Terminal, Mockito.times(1)).setQ(setterCaptor.capture());
        assertEquals(leg3Q, setterCaptor.getValue(), 0.3);
    }

}
