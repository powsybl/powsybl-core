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
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.*;

import java.util.function.Function;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
class VoltageRegulationBuilderImpl extends AbstractVoltageRegulationAdderOrBuilder<VoltageRegulationBuilder> implements VoltageRegulationBuilder {

    private final Function<VoltageRegulation.AttributesWithTerminal, VoltageRegulationExt> voltageRegulationSetter;

    VoltageRegulationBuilderImpl(Class<? extends VoltageRegulationHolder<?>> holderClass,
                                        Validable validable,
                                        VoltageRegulationHolder<?> holder,
                                        Ref<NetworkImpl> network,
                                        Function<VoltageRegulation.AttributesWithTerminal, VoltageRegulationExt> voltageRegulationSetter) {
        super(holderClass, validable, holder, network);
        this.voltageRegulationSetter = voltageRegulationSetter;
    }

    @Override
    protected VoltageRegulationBuilder self() {
        return this;
    }

    @Override
    public VoltageRegulation build() {
        VoltageRegulation.AttributesWithTerminal voltageRegulationAttributes = checkAndGetVoltageRegulationAttributes();
        ValidationUtil.checkLocalTargetQandV(validable,
            classHolder,
            holder.getLocalTargetV(),
            holder.getLocalTargetQ(),
            voltageRegulationAttributes,
            network.get().getMinValidationLevel(),
            network.get().getReportNodeContext().getReportNode());
        return this.voltageRegulationSetter.apply(voltageRegulationAttributes);
    }
}
