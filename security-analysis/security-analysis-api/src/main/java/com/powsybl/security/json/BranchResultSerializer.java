/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.BranchResult;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class BranchResultSerializer extends StdSerializer<BranchResult> {

    public BranchResultSerializer() {
        super(BranchResult.class);
    }

    @Override
    public void serialize(BranchResult branchResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("branchId", branchResult.getBranchId());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "p1", branchResult.getP1());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "q1", branchResult.getQ1());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "i1", branchResult.getI1());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "p2", branchResult.getP2());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "q2", branchResult.getQ2());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "i2", branchResult.getI2());
        JsonUtil.writeOptionalDoubleField(jsonGenerator, "flowTransfer", branchResult.getFlowTransfer());
        JsonUtil.writeExtensions(branchResult, jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
    }
}
