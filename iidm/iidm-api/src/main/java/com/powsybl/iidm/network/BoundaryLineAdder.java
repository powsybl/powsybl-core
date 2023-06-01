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
public interface BoundaryLineAdder extends InjectionAdder<BoundaryLine, BoundaryLineAdder> {

    interface GenerationAdder {

        GenerationAdder setTargetP(double targetP);

        GenerationAdder setMaxP(double maxP);

        GenerationAdder setMinP(double minP);

        GenerationAdder setTargetQ(double targetQ);

        GenerationAdder setVoltageRegulationOn(boolean voltageRegulationOn);

        GenerationAdder setTargetV(double targetV);

        BoundaryLineAdder add();
    }

    BoundaryLineAdder setP0(double p0);

    BoundaryLineAdder setQ0(double q0);

    BoundaryLineAdder setR(double r);

    BoundaryLineAdder setX(double x);

    BoundaryLineAdder setG(double g);

    BoundaryLineAdder setB(double b);

    BoundaryLineAdder setUcteXnodeCode(String ucteXnodeCode);

    default GenerationAdder newGeneration() {
        throw new UnsupportedOperationException();
    }

    @Override
    BoundaryLine add();
}
