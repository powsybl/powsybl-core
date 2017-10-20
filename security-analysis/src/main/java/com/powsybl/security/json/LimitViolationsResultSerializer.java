/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.LimitViolationsResult;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LimitViolationsResultSerializer extends StdSerializer<LimitViolationsResult> {

    private final Network network;

    private final LimitViolationFilter filter;

    public LimitViolationsResultSerializer(Network network, LimitViolationFilter filter) {
        super(LimitViolationsResult.class);

        this.network = network;
        this.filter = filter;
    }

    @Override
    public void serialize(LimitViolationsResult limitViolationsResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBooleanField("computationOk", limitViolationsResult.isComputationOk());
        jsonGenerator.writeObjectField("limitViolations", filter.apply(limitViolationsResult.getLimitViolations(), network));
        jsonGenerator.writeObjectField("actionsTaken", limitViolationsResult.getActionsTaken());
        jsonGenerator.writeEndObject();
    }
}
