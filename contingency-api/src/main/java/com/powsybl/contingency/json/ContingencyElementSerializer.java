/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.BranchContingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.HvdcLineContingency;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class ContingencyElementSerializer extends StdSerializer<ContingencyElement> {

    public ContingencyElementSerializer() {
        super(ContingencyElement.class);
    }

    @Override
    public void serialize(ContingencyElement contingencyElement, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", contingencyElement.getId());
        jsonGenerator.writeStringField("type", contingencyElement.getType().name());
        if (contingencyElement instanceof BranchContingency) {
            BranchContingency branchContingencyElement = (BranchContingency) contingencyElement;
            JsonUtil.writeOptionalStringField(jsonGenerator, "voltageLevelId", branchContingencyElement.getVoltageLevelId());
        } else if (contingencyElement instanceof HvdcLineContingency) {
            HvdcLineContingency hvdcLineContingencyElement = (HvdcLineContingency) contingencyElement;
            JsonUtil.writeOptionalStringField(jsonGenerator, "voltageLevelId", hvdcLineContingencyElement.getVoltageLevelId());
        }
        jsonGenerator.writeEndObject();
    }
}
