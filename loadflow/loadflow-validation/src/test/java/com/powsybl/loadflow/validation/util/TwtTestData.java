/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation.util;

import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class TwtTestData {

    public static double P1 = 99.218431;
    public static double Q1 = 2.7304328;
    public static double P2 = -216.19819;
    public static double Q2 = -85.368180;
    public static double P3 = 118;
    public static double Q3 = 92.612077;

    public static double COMPUTED_P1 = 99.227288294050368;
    public static double COMPUTED_Q1 = 2.747147185209846;
    public static double COMPUTED_P2 = -216.195866533486196;
    public static double COMPUTED_Q2 = -85.490493190353362;
    public static double COMPUTED_P3 = 117.988318295632794;
    public static double COMPUTED_Q3 = 92.500849015581423;

    public static double U1 = 412.989001;
    public static double ANGLE1 = -6.78071;
    public static double U2 = 224.315268;
    public static double ANGLE2 = -8.77012;
    public static double U3 = 21.987;
    public static double ANGLE3 = -6.6508;

    public static double STAR_U = 412.662007016922757;
    public static double STAR_ANGLE = -7.353686938578365;

    public static double R1 = 0.898462;
    public static double X1 = 17.204128;
    public static double G11 = 0;
    public static double B11 = 2.4375E-6;
    public static double G12 = 0;
    public static double B12 = 0;
    public static double RATED_U1 = 400;
    public static double R2 = 1.070770247933884;
    public static double X2 = 19.6664;
    public static double G21 = 0;
    public static double B21 = 0;
    public static double G22 = 0;
    public static double B22 = 0;
    public static double RATED_U2 = 220;
    public static double R3 = 4.837006802721089;
    public static double X3 = 21.76072562358277;
    public static double G31 = 0;
    public static double B31 = 0;
    public static double G32 = 0;
    public static double B32 = 0;
    public static double RATED_U3 = 21;
    public static int PHASE_ANGLE_CLOCK_2 = 0;
    public static int PHASE_ANGLE_CLOCK_3 = 0;
    public static double RATED_U0 = RATED_U1;

    public static boolean CONNECTED1 = true;
    public static boolean CONNECTED2 = true;
    public static boolean CONNECTED3 = true;
    public static boolean MAIN_COMPONENT1 = true;
    public static boolean MAIN_COMPONENT2 = true;
    public static boolean MAIN_COMPONENT3 = true;

    private Terminal leg1Terminal;
    private Terminal leg2Terminal;
    private Terminal leg3Terminal;
    private ThreeWindingsTransformer twt3w;

    public TwtTestData() {
        Bus leg1Bus = Mockito.mock(Bus.class);
        Mockito.when(leg1Bus.getV()).thenReturn(U1);
        Mockito.when(leg1Bus.getAngle()).thenReturn(ANGLE1);
        Mockito.when(leg1Bus.isInMainConnectedComponent()).thenReturn(MAIN_COMPONENT1);

        BusView leg1BusView = Mockito.mock(BusView.class);
        Mockito.when(leg1BusView.getBus()).thenReturn(leg1Bus);

        leg1Terminal = Mockito.mock(Terminal.class);
        Mockito.when(leg1Terminal.isConnected()).thenReturn(CONNECTED1);
        Mockito.when(leg1Terminal.getP()).thenReturn(P1);
        Mockito.when(leg1Terminal.getQ()).thenReturn(Q1);
        Mockito.when(leg1Terminal.getBusView()).thenReturn(leg1BusView);

        Leg leg1 = Mockito.mock(Leg.class);
        Mockito.when(leg1.getR()).thenReturn(R1);
        Mockito.when(leg1.getX()).thenReturn(X1);
        Mockito.when(leg1.getRatedU()).thenReturn(RATED_U1);
        Mockito.when(leg1.getB()).thenReturn(B11 + B12);
        Mockito.when(leg1.getG()).thenReturn(G11 + G12);
        Mockito.when(leg1.getTerminal()).thenReturn(leg1Terminal);

        Bus leg2Bus = Mockito.mock(Bus.class);
        Mockito.when(leg2Bus.getV()).thenReturn(U2);
        Mockito.when(leg2Bus.getAngle()).thenReturn(ANGLE2);
        Mockito.when(leg2Bus.isInMainConnectedComponent()).thenReturn(MAIN_COMPONENT2);

        BusView leg2BusView = Mockito.mock(BusView.class);
        Mockito.when(leg2BusView.getBus()).thenReturn(leg2Bus);

        leg2Terminal = Mockito.mock(Terminal.class);
        Mockito.when(leg2Terminal.isConnected()).thenReturn(CONNECTED2);
        Mockito.when(leg2Terminal.getP()).thenReturn(P2);
        Mockito.when(leg2Terminal.getQ()).thenReturn(Q2);
        Mockito.when(leg2Terminal.getBusView()).thenReturn(leg2BusView);

        Leg leg2 = Mockito.mock(Leg.class);
        Mockito.when(leg2.getR()).thenReturn(R2);
        Mockito.when(leg2.getX()).thenReturn(X2);
        Mockito.when(leg2.getB()).thenReturn(B21 + B22);
        Mockito.when(leg2.getG()).thenReturn(G21 + G22);
        Mockito.when(leg2.getRatedU()).thenReturn(RATED_U2);
        Mockito.when(leg2.getTerminal()).thenReturn(leg2Terminal);

        Bus leg3Bus = Mockito.mock(Bus.class);
        Mockito.when(leg3Bus.getV()).thenReturn(U3);
        Mockito.when(leg3Bus.getAngle()).thenReturn(ANGLE3);
        Mockito.when(leg3Bus.isInMainConnectedComponent()).thenReturn(MAIN_COMPONENT3);

        BusView leg3BusView = Mockito.mock(BusView.class);
        Mockito.when(leg3BusView.getBus()).thenReturn(leg3Bus);

        leg3Terminal = Mockito.mock(Terminal.class);
        Mockito.when(leg3Terminal.isConnected()).thenReturn(CONNECTED3);
        Mockito.when(leg3Terminal.getP()).thenReturn(P3);
        Mockito.when(leg3Terminal.getQ()).thenReturn(Q3);
        Mockito.when(leg3Terminal.getBusView()).thenReturn(leg3BusView);

        Leg leg3 = Mockito.mock(Leg.class);
        Mockito.when(leg3.getR()).thenReturn(R3);
        Mockito.when(leg3.getX()).thenReturn(X3);
        Mockito.when(leg3.getB()).thenReturn(B31 + B32);
        Mockito.when(leg3.getG()).thenReturn(G31 + G32);
        Mockito.when(leg3.getRatedU()).thenReturn(RATED_U3);
        Mockito.when(leg3.getTerminal()).thenReturn(leg3Terminal);

        twt3w = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(twt3w.getId()).thenReturn("twt3w");
        Mockito.when(twt3w.getLeg1()).thenReturn(leg1);
        Mockito.when(twt3w.getLeg2()).thenReturn(leg2);
        Mockito.when(twt3w.getLeg3()).thenReturn(leg3);
        Mockito.when(twt3w.getRatedU0()).thenReturn(RATED_U0);
    }

    public ThreeWindingsTransformer get3WTransformer() {
        return twt3w;
    }

    public void setNanLeg1P() {
        Mockito.when(leg1Terminal.getP()).thenReturn(Double.NaN);
    }

    public void setLeg1Disconnected() {
        Mockito.when(leg1Terminal.isConnected()).thenReturn(false);
    }

    public void setLeg2Disconnected() {
        Mockito.when(leg2Terminal.isConnected()).thenReturn(false);
    }

    public void setLeg3Disconnected() {
        Mockito.when(leg3Terminal.isConnected()).thenReturn(false);
    }
}
