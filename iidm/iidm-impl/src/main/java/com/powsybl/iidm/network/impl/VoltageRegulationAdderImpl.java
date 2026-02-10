/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.*;

import java.util.function.Consumer;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationAdderImpl<T extends VoltageRegulationHolder> extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationAdder<T>> implements VoltageRegulationAdder<T> {

    private final T parent;
    private final Consumer<VoltageRegulationImpl> setVoltageRegulation;
    protected double targetValue = Double.NaN;
    protected double targetDeadband = Double.NaN;
    protected double slope = Double.NaN;
    protected Terminal terminal = null;
    protected RegulationMode mode = null;
    protected boolean regulating = false;

    public VoltageRegulationAdderImpl(T parent, Ref<NetworkImpl> network, Consumer<VoltageRegulationImpl> setVoltageRegulation) {
        this.parent = parent;
        this.network = network;
        this.setVoltageRegulation = setVoltageRegulation;
    }

    @Override
    protected VoltageRegulationAdder<T> self() {
        return this;
    }

    @Override
    public T add() {
        this.setVoltageRegulation.accept(createVoltageRegulation(this.parent));
        return parent;
    }

}
