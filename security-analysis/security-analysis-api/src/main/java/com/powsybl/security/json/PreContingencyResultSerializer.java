/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.results.PreContingencyResult;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class PreContingencyResultSerializer extends StdSerializer<PreContingencyResult> {

    PreContingencyResultSerializer() {
        super(PreContingencyResult.class);
    }

    @Override
    public void serialize(PreContingencyResult preContingencyResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("limitViolationsResult", preContingencyResult.getLimitViolationsResult());
        jsonGenerator.writeObjectField("networkResult", preContingencyResult.getNetworkResult());
        jsonGenerator.writeEndObject();
    }
}
