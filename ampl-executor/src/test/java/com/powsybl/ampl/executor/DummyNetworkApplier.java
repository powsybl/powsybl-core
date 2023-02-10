/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.NetworkApplier;
import com.powsybl.iidm.network.*;

/**
 * @author Nicolas Pierre <nicolas.pierre@artelys.com>
 */
public class DummyNetworkApplier implements NetworkApplier {
    @Override
    public void applyGenerators(Generator g, int busNum, boolean vregul, double targetV, double targetP, double targetQ, double p, double q) {
        // do nothing with the results
    }

    @Override
    public void applyBattery(Battery b, int busNum, double targetP, double targetQ, double p, double q) {
        // do nothing with the results
    }

    @Override
    public void applyShunt(ShuntCompensator sc, int busNum, double q, double b, int sections) {
        // do nothing with the results
    }

    @Override
    public void applySvc(StaticVarCompensator svc, int busNum, boolean vregul, double targetV, double q) {
        // do nothing with the results
    }

    @Override
    public void applyVsc(VscConverterStation vsc, int busNum, boolean vregul, double targetV, double targetQ, double p, double q) {
        // do nothing with the results
    }

    @Override
    public void applyLoad(Load l, Network network, String id, int busNum, double p, double q, double p0, double q0) {
        // do nothing with the results
    }

    @Override
    public void applyRatioTapChanger(Network network, String id, int tap) {
        // do nothing with the results
    }

    @Override
    public void applyPhaseTapChanger(Network network, String id, int tap) {
        // do nothing with the results
    }

    @Override
    public void applyBus(Bus bus, double v, double theta) {
        // do nothing with the results
    }

    @Override
    public void applyBranch(Branch br, Network network, String id, int busNum, int busNum2, double p1, double p2, double q1, double q2) {
        // do nothing with the results
    }

    @Override
    public void applyHvdcLine(HvdcLine hl, String converterMode, double targetP) {
        // do nothing with the results
    }

    @Override
    public void applyLcc(LccConverterStation lcc, int busNum, double p, double q) {
        // do nothing with the results
    }
}
