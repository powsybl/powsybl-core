/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.iidm.network.*;

/**
 * Interface to modify a network after an Ampl solve.<br>
 * {@link AmplNetworkReader} does the Ampl output parsing,
 * and {@link AmplNetworkUpdater} modifies the {@link Network}.<br>
 * <p>
 * Default implementation in {@link DefaultAmplNetworkUpdater}.
 *
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 * @see AmplNetworkReader
 * @see AbstractAmplNetworkUpdater
 * @see DefaultAmplNetworkUpdater
 */
public interface AmplNetworkUpdater {

    void updateNetworkGenerators(Generator g, int busNum, boolean vregul, double targetV, double targetP,
                                 double targetQ, double p, double q);

    void updateNetworkBattery(Battery b, int busNum, double targetP, double targetQ, double p, double q);

    void updateNetworkShunt(ShuntCompensator sc, int busNum, double q, double b, int sections);

    void updateNetworkSvc(StaticVarCompensator svc, int busNum, boolean vregul, double targetV, double q);

    void updateNetworkVsc(VscConverterStation vsc, int busNum, boolean vregul, double targetV, double targetQ, double p,
                          double q);

    void updateNetworkLoad(Load l, Network network, String id, int busNum, double p, double q, double p0, double q0);

    void updateNetworkRatioTapChanger(Network network, String id, int tap);

    void updateNetworkPhaseTapChanger(Network network, String id, int tap);

    void updateNetworkBus(Bus bus, double v, double theta);

    void updateNetworkBranch(Branch br, Network network, String id, int busNum, int busNum2, double p1, double p2,
                             double q1, double q2);

    void updateNetworkHvdcLine(HvdcLine hl, String converterMode, double targetP);

    void updateNetworkLcc(LccConverterStation lcc, int busNum, double p, double q);

}
