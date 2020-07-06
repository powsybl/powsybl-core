/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Load;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public interface LoadDetailAdder extends ExtensionAdder<Load, LoadDetail> {

    @Override
    default Class<LoadDetail> getExtensionClass() {
        return LoadDetail.class;
    }

    LoadDetailAdder withFixedActivePower(float fixedActivePower);

    LoadDetailAdder withFixedReactivePower(float fixedReactivePower);

    LoadDetailAdder withVariableActivePower(float variableActivePower);

    LoadDetailAdder withVariableReactivePower(float variableReactivePower);
}
