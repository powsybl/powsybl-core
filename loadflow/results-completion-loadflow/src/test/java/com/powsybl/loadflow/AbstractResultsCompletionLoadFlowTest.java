/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import org.junit.Before;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.iidm.network.TwoTerminalsConnectable.Side;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractResultsCompletionLoadFlowTest {

    protected String lineId = "line";
    protected float lineP1 = -42.0512f;
    protected float lineQ1 = -19.5162f;
    protected float lineP2 = 42.0685f;
    protected float lineQ2 = 18.8653f;
    protected Terminal lineTerminal1;
    protected Terminal lineTerminal2;
    protected Line line;

    protected String twtId = "twt";
    protected float twtP1 = 436.5483f;
    protected float twtQ1 = 43.472f;
    protected float twtP2 = -436.4086f;
    protected float twtQ2 = 11.8373f;
    protected Terminal twtTerminal1;
    protected Terminal twtTerminal2;
    protected TwoWindingsTransformer transformer;

    @Before
    public void setUp() {
        Bus lineBus1 = Mockito.mock(Bus.class);
        Mockito.when(lineBus1.getV()).thenReturn(225.105f);
        Mockito.when(lineBus1.getAngle()).thenReturn((float) Math.toDegrees(0.0765428f));
        Mockito.when(lineBus1.isInMainConnectedComponent()).thenReturn(true);

        Bus lineBus2 = Mockito.mock(Bus.class);
        Mockito.when(lineBus2.getV()).thenReturn(225.392f);
        Mockito.when(lineBus2.getAngle()).thenReturn((float) Math.toDegrees(0.0784353f));
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
        Mockito.when(line.getR()).thenReturn(0.409999f);
        Mockito.when(line.getX()).thenReturn(2.47000f);
        Mockito.when(line.getG1()).thenReturn(0f);
        Mockito.when(line.getG2()).thenReturn(0f);
        Mockito.when(line.getB1()).thenReturn(7.44000e-06f);
        Mockito.when(line.getB2()).thenReturn(7.44000e-06f);


        Bus twtBus1 = Mockito.mock(Bus.class);
        Mockito.when(twtBus1.getV()).thenReturn(408.266f);
        Mockito.when(twtBus1.getAngle()).thenReturn((float) Math.toDegrees(-0.1662f));
        Mockito.when(twtBus1.isInMainConnectedComponent()).thenReturn(true);

        Bus twtBus2 = Mockito.mock(Bus.class);
        Mockito.when(twtBus2.getV()).thenReturn(406.276f);
        Mockito.when(twtBus2.getAngle()).thenReturn((float) Math.toDegrees(-0.292572f));
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
        Mockito.when(step.getR()).thenReturn(0f);
        Mockito.when(step.getX()).thenReturn(0f);
        Mockito.when(step.getG()).thenReturn(0f);
        Mockito.when(step.getB()).thenReturn(0f);
        Mockito.when(step.getRho()).thenReturn(1f);

        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger.getCurrentStep()).thenReturn(step);

        transformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(transformer.getId()).thenReturn(twtId);
        Mockito.when(transformer.getTerminal1()).thenReturn(twtTerminal1);
        Mockito.when(transformer.getTerminal2()).thenReturn(twtTerminal2);
        Mockito.when(transformer.getTerminal(Side.ONE)).thenReturn(twtTerminal1);
        Mockito.when(transformer.getTerminal(Side.TWO)).thenReturn(twtTerminal2);
        Mockito.when(transformer.getR()).thenReturn(0.121f);
        Mockito.when(transformer.getX()).thenReturn(47.9f);
        Mockito.when(transformer.getG()).thenReturn(0f);
        Mockito.when(transformer.getB()).thenReturn(0f);
        Mockito.when(transformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(transformer.getRatedU1()).thenReturn(380f);
        Mockito.when(transformer.getRatedU2()).thenReturn(380f);
    }

}
