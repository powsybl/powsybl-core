/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface VoltageLevelAdder extends IdentifiableAdder<VoltageLevelAdder> {

    VoltageLevelAdder setNominalV(double nominalV);

    /**
     * @deprecated Use {@link VoltageLimitsAdder#setLowVoltage(double)} instead.
     */
    @Deprecated
    default VoltageLevelAdder setLowVoltageLimit(double lowVoltageLimit) {
        return this;
    }

    /**
     * @deprecated Use {@link VoltageLimitsAdder#setHighVoltage(double)} instead.
     */
    @Deprecated
    default VoltageLevelAdder setHighVoltageLimit(double highVoltageLimit) {
        return this;
    }

    VoltageLevelAdder setTopologyKind(String topologyKind);

    VoltageLevelAdder setTopologyKind(TopologyKind topologyKind);

    VoltageLevel add();
}
