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
import com.fasterxml.jackson.databind.DeserializationContext;
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
public class SensitivityAnalysisResultDeserializer extends StdDeserializer<SensitivityAnalysisResult> {

    public static final String SOURCE_VERSION_ATTRIBUTE = "sourceVersionAttribute";

    protected SensitivityAnalysisResultDeserializer() {
        super(SensitivityAnalysisResult.class);
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
                    factors = JsonUtil.readList(deserializationContext, parser, SensitivityFactor.class);
                    break;

                case "sensitivityValues":
                    parser.nextToken();
                    sensitivityValues = JsonUtil.readList(deserializationContext, parser, SensitivityValue.class);
                    break;

                case "contingencyStatus":
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: contingencyStatus", version, "1.0");
                    parser.nextToken();
                    stateStatus = JsonUtil.readList(deserializationContext, parser, SensitivityAnalysisResult.SensitivityStateStatus.class);
                    break;

                case "stateStatus":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: stateStatus", version, "1.1");
                    parser.nextToken();
                    stateStatus = JsonUtil.readList(deserializationContext, parser, SensitivityAnalysisResult.SensitivityStateStatus.class);
                    break;

                case "contingencyIds":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: contingencyIds", version, "1.1");
                    parser.nextToken();
                    contingencyIds = JsonUtil.readList(deserializationContext, parser, String.class);
                    break;

                case "operatorStrategyIds":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: operatorStrategyIds", version, "1.1");
                    parser.nextToken();
                    operatorStrategyIds = JsonUtil.readList(deserializationContext, parser, String.class);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        if (version == null || !version.equals("1.0") && !version.equals("1.1")) {
            throw new IllegalStateException("Only version 1.0 and 1.1 are supported.");
        }
        if ("1.0".equals(version)) {
            // in 1.0 the contingency IDs and the mapping contingency index -> ID was taken from 'contingencyStatus' list
            // not possible anymore when >= 1.1 because the new 'stateStatus' can contain also some post operator strategy
            // statuses and are not indexed in contingencies
            contingencyIds = stateStatus.stream().map(s -> s.getState().contingencyId()).toList();
        }
        return new SensitivityAnalysisResult(factors, stateStatus, contingencyIds, operatorStrategyIds, sensitivityValues);
    }
}
