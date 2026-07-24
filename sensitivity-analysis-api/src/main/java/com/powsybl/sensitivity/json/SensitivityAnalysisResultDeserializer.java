/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityAnalysisResult;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.SensitivityValue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityAnalysisResultDeserializer extends StdDeserializer<SensitivityAnalysisResult> implements ContextualDeserializer {

    public static final String SOURCE_VERSION_ATTRIBUTE = "sourceVersionAttribute";

    private final transient JsonDeserializer<Object> factorsDeserializer;
    private final transient JsonDeserializer<Object> sensitivityValuesDeserializer;
    private final transient JsonDeserializer<Object> stateStatusDeserializer;
    //TODO both are deserializer on strings, could we use the same ?
    private final transient JsonDeserializer<Object> contingencyIdsDeserializer;
    private final transient JsonDeserializer<Object> operatorStrategyIdsDeserializer;

    protected SensitivityAnalysisResultDeserializer() {
        this(null, null, null, null, null);
    }

    protected SensitivityAnalysisResultDeserializer(JsonDeserializer<Object> factorsDeserializer,
                                                    JsonDeserializer<Object> sensitivityValuesDeserializer,
                                                    JsonDeserializer<Object> stateStatusDeserializer,
                                                    JsonDeserializer<Object> contingencyIdsDeserializer,
                                                    JsonDeserializer<Object> operatorStrategyIdsDeserializer) {
        super(SensitivityAnalysisResult.class);
        this.factorsDeserializer = factorsDeserializer;
        this.sensitivityValuesDeserializer = sensitivityValuesDeserializer;
        this.stateStatusDeserializer = stateStatusDeserializer;
        this.contingencyIdsDeserializer = contingencyIdsDeserializer;
        this.operatorStrategyIdsDeserializer = operatorStrategyIdsDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new SensitivityAnalysisResultDeserializer(
            JsonUtil.buildListDeserializer(ctxt, property, SensitivityFactor.class),
            JsonUtil.buildListDeserializer(ctxt, property, SensitivityValue.class),
            JsonUtil.buildListDeserializer(ctxt, property, SensitivityAnalysisResult.SensitivityStateStatus.class),
            JsonUtil.buildListDeserializer(ctxt, property, String.class),
            JsonUtil.buildListDeserializer(ctxt, property, String.class)
        );
    }

    @Override
    public SensitivityAnalysisResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String version = null;
        List<SensitivityValue> sensitivityValues = Collections.emptyList();
        List<SensitivityAnalysisResult.SensitivityStateStatus> stateStatus = Collections.emptyList();
        List<String> contingencyIds = Collections.emptyList();
        List<String> operatorStrategyIds = Collections.emptyList();
        List<SensitivityFactor> factors = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version":
                    parser.nextToken(); // skip
                    version = parser.getValueAsString();
                    JsonUtil.setSourceVersion(deserializationContext, version, SOURCE_VERSION_ATTRIBUTE);
                    break;

                case "sensitivityFactors":
                    parser.nextToken();
                    factors = JsonUtil.readList(factorsDeserializer, deserializationContext, parser, SensitivityFactor.class);
                    break;

                case "sensitivityValues":
                    parser.nextToken();
                    sensitivityValues = JsonUtil.readList(sensitivityValuesDeserializer, deserializationContext, parser, SensitivityValue.class);
                    break;

                case "contingencyStatus":
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: contingencyStatus", version, "1.0");
                    parser.nextToken();
                    stateStatus = JsonUtil.readList(stateStatusDeserializer, deserializationContext, parser, SensitivityAnalysisResult.SensitivityStateStatus.class);
                    break;

                case "stateStatus":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: stateStatus", version, "1.1");
                    parser.nextToken();
                    stateStatus = JsonUtil.readList(stateStatusDeserializer, deserializationContext, parser, SensitivityAnalysisResult.SensitivityStateStatus.class);
                    break;

                case "contingencyIds":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: contingencyIds", version, "1.1");
                    parser.nextToken();
                    contingencyIds = JsonUtil.readList(contingencyIdsDeserializer, deserializationContext, parser, String.class);
                    break;

                case "operatorStrategyIds":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: operatorStrategyIds", version, "1.1");
                    parser.nextToken();
                    operatorStrategyIds = JsonUtil.readList(operatorStrategyIdsDeserializer, deserializationContext, parser, String.class);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        if (!"1.0".equals(version) && !"1.1".equals(version) && !"1.2".equals(version)) {
            throw new IllegalStateException("Only version 1.0, 1.1 and 1.2 are supported.");
        }
        if ("1.0".equals(version)) {
            // In 1.0 the contingency IDs and the mapping contingency index -> ID were directly taken from 'contingencyStatus' list.
            // Therefore for this version, the "contingencyIds" tag was not encountered.
            // Note that these elements cannot be computed from the state statuses (for versions >= 1.1) because
            // they also contain some post operator strategy statuses and are not indexed by contingency
            contingencyIds = stateStatus.stream().map(s -> s.getState().contingencyId()).toList();
        }
        return new SensitivityAnalysisResult(factors, stateStatus, contingencyIds, operatorStrategyIds, sensitivityValues);
    }
}
