/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.*;
import com.powsybl.security.LimitViolationsResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractContingencyResultDeserializer<T extends AbstractContingencyResult> extends StdDeserializer<T> {
    protected AbstractContingencyResultDeserializer(Class<T> vc) {
        super(vc);
    }

    protected static class ParsingContext {
        LimitViolationsResult limitViolationsResult = null;
        NetworkResult networkResult = null;
        List<BranchResult> branchResults = Collections.emptyList();
        List<BusResult> busResults = Collections.emptyList();
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = Collections.emptyList();
    }

    protected boolean deserializeCommonAttributes(JsonParser parser, ParsingContext context, String name,
                                                  DeserializationContext deserializationContext, String version,
                                                  String contextName) throws IOException {
        switch (name) {
            case "limitViolationsResult":
                parser.nextToken();
                context.limitViolationsResult = JsonUtil.readValue(deserializationContext, parser,
                        LimitViolationsResult.class);
                return true;
            case "networkResult":
                parser.nextToken();
                JsonUtil.assertGreaterOrEqualThanReferenceVersion(contextName,
                        "Tag: networkResult", version, "1.2");
                context.networkResult = JsonUtil.readValue(deserializationContext, parser, NetworkResult.class);
                return true;
            case "busResults":
                parser.nextToken();
                JsonUtil.assertLessThanOrEqualToReferenceVersion(contextName, "Tag: busResults",
                        version, "1.1");
                context.busResults = JsonUtil.readList(deserializationContext, parser, BusResult.class);
                return true;
            case "branchResults":
                parser.nextToken();
                JsonUtil.assertLessThanOrEqualToReferenceVersion(contextName, "Tag: branchResults",
                        version, "1.1");
                context.branchResults = JsonUtil.readList(deserializationContext, parser, BranchResult.class);
                return true;
            case "threeWindingsTransformerResults":
                parser.nextToken();
                JsonUtil.assertLessThanOrEqualToReferenceVersion(contextName, "Tag: threeWindingsTransformerResults",
                        version, "1.1");
                context.threeWindingsTransformerResults = JsonUtil.readList(deserializationContext, parser,
                        ThreeWindingsTransformerResult.class);
                return true;
            default:
                return false;
        }
    }
}
