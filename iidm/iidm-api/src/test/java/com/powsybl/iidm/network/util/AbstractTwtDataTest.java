/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import org.junit.Before;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg1;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg2or3;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public abstract class AbstractTwtDataTest {

    ThreeWindingsTransformer twt;

    @Before
    public void setUp() {
        Bus leg1Bus = Mockito.mock(Bus.class);
        Mockito.when(leg1Bus.getV()).thenReturn(412.989001);
        Mockito.when(leg1Bus.getAngle()).thenReturn(-6.78071);
        Mockito.when(leg1Bus.isInMainConnectedComponent()).thenReturn(true);
        BusView leg1BusView = Mockito.mock(BusView.class);
        Mockito.when(leg1BusView.getBus()).thenReturn(leg1Bus);
        Terminal leg1Terminal = Mockito.mock(Terminal.class);
        Mockito.when(leg1Terminal.isConnected()).thenReturn(true);
        Mockito.when(leg1Terminal.getBusView()).thenReturn(leg1BusView);
        Leg1 leg1 = Mockito.mock(Leg1.class);
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
        BusView leg2BusView = Mockito.mock(BusView.class);
        Mockito.when(leg2BusView.getBus()).thenReturn(leg2Bus);
        Terminal leg2Terminal = Mockito.mock(Terminal.class);
        Mockito.when(leg2Terminal.isConnected()).thenReturn(true);
        Mockito.when(leg2Terminal.getBusView()).thenReturn(leg2BusView);
        Leg2or3 leg2 = Mockito.mock(Leg2or3.class);
        Mockito.when(leg2.getR()).thenReturn(1.070770247933884);
        Mockito.when(leg2.getX()).thenReturn(19.6664);
        Mockito.when(leg2.getRatedU()).thenReturn(220.0);
        Mockito.when(leg2.getTerminal()).thenReturn(leg2Terminal);

        Bus leg3Bus = Mockito.mock(Bus.class);
        Mockito.when(leg3Bus.getV()).thenReturn(21.987);
        Mockito.when(leg3Bus.getAngle()).thenReturn(-6.6508);
        Mockito.when(leg3Bus.isInMainConnectedComponent()).thenReturn(true);
        BusView leg3BusView = Mockito.mock(BusView.class);
        Mockito.when(leg3BusView.getBus()).thenReturn(leg3Bus);
        Terminal leg3Terminal = Mockito.mock(Terminal.class);
        Mockito.when(leg3Terminal.isConnected()).thenReturn(true);
        Mockito.when(leg3Terminal.getBusView()).thenReturn(leg3BusView);
        Leg2or3 leg3 = Mockito.mock(Leg2or3.class);
        Mockito.when(leg3.getR()).thenReturn(4.837006802721089);
        Mockito.when(leg3.getX()).thenReturn(21.76072562358277);
        Mockito.when(leg3.getRatedU()).thenReturn(21.0);
        Mockito.when(leg3.getTerminal()).thenReturn(leg3Terminal);

        twt = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(twt.getId()).thenReturn("twt");
        Mockito.when(twt.getLeg1()).thenReturn(leg1);
        Mockito.when(twt.getLeg2()).thenReturn(leg2);
        Mockito.when(twt.getLeg3()).thenReturn(leg3);
    }

}
