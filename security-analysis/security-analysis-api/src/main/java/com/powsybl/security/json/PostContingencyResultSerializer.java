/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.security.results.PostContingencyResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class PostContingencyResultSerializer extends StdSerializer<PostContingencyResult> {

    public PostContingencyResultSerializer() {
        super(PostContingencyResult.class);
    }

    @Override
    public void serialize(PostContingencyResult postContingencyResult, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        serializationContext.defaultSerializeProperty("contingency", postContingencyResult.getContingency(), jsonGenerator);
        serializationContext.defaultSerializeProperty("status", postContingencyResult.getStatus(), jsonGenerator);
        serializationContext.defaultSerializeProperty("limitViolationsResult", postContingencyResult.getLimitViolationsResult(), jsonGenerator);
        serializationContext.defaultSerializeProperty("networkResult", postContingencyResult.getNetworkResult(), jsonGenerator);
        serializationContext.defaultSerializeProperty("connectivityResult", postContingencyResult.getConnectivityResult(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
