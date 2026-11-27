/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityAnalysisResult;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.SensitivityStateStatus;
import com.powsybl.sensitivity.SensitivityValue;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityAnalysisResultDeserializer extends StdDeserializer<SensitivityAnalysisResult> {

    public static final String SOURCE_VERSION_ATTRIBUTE = "sourceVersionAttribute";

    private final ValueDeserializer<Object> factorsDeserializer;
    private final ValueDeserializer<Object> sensitivityValuesDeserializer;
    private final ValueDeserializer<Object> stateStatusDeserializer;
    private final ValueDeserializer<Object> contingencyIdsDeserializer;
    private final ValueDeserializer<Object> operatorStrategyIdsDeserializer;

    protected SensitivityAnalysisResultDeserializer() {
        this(null, null, null, null, null);
    }

    protected SensitivityAnalysisResultDeserializer(ValueDeserializer<Object> factorsDeserializer,
                                                    ValueDeserializer<Object> sensitivityValuesDeserializer,
                                                    ValueDeserializer<Object> stateStatusDeserializer,
                                                    ValueDeserializer<Object> contingencyIdsDeserializer,
                                                    ValueDeserializer<Object> operatorStrategyIdsDeserializer) {
        super(SensitivityAnalysisResult.class);
        this.factorsDeserializer = factorsDeserializer;
        this.sensitivityValuesDeserializer = sensitivityValuesDeserializer;
        this.stateStatusDeserializer = stateStatusDeserializer;
        this.contingencyIdsDeserializer = contingencyIdsDeserializer;
        this.operatorStrategyIdsDeserializer = operatorStrategyIdsDeserializer;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws DatabindException {
        return new SensitivityAnalysisResultDeserializer(
            JsonUtil.buildListDeserializer(ctxt, property, SensitivityFactor.class),
            JsonUtil.buildListDeserializer(ctxt, property, SensitivityValue.class),
            JsonUtil.buildListDeserializer(ctxt, property, SensitivityStateStatus.class),
            JsonUtil.buildListDeserializer(ctxt, property, String.class),
            JsonUtil.buildListDeserializer(ctxt, property, String.class)
        );
    }

    @Override
    public SensitivityAnalysisResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        String version = null;
        List<SensitivityValue> sensitivityValues = Collections.emptyList();
        List<SensitivityStateStatus> stateStatus = Collections.emptyList();
        List<String> contingencyIds = Collections.emptyList();
        List<String> operatorStrategyIds = Collections.emptyList();
        List<SensitivityFactor> factors = Collections.emptyList();
        Boolean computationComplete = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version":
                    parser.nextToken();
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
                    stateStatus = JsonUtil.readList(stateStatusDeserializer, deserializationContext, parser, SensitivityStateStatus.class);
                    break;

                case "stateStatus":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: stateStatus", version, "1.1");
                    parser.nextToken();
                    stateStatus = JsonUtil.readList(stateStatusDeserializer, deserializationContext, parser, SensitivityStateStatus.class);
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

                case "computationComplete":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: computationComplete", version, "1.3");
                    parser.nextToken();
                    computationComplete = JsonUtil.readValue(deserializationContext, parser, Boolean.class);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        if (JsonUtil.compareVersions(version, "1.0") < 0 || JsonUtil.compareVersions(version, "1.3") > 0) {
            throw new IllegalStateException("Only versions 1.0, 1.1, 1.2, 1.3 are supported.");
        }
        // Before version 1.3 computationComplete was not present, default to true
        if (JsonUtil.compareVersions(version, "1.3") < 0) {
            computationComplete = true;
        }
        if (JsonUtil.compareVersions(version, "1.0") == 0) {
            // In 1.0 the contingency IDs and the mapping contingency index -> ID were directly taken from 'contingencyStatus' list.
            // Therefore for this version, the "contingencyIds" tag was not encountered.
            // Note that these elements cannot be computed from the state statuses (for versions >= 1.1) because
            // they also contain some post operator strategy statuses and are not indexed by contingency
            contingencyIds = stateStatus.stream().map(s -> s.getState().contingencyId()).toList();
        }
        return new SensitivityAnalysisResult(factors, stateStatus, contingencyIds, operatorStrategyIds, sensitivityValues, Objects.requireNonNull(computationComplete));
    }
}
