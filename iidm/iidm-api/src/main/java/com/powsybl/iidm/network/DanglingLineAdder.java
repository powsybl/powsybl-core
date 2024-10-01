/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface DanglingLineAdder extends InjectionAdder<DanglingLine, DanglingLineAdder> {

    interface GenerationAdder {

        GenerationAdder setTargetP(double targetP);

        GenerationAdder setMaxP(double maxP);

        GenerationAdder setMinP(double minP);

        GenerationAdder setTargetQ(double targetQ);

        GenerationAdder setVoltageRegulationOn(boolean voltageRegulationOn);

        GenerationAdder setTargetV(double targetV);

        DanglingLineAdder add();
    }

    DanglingLineAdder setP0(double p0);

    DanglingLineAdder setQ0(double q0);

    DanglingLineAdder setR(double r);

    DanglingLineAdder setX(double x);

    DanglingLineAdder setG(double g);

    DanglingLineAdder setB(double b);

    DanglingLineAdder setPairingKey(String pairingKey);

    DanglingLineAdder setShuntAdmittanceHasBeenMerged(boolean shuntAdmittanceHasBeenMerged);

    default GenerationAdder newGeneration() {
        throw new UnsupportedOperationException();
    }

    @Override
    DanglingLine add();
}
