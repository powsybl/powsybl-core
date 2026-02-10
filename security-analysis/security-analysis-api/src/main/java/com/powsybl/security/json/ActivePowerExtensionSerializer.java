/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.extensions.ActivePowerExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(ExtensionJsonSerializer.class)
public class ActivePowerExtensionSerializer implements ExtensionJsonSerializer<LimitViolation, ActivePowerExtension> {

    @Override
    public String getExtensionName() {
        return "ActivePower";
    }

    @Override
    public String getCategoryName() {
        return "security-analysis";
    }

    @Override
    public Class<? super ActivePowerExtension> getExtensionClass() {
        return ActivePowerExtension.class;
    }

    @Override
    public void serialize(ActivePowerExtension extension, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        if (!Double.isNaN(extension.getPostContingencyValue())) {
            jsonGenerator.writeNumberProperty("preContingencyValue", extension.getPreContingencyValue());
            jsonGenerator.writeNumberProperty("postContingencyValue", extension.getPostContingencyValue());
        } else {
            jsonGenerator.writeNumberProperty("value", extension.getPreContingencyValue());
        }
        jsonGenerator.writeEndObject();
    }

    @Override
    public ActivePowerExtension deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        double value = Double.NaN;
        double preContingencyValue = Double.NaN;
        double postContingencyValue = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentName().equals("value")) {
                parser.nextToken();
                value = parser.readValueAs(Double.class);
            } else if (parser.currentName().equals("preContingencyValue")) {
                parser.nextToken();
                preContingencyValue = parser.readValueAs(Double.class);
            } else if (parser.currentName().equals("postContingencyValue")) {
                parser.nextToken();
                postContingencyValue = parser.readValueAs(Double.class);
            } else {
                throw new PowsyblException("Unexpected field: " + parser.currentName());
            }
        }

        if (!Double.isNaN(value)) {
            return new ActivePowerExtension(value);
        } else {
            return new ActivePowerExtension(preContingencyValue, postContingencyValue);
        }
    }
}
