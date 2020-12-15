/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Load;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LoadDetail extends Extension<Load> {

    @Override
    default String getName() {
        return "detail";
    }

    float getFixedActivePower();

    LoadDetail setFixedActivePower(float fixedActivePower);

    float getFixedReactivePower();

    LoadDetail setFixedReactivePower(float fixedReactivePower);

    float getVariableActivePower();

    LoadDetail setVariableActivePower(float variableActivePower);

    float getVariableReactivePower();

    LoadDetail setVariableReactivePower(float variableReactivePower);
}
