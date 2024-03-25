/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.IdBasedBusRef;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyLevel;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityFactorModelReader implements SensitivityFactorReader {

    private final List<SensitivityFactor> factors;
    private final Network network;

    public SensitivityFactorModelReader(List<SensitivityFactor> factors, Network network) {
        this.factors = Objects.requireNonNull(factors);
        this.network = Objects.requireNonNull(network);
    }

    @Override
    public void read(Handler handler) {
        Objects.requireNonNull(handler);
        for (SensitivityFactor factor : factors) {
            String functionId = factor.getFunctionId();
            if (factor.getFunctionType() == SensitivityFunctionType.BUS_VOLTAGE) {
                Bus bus = new IdBasedBusRef(factor.getFunctionId()).resolve(network, TopologyLevel.BUS_BRANCH)
                    .orElseThrow(() -> new PowsyblException("The bus ref for '" + factor.getFunctionId() + "' cannot be resolved."));
                functionId = bus.getId();
            }
            handler.onFactor(factor.getFunctionType(), functionId, factor.getVariableType(),
                    factor.getVariableId(), factor.isVariableSet(), factor.getContingencyContext());
        }
    }
}
