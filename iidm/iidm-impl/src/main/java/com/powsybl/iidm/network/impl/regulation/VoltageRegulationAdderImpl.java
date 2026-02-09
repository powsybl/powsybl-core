/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.regulation;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.impl.NetworkImpl;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationAdderImpl<P extends VoltageRegulationHolder<P>> extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationAdder<P>> implements VoltageRegulationAdder<P> {

    private final P parent;
    private final Ref<NetworkImpl> network;

    public VoltageRegulationAdderImpl(P parent, Ref<NetworkImpl> network) {
        this.parent = parent;
        this.network = network;
    }

    @Override
    public P add() {
        // TODO MSA checkValidation
        VoltageRegulation voltageRegulation = new VoltageRegulationImpl(network, targetValue, targetDeadband, slope, terminal, mode, regulating);
        parent.setVoltageRegulation(voltageRegulation);
        return parent;
    }

    @Override
    protected VoltageRegulationAdder<P> self() {
        return this;
    }
}
