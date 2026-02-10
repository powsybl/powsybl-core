/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.regulation.*;

import java.util.function.Consumer;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationBuilderImpl extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationBuilder> implements VoltageRegulationBuilder {

    private final Object parent;
    private final Consumer<VoltageRegulationImpl> setVoltageRegulation;

    public VoltageRegulationBuilderImpl(Object parent, Ref<NetworkImpl> network, Consumer<VoltageRegulationImpl> setVoltageRegulation) {
        this.parent = parent;
        this.network = network;
        this.setVoltageRegulation = setVoltageRegulation;
    }

    @Override
    protected VoltageRegulationBuilder self() {
        return this;
    }

    @Override
    public VoltageRegulation build() {
        VoltageRegulationImpl voltageRegulation = createVoltageRegulation(parent); // TODO MSA
        this.setVoltageRegulation.accept(voltageRegulation);
        return voltageRegulation;
    }
}
