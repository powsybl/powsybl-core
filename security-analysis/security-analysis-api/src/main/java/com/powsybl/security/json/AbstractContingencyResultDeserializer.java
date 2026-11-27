/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.results.*;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractContingencyResultDeserializer<T extends AbstractContingencyResult> extends StdDeserializer<T> {

    protected final transient ValueDeserializer<Object> limitViolationsResultDeserializer;
    protected final transient ValueDeserializer<Object> networkResultDeserializer;
    protected final transient ValueDeserializer<Object> busResultDeserializer;
    protected final transient ValueDeserializer<Object> branchResultDeserializer;
    protected final transient ValueDeserializer<Object> threeWindingsTransformerResultDeserializer;

    protected AbstractContingencyResultDeserializer(Class<T> vc) {
        this(vc, null, null, null, null, null);
    }

    protected AbstractContingencyResultDeserializer(Class<T> vc,
                                                    ValueDeserializer<Object> limitViolationsResultDeserializer,
                                                    ValueDeserializer<Object> networkResultDeserializer,
                                                    ValueDeserializer<Object> busResultDeserializer,
                                                    ValueDeserializer<Object> branchResultDeserializer,
                                                    ValueDeserializer<Object> threeWindingsTransformerResultDeserializer) {
        super(vc);
        this.limitViolationsResultDeserializer = limitViolationsResultDeserializer;
        this.networkResultDeserializer = networkResultDeserializer;
        this.busResultDeserializer = busResultDeserializer;
        this.branchResultDeserializer = branchResultDeserializer;
        this.threeWindingsTransformerResultDeserializer = threeWindingsTransformerResultDeserializer;
    }

    protected abstract ValueDeserializer<T> create(ValueDeserializer<Object> limitViolationsResultDeserializer,
                                                  ValueDeserializer<Object> networkResultDeserializer,
                                                  ValueDeserializer<Object> busResultDeserializer,
                                                  ValueDeserializer<Object> branchResultDeserializer,
                                                  ValueDeserializer<Object> threeWindingsTransformerResultDeserializer);

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return create(JsonUtil.buildValueDeserializer(ctxt, property, LimitViolationsResult.class),
            JsonUtil.buildValueDeserializer(ctxt, property, NetworkResult.class),
            JsonUtil.buildValueDeserializer(ctxt, property, BusResult.class),
            JsonUtil.buildValueDeserializer(ctxt, property, BranchResult.class),
            JsonUtil.buildValueDeserializer(ctxt, property, ThreeWindingsTransformerResult.class));
    }

    protected static class ParsingContext {
        LimitViolationsResult limitViolationsResult = null;
        NetworkResult networkResult = null;
        double distributedActivePower = Double.NaN;
        List<BranchResult> branchResults = Collections.emptyList();
        List<BusResult> busResults = Collections.emptyList();
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = Collections.emptyList();
    }

    protected boolean deserializeCommonAttributes(JsonParser parser, ParsingContext context, String name,
                                                  DeserializationContext deserializationContext, String version,
                                                  String contextName) throws JacksonException {
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
            case "distributedActivePower":
                parser.nextToken();
                JsonUtil.assertGreaterOrEqualThanReferenceVersion(contextName,
                        "Tag: distributedActivePower", version, "1.9");
                context.distributedActivePower = parser.getValueAsDouble();
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
