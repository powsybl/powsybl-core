/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.AbstractSidedContingency;
import com.powsybl.contingency.ContingencyElement;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class ContingencyElementSerializer extends StdSerializer<ContingencyElement> {

    public ContingencyElementSerializer() {
        super(ContingencyElement.class);
    }

    @Override
    public void serialize(ContingencyElement contingencyElement, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("id", contingencyElement.getId());
        jsonGenerator.writeStringProperty("type", contingencyElement.getType().name());
        if (contingencyElement instanceof AbstractSidedContingency sidedContingency) {
            JsonUtil.writeOptionalStringProperty(jsonGenerator, "voltageLevelId", sidedContingency.getVoltageLevelId());
        }
        jsonGenerator.writeEndObject();
    }
}
