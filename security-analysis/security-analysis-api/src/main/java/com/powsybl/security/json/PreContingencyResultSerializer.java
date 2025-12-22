/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.security.results.PreContingencyResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PreContingencyResultSerializer extends StdSerializer<PreContingencyResult> {

    public PreContingencyResultSerializer() {
        super(PreContingencyResult.class);
    }

    @Override
    public void serialize(PreContingencyResult preContingencyResult, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        serializationContext.defaultSerializeProperty("status", preContingencyResult.getStatus(), jsonGenerator);
        serializationContext.defaultSerializeProperty("limitViolationsResult", preContingencyResult.getLimitViolationsResult(), jsonGenerator);
        serializationContext.defaultSerializeProperty("networkResult", preContingencyResult.getNetworkResult(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
