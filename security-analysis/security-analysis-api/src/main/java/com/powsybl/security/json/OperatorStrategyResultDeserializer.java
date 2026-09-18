/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;
import com.powsybl.security.results.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategyResultDeserializer extends StdDeserializer<OperatorStrategyResult> implements ContextualDeserializer {

    private static final String CONTEXT_NAME = "OperatorStrategyResult";

    private final transient JsonDeserializer<Object> operatorStrategyDeserializer;
    private final transient JsonDeserializer<Object> limitViolationsResultDeserializer;
    private final transient JsonDeserializer<Object> networkResultDeserializer;
    private final transient JsonDeserializer<Object> conditionalActionsResultDeserializer;

    public OperatorStrategyResultDeserializer() {
        this(null, null, null, null);
    }

    public OperatorStrategyResultDeserializer(JsonDeserializer<Object> operatorStrategyDeserializer,
                                              JsonDeserializer<Object> limitViolationsResultDeserializer,
                                              JsonDeserializer<Object> networkResultDeserializer,
                                              JsonDeserializer<Object> conditionalActionsResultDeserializer) {
        super(OperatorStrategyResult.class);
        this.operatorStrategyDeserializer = operatorStrategyDeserializer;
        this.limitViolationsResultDeserializer = limitViolationsResultDeserializer;
        this.networkResultDeserializer = networkResultDeserializer;
        this.conditionalActionsResultDeserializer = conditionalActionsResultDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new OperatorStrategyResultDeserializer(
            JsonUtil.buildValueDeserializer(ctxt, property, OperatorStrategy.class),
            JsonUtil.buildValueDeserializer(ctxt, property, LimitViolationsResult.class),
            JsonUtil.buildValueDeserializer(ctxt, property, NetworkResult.class),
            JsonUtil.buildListDeserializer(ctxt, property, OperatorStrategyResult.ConditionalActionsResult.class)
        );
    }

    @Override
    public OperatorStrategyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        OperatorStrategy operatorStrategy = null;
        LimitViolationsResult limitViolationsResult = null;
        NetworkResult networkResult = null;
        PostContingencyComputationStatus status = null;
        List<OperatorStrategyResult.ConditionalActionsResult> conditionalActionsResultList = null;
        String version = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        if (version == null) {  // assuming current version...
            version = SecurityAnalysisResultSerializer.VERSION;
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "operatorStrategy":
                    parser.nextToken();
                    // operator strategies are independent of security analysis, we need to shift to operator strategy
                    // version values
                    // <= 1.4 -> 1.0
                    // between 1.5 and 1.7 -> 1.1
                    // >= 1.8 -> 1.2
                    String operatorStrategyVersion;
                    if (version.compareTo("1.4") <= 0) {
                        operatorStrategyVersion = "1.0";
                    } else if (version.compareTo("1.5") >= 0 && version.compareTo("1.7") <= 0) {
                        operatorStrategyVersion = "1.1";
                    } else {
                        operatorStrategyVersion = "1.2";
                    }
                    JsonUtil.setSourceVersion(deserializationContext, operatorStrategyVersion, SOURCE_VERSION_ATTRIBUTE);
                    operatorStrategy = JsonUtil.readValue(operatorStrategyDeserializer, deserializationContext, parser, OperatorStrategy.class);
                    JsonUtil.setSourceVersion(deserializationContext, version, SOURCE_VERSION_ATTRIBUTE); // restore
                    break;

                case "limitViolationsResult":
                    parser.nextToken();
                    limitViolationsResult = JsonUtil.readValue(limitViolationsResultDeserializer, deserializationContext, parser, LimitViolationsResult.class);
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: limitViolationsResult",
                            version, "1.5");
                    break;

                case "networkResult":
                    parser.nextToken();
                    networkResult = JsonUtil.readValue(networkResultDeserializer, deserializationContext, parser, NetworkResult.class);
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: networkResult",
                            version, "1.5");
                    break;

                case "status":
                    status = PostContingencyComputationStatus.valueOf(parser.nextTextValue());
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: contingencyStatus",
                            version, "1.3");
                    JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, "Tag: contingencyStatus",
                            version, "1.6");
                    break;

                case "conditionalActionsResults":
                    parser.nextToken();
                    conditionalActionsResultList = JsonUtil.readList(conditionalActionsResultDeserializer, deserializationContext, parser, OperatorStrategyResult.ConditionalActionsResult.class);
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: conditionalActionsResults",
                            version, "1.6");
                    break;
                default:
                    throw new JsonMappingException(parser, "Unexpected field: " + parser.currentName());
            }
        }
        Objects.requireNonNull(operatorStrategy);
        if (version.compareTo("1.3") < 0) {
            Objects.requireNonNull(limitViolationsResult);
            return new OperatorStrategyResult(operatorStrategy, List.of(
                    new OperatorStrategyResult.ConditionalActionsResult(
                            operatorStrategy.getId(),
                            limitViolationsResult.isComputationOk() ? PostContingencyComputationStatus.CONVERGED : PostContingencyComputationStatus.FAILED,
                            limitViolationsResult, networkResult, Double.NaN
                    )));
        } else if (version.compareTo("1.6") < 0) {
            return new OperatorStrategyResult(operatorStrategy, List.of(
                    new OperatorStrategyResult.ConditionalActionsResult(
                            operatorStrategy.getId(),
                            status,
                            limitViolationsResult, networkResult, Double.NaN
                    )));
        } else {
            return new OperatorStrategyResult(operatorStrategy, conditionalActionsResultList);
        }
    }
}
