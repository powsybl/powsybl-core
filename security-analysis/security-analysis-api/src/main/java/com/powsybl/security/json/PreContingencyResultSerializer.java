/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.results.PreContingencyResult;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PreContingencyResultSerializer extends StdSerializer<PreContingencyResult> {

    public PreContingencyResultSerializer() {
        super(PreContingencyResult.class);
    }

    @Override
    public void serialize(PreContingencyResult preContingencyResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        serializerProvider.defaultSerializeField("status", preContingencyResult.getStatus(), jsonGenerator);
        serializerProvider.defaultSerializeField("limitViolationsResult", preContingencyResult.getLimitViolationsResult(), jsonGenerator);
        serializerProvider.defaultSerializeField("networkResult", preContingencyResult.getNetworkResult(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
