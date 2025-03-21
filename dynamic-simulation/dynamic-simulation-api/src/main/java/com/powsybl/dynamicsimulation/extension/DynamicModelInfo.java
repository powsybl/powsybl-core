/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.extension;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public interface DynamicModelInfo<I extends Identifiable<I>> extends Extension<I> {

    String NAME = "dynamicModel";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * The dynamic model name used in the simulation
     */
    String getModelName();

    void setModelName(String modelName);

}
