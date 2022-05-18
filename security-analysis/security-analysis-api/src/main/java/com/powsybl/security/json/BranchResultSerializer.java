/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.BranchResult;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BranchResultSerializer extends StdSerializer<BranchResult>  {

    public BranchResultSerializer() {
        super(BranchResult.class);
    }

    @Override
    public void serialize(BranchResult branchResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("branchId", branchResult.getBranchId());
        jsonGenerator.writeNumberField("p1", branchResult.getP1());
        jsonGenerator.writeNumberField("q1", branchResult.getQ1());
        jsonGenerator.writeNumberField("i1", branchResult.getI1());
        jsonGenerator.writeNumberField("p2", branchResult.getP2());
        jsonGenerator.writeNumberField("q2", branchResult.getQ2());
        jsonGenerator.writeNumberField("i2", branchResult.getI2());
        if (!Double.isNaN(branchResult.getFlowTransfer())) {
            jsonGenerator.writeNumberField("flowTransfer", branchResult.getFlowTransfer());
        }
        JsonUtil.writeExtensions(branchResult, jsonGenerator, serializerProvider);
        jsonGenerator.writeEndObject();
    }
}
