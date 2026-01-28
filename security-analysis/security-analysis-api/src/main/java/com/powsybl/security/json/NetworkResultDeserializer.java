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
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class NetworkResultDeserializer extends StdDeserializer<NetworkResult> {

    public NetworkResultDeserializer() {
        super(NetworkResult.class);
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
                    branchResults = JsonUtil.readList(deserializationContext, parser, BranchResult.class);
                    break;

                case "busResults":
                    parser.nextToken();
                    busResults = JsonUtil.readList(deserializationContext, parser, BusResult.class);
                    break;

                case "threeWindingsTransformerResults":
                    parser.nextToken();
                    threeWindingsTransformerResults = JsonUtil.readList(deserializationContext, parser, ThreeWindingsTransformerResult.class);
                    break;

                default:
                    throw DatabindException.from(parser, "Unexpected field: " + parser.currentName());
            }
        }
        return new NetworkResult(branchResults, busResults, threeWindingsTransformerResults);
    }
}
