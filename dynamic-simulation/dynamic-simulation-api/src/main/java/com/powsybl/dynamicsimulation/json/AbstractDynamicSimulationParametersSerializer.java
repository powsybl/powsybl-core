/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.dynamicsimulation.AbstractDynamicSimulationParameters;

import java.io.IOException;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractDynamicSimulationParametersSerializer<T extends AbstractDynamicSimulationParameters<T>> extends StdSerializer<T> {

    protected AbstractDynamicSimulationParametersSerializer(Class<T> ds) {
        super(ds);
    }

    protected void serializeCommon(AbstractDynamicSimulationParameters<T> parameters, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField("version", parameters.getVersion());
        jsonGenerator.writeNumberField("startTime", parameters.getStartTime());
        jsonGenerator.writeNumberField("stopTime", parameters.getStopTime());
    }
}
