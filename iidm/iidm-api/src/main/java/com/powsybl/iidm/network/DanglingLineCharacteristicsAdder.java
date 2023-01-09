/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface DanglingLineCharacteristicsAdder<A extends DanglingLineCharacteristicsAdder<A>> extends InjectionAdder<A> {

    interface GenerationAdder<A extends DanglingLineCharacteristicsAdder<A>> {

        GenerationAdder<A> setTargetP(double targetP);

        GenerationAdder<A> setMaxP(double maxP);

        GenerationAdder<A> setMinP(double minP);

        GenerationAdder<A> setTargetQ(double targetQ);

        GenerationAdder<A> setVoltageRegulationOn(boolean voltageRegulationOn);

        GenerationAdder<A> setTargetV(double targetV);

        A add();
    }

    A setP0(double p0);

    A setQ0(double q0);

    A setR(double r);

    A setX(double x);

    A setG(double g);

    A setB(double b);

    A setUcteXnodeCode(String ucteXnodeCode);

    default GenerationAdder<A> newGeneration() {
        throw new UnsupportedOperationException();
    }
}
