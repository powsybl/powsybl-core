/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class BaseVoltageMappingAdderImpl extends AbstractExtensionAdder<Network, BaseVoltageMapping> implements BaseVoltageMappingAdder {

    private Set<BaseVoltageMapping.BaseVoltageSource> baseVoltages = new HashSet<>();

    protected BaseVoltageMappingAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public BaseVoltageMappingAdder addBaseVoltage(String baseVoltage, double nominalVoltage, Source source) {
        baseVoltages.add(new BaseVoltageMappingImpl.BaseVoltageSourceImpl(baseVoltage, nominalVoltage, source));
        return this;
    }

    @Override
    protected BaseVoltageMapping createExtension(Network extendable) {
        return new BaseVoltageMappingImpl(baseVoltages);
    }
}
