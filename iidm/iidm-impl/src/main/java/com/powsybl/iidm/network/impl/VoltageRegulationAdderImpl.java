/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.regulation.*;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationAdderImpl<T extends VoltageRegulationHolderAdder<T>> extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationAdder<T>> implements VoltageRegulationAdder<T> {
    private final T equipmentAdder;

    private final Consumer<Supplier<VoltageRegulation>> voltageRegulationCreatorConsumer;

    public VoltageRegulationAdderImpl(Class<? extends VoltageRegulationHolder<?>> holderClass,
                                      Validable validable,
                                      T equipmentAdder,
                                      Ref<NetworkImpl> network,
                                      Consumer<Supplier<VoltageRegulation>> voltageRegulationCreatorConsumer) {
        super(holderClass, validable, null, network);
        this.equipmentAdder = equipmentAdder;
        this.voltageRegulationCreatorConsumer = voltageRegulationCreatorConsumer;
    }

    @Override
    protected VoltageRegulationAdder<T> self() {
        return this;
    }

    @Override
    public T add() {
        if (voltageRegulationCreatorConsumer != null) {
            voltageRegulationCreatorConsumer.accept(this::checkAndCreateVoltageRegulation);
        }
        return equipmentAdder;
    }
}
