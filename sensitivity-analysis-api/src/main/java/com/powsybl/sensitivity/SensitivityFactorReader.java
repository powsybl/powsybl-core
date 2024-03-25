/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.contingency.ContingencyContext;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface SensitivityFactorReader {

    interface Handler {

        void onFactor(SensitivityFunctionType functionType, String functionId, SensitivityVariableType variableType, String variableId, boolean variableSet, ContingencyContext contingencyContext);
    }

    void read(Handler handler);
}
