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
public class VoltageRegulationBuilderImpl<P extends VoltageRegulationHolder> extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationBuilder, P> implements VoltageRegulationBuilder {

    public VoltageRegulationBuilderImpl(Class<? extends VoltageRegulationHolder> holderClass, P parent, Ref<NetworkImpl> network, Consumer<VoltageRegulationImpl> consumerSetVoltageRegulation) {
        super(holderClass, parent, network, consumerSetVoltageRegulation);
    }

    @Override
    protected VoltageRegulationBuilder self() {
        return this;
    }

    @Override
    public VoltageRegulation build() {
        VoltageRegulationImpl voltageRegulation = createVoltageRegulation();
        this.setVoltageRegulation.accept(voltageRegulation);
        return voltageRegulation;
    }
}
