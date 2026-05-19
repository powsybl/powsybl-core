/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import com.powsybl.timeseries.ast.NodeCalc;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NodeCalcJsonSerializer extends StdSerializer<NodeCalc> {

    public NodeCalcJsonSerializer() {
        super(NodeCalc.class);
    }

    @Override
    public void serialize(NodeCalc node, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        NodeCalc.writeJson(node, jsonGenerator);
    }
}
