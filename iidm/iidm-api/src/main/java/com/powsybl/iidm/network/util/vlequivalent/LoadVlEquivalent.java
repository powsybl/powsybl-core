/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.vlequivalent;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public class LoadVlEquivalent extends AbstractInjectionVlEquivalent {
    private final LoadType type;

    /**
     * Calculate the characteristics of an equivalent load for a voltage level containing only a load and linked to another voltage level with a two-winding transformer.
     * Only use this if those conditions are met, otherwise the result might be unpredictable. See {@link Networks#getSingleConnectableReducibleVoltageLevelStream(Network)}
     * @param voltageLevel the voltage level containing a single battery and a side of a two-winding transformer
     */
    public LoadVlEquivalent(VoltageLevel voltageLevel) {
        this(voltageLevel.getLoads().iterator().next(), voltageLevel.getTwoWindingsTransformers().iterator().next());
    }

    public LoadVlEquivalent(Load load, TwoWindingsTransformer transformer) {
        super(load.getId(), load.getOptionalName().orElse(null), load.getP0(), load.getQ0(), transformer);
        this.type = load.getLoadType();
    }

    public LoadType getType() {
        return type;
    }

    public double getP0() {
        return activePower;
    }

    public double getQ0() {
        return reactivePower;
    }
}
