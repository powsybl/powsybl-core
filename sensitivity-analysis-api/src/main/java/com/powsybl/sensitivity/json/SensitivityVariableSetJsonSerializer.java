/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.sensitivity.SensitivityVariableSet;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityVariableSetJsonSerializer extends StdSerializer<SensitivityVariableSet> {

    public SensitivityVariableSetJsonSerializer() {
        super(SensitivityVariableSet.class);
    }

    @Override
    public void serialize(SensitivityVariableSet variableSet, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        SensitivityVariableSet.writeJson(jsonGenerator, variableSet);
    }
}
