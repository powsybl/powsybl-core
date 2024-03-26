/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.results.PostContingencyResult;

import java.io.IOException;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class PostContingencyResultSerializer extends StdSerializer<PostContingencyResult> {

    PostContingencyResultSerializer() {
        super(PostContingencyResult.class);
    }

    @Override
    public void serialize(PostContingencyResult postContingencyResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        serializerProvider.defaultSerializeField("contingency", postContingencyResult.getContingency(), jsonGenerator);
        serializerProvider.defaultSerializeField("status", postContingencyResult.getStatus(), jsonGenerator);
        serializerProvider.defaultSerializeField("limitViolationsResult", postContingencyResult.getLimitViolationsResult(), jsonGenerator);
        serializerProvider.defaultSerializeField("networkResult", postContingencyResult.getNetworkResult(), jsonGenerator);
        serializerProvider.defaultSerializeField("connectivityResult", postContingencyResult.getConnectivityResult(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
