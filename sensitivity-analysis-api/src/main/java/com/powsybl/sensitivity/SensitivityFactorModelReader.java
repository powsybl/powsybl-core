/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityFactorModelReader implements SensitivityFactorReader {

    private final List<SensitivityFactor> factors;

    public SensitivityFactorModelReader(List<SensitivityFactor> factors) {
        this.factors = Objects.requireNonNull(factors);
    }

    @Override
    public void read(Handler handler) {
        Objects.requireNonNull(handler);
        for (SensitivityFactor factor : factors) {
            String functionId = factor.getFunctionId();
            handler.onFactor(factor.getFunctionType(), functionId, factor.getVariableType(),
                    factor.getVariableId(), factor.isVariableSet(), factor.getContingencyContext());
        }
    }
}
