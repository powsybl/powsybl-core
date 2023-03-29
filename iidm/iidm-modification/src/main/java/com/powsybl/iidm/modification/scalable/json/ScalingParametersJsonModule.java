/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.iidm.modification.scalable.ScalingParameters;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class ScalingParametersJsonModule extends SimpleModule {

    public ScalingParametersJsonModule() {
        addSerializer(ScalingParameters.class, new ScalingParametersSerializer());
        addDeserializer(ScalingParameters.class, new ScalingParametersDeserializer());
    }
}
