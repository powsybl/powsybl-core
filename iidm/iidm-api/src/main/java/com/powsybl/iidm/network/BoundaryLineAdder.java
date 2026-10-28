/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.regulation.VoltageRegulationHolderAdder;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface BoundaryLineAdder extends InjectionAdder<BoundaryLine, BoundaryLineAdder> {

    interface GenerationAdder extends VoltageRegulationHolderAdder<GenerationAdder> {

        GenerationAdder setTargetP(double targetP);

        GenerationAdder setMaxP(double maxP);

        GenerationAdder setMinP(double minP);

        /**
         * @deprecated Use the voltage regulation holder adder (newVoltageRegulation) and its local target values instead.
         */
        @Deprecated(since = "7.5.0", forRemoval = true)
        GenerationAdder setTargetQ(double targetQ);

        /**
         * @deprecated Use the voltage regulation holder adder (newVoltageRegulation) and its local target values instead.
         */
        @Deprecated(since = "7.5.0", forRemoval = true)
        GenerationAdder setVoltageRegulationOn(boolean voltageRegulationOn);

        /**
         * @deprecated Use the voltage regulation holder adder (newVoltageRegulation) and its local target values instead.
         */
        @Deprecated(since = "7.5.0", forRemoval = true)
        GenerationAdder setTargetV(double targetV);

        BoundaryLineAdder add();
    }

    BoundaryLineAdder setP0(double p0);

    BoundaryLineAdder setQ0(double q0);

    BoundaryLineAdder setR(double r);

    BoundaryLineAdder setX(double x);

    BoundaryLineAdder setG(double g);

    BoundaryLineAdder setB(double b);

    BoundaryLineAdder setPairingKey(String pairingKey);

    default GenerationAdder newGeneration() {
        throw new UnsupportedOperationException();
    }

    @Override
    BoundaryLine add();
}
