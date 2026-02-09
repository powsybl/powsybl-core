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
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationBuilderImpl extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationBuilder> implements VoltageRegulationBuilder {

    private Ref<NetworkImpl> network;

    @Override
    protected VoltageRegulationBuilderImpl self() {
        return this;
    }

    public VoltageRegulationBuilderImpl withNetwork(Ref<NetworkImpl> network) {
        this.network = network;
        return self();
    }

    @Override
    public VoltageRegulation build() {
        // TODO MSA checkValidation
        return new VoltageRegulationImpl(network, targetValue, targetDeadband, slope, terminal, mode, regulating);
    }
}
