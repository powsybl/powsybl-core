/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.BranchResult;
import com.powsybl.security.results.BusResult;
import com.powsybl.security.results.NetworkResult;
import com.powsybl.security.results.ThreeWindingsTransformerResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class NetworkResultDeserializer extends StdDeserializer<NetworkResult> {

    private final transient ValueDeserializer<Object> branchResultDeserializer;
    private final transient ValueDeserializer<Object> busResultDeserializer;
    private final transient ValueDeserializer<Object> threeWindingsTransformerResultDeserializer;

    public NetworkResultDeserializer() {
        this(null, null, null);
    }

    public NetworkResultDeserializer(ValueDeserializer<Object> branchResultDeserializer,
                                     ValueDeserializer<Object> busResultDeserializer,
                                     ValueDeserializer<Object> threeWindingsTransformerResultDeserializer) {
        super(NetworkResult.class);
        this.branchResultDeserializer = branchResultDeserializer;
        this.busResultDeserializer = busResultDeserializer;
        this.threeWindingsTransformerResultDeserializer = threeWindingsTransformerResultDeserializer;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws DatabindException {
        return new NetworkResultDeserializer(
            JsonUtil.buildListDeserializer(ctxt, property, BranchResult.class),
            JsonUtil.buildListDeserializer(ctxt, property, BusResult.class),
            JsonUtil.buildListDeserializer(ctxt, property, ThreeWindingsTransformerResult.class)
        );
    }

    @Override
    public NetworkResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        List<BranchResult> branchResults = null;
        List<BusResult> busResults = null;
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "branchResults":
                    parser.nextToken();
                    branchResults = JsonUtil.readList(branchResultDeserializer, deserializationContext, parser, BranchResult.class);
                    break;

                case "busResults":
                    parser.nextToken();
                    busResults = JsonUtil.readList(busResultDeserializer, deserializationContext, parser, BusResult.class);
                    break;

                case "threeWindingsTransformerResults":
                    parser.nextToken();
                    threeWindingsTransformerResults = JsonUtil.readList(threeWindingsTransformerResultDeserializer, deserializationContext, parser, ThreeWindingsTransformerResult.class);
                    break;

                default:
                    throw DatabindException.from(parser, "Unexpected field: " + parser.currentName());
            }
        }
        return new NetworkResult(branchResults, busResults, threeWindingsTransformerResults);
    }
}
