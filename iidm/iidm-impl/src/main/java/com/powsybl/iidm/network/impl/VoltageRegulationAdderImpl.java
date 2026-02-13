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
public class VoltageRegulationAdderImpl<T> extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationAdder<T>, T> implements VoltageRegulationAdder<T> {

    public VoltageRegulationAdderImpl(Class<? extends VoltageRegulationHolder> msaClass, T parent, Ref<NetworkImpl> network, Consumer<VoltageRegulationImpl> setVoltageRegulation) {
        super(msaClass, parent, network, setVoltageRegulation);
    }

    @Override
    protected VoltageRegulationAdder<T> self() {
        return this;
    }

    @Override
    public T add() {
        this.setVoltageRegulation.accept(createVoltageRegulation());
        return parent;
    }

}
