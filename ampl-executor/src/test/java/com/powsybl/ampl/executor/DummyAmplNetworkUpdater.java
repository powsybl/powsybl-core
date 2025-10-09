/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.AmplNetworkUpdater;
import com.powsybl.iidm.network.*;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
public class DummyAmplNetworkUpdater implements AmplNetworkUpdater {
    @Override
    public void updateNetworkGenerators(Generator g, int busNum, boolean vregul, double targetV, double targetP,
                                        double targetQ, double p, double q) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkBattery(Battery b, int busNum, double targetP, double targetQ, double p, double q) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkShunt(ShuntCompensator sc, int busNum, double q, double b, int sections) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkSvc(StaticVarCompensator svc, int busNum, boolean vregul, double targetV, double q) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkVsc(VscConverterStation vsc, int busNum, boolean vregul, double targetV, double targetQ,
                                 double p, double q) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkLoad(Load l, Network network, String id, int busNum, double p, double q, double p0,
                                  double q0) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkRatioTapChanger(Network network, String id, int tap) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkPhaseTapChanger(Network network, String id, int tap) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkBus(Bus bus, double v, double theta) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkBranch(Branch br, Network network, String id, int busNum, int busNum2, double p1,
                                    double p2, double q1, double q2) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkHvdcLine(HvdcLine hl, String converterMode, double targetP) {
        // do nothing with the results
    }

    @Override
    public void updateNetworkLcc(LccConverterStation lcc, int busNum, double p, double q) {
        // do nothing with the results
    }

}
