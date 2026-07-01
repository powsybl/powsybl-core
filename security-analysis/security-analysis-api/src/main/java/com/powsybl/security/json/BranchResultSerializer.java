/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.BranchResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class BranchResultSerializer extends StdSerializer<BranchResult> {

    public BranchResultSerializer() {
        super(BranchResult.class);
    }

    @Override
    public void serialize(BranchResult branchResult, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("branchId", branchResult.getBranchId());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "p1", branchResult.getP1());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "q1", branchResult.getQ1());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "i1", branchResult.getI1());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "p2", branchResult.getP2());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "q2", branchResult.getQ2());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "i2", branchResult.getI2());
        JsonUtil.writeOptionalDoubleProperty(jsonGenerator, "flowTransfer", branchResult.getFlowTransfer());
        JsonUtil.writeExtensions(branchResult, jsonGenerator, serializationContext);
        jsonGenerator.writeEndObject();
    }
}
