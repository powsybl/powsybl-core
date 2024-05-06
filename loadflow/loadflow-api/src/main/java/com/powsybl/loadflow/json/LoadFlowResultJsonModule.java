/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.loadflow.LoadFlowResult;

/**
 * @author Etienne Homer {@literal <etienne.homer at rte-france.com>}
 */
public class LoadFlowResultJsonModule extends SimpleModule {

    public LoadFlowResultJsonModule() {
        addDeserializer(LoadFlowResult.class, new LoadFlowResultDeserializer());
        addSerializer(LoadFlowResult.class, new LoadFlowResultSerializer());
    }
}
