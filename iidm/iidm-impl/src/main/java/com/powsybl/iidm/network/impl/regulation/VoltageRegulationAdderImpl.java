/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.regulation;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.impl.NetworkImpl;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationAdderImpl<T extends VoltageRegulationAdder<T>> implements VoltageRegulationBuilder<T> {

    private final VoltageRegulationImpl.Builder voltageRegulationBuilder;
    private final T parent;
    private final Ref<NetworkImpl> network;

    public VoltageRegulationAdderImpl(T parent, Ref<NetworkImpl> network) {
        this.parent = parent;
        this.network = network;
        this.voltageRegulationBuilder = VoltageRegulationImpl.builder();
    }

    @Override
    public VoltageRegulationBuilder<T> setTargetValue(Double targetValue) {
        voltageRegulationBuilder.setTargetValue(targetValue);
        return this;
    }

    @Override
    public VoltageRegulationBuilder<T> setTargetDeadband(Double targetDeadband) {
        return this;
    }

    @Override
    public VoltageRegulationBuilder<T> setSlope(Double slope) {
        voltageRegulationBuilder.setSlope(slope);
        return this;
    }

    @Override
    public VoltageRegulationBuilder<T> setTerminal(Terminal terminal) {
        voltageRegulationBuilder.setTerminal(terminal);
        return this;
    }

    @Override
    public VoltageRegulationBuilder<T> setMode(RegulationMode mode) {
        voltageRegulationBuilder.setMode(mode);
        return this;
    }

    @Override
    public VoltageRegulationBuilder<T> setRegulating(boolean regulating) {
        voltageRegulationBuilder.setRegulating(regulating);
        return this;
    }

    @Override
    public T addVoltageRegulation() {
        // TODO MSA Add check
        VoltageRegulationImpl voltageRegulation = voltageRegulationBuilder
            .setNetwork(network)
            .build();
        parent.setVoltageRegulation(voltageRegulation);
        return parent;
    }
}
