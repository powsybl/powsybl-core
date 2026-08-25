/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.results.MovedPhaseShifterResult;
import com.powsybl.security.results.NetworkResult;
import com.powsybl.security.results.PreContingencyResult;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PreContingencyResultDeserializer extends AbstractContingencyResultDeserializer<PreContingencyResult> {

    private static final String CONTEXT_NAME = "PreContingencyResult";

    private static final class ParsingContext {
        LoadFlowResult.ComponentResult.Status status = null;
        Map<String, MovedPhaseShifterResult> phaseShifterResults = Collections.emptyMap();
    }

    public PreContingencyResultDeserializer() {
        super(PreContingencyResult.class);
    }

    @Override
    public PreContingencyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String version = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        if (version == null) {
            version = SecurityAnalysisResultSerializer.VERSION;
        }
        final String finalVersion = version;
        ParsingContext parsingContext = new ParsingContext();
        AbstractContingencyResultDeserializer.ParsingContext commonParsingContext =
                new AbstractContingencyResultDeserializer.ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> parsePreContingencyResult(
                parser, deserializationContext, parsingContext, finalVersion, commonParsingContext, name));
        if (finalVersion.compareTo("1.3") < 0) {
            Objects.requireNonNull(commonParsingContext.limitViolationsResult);
            parsingContext.status = commonParsingContext.limitViolationsResult.isComputationOk()
                    ? LoadFlowResult.ComponentResult.Status.CONVERGED
                    : LoadFlowResult.ComponentResult.Status.FAILED;
        }
        return new PreContingencyResult(
                parsingContext.status,
                commonParsingContext.limitViolationsResult,
                Objects.requireNonNullElseGet(commonParsingContext.networkResult,
                        () -> new NetworkResult(commonParsingContext.branchResults,
                                commonParsingContext.busResults, commonParsingContext.threeWindingsTransformerResults)),
                commonParsingContext.distributedActivePower,
                parsingContext.phaseShifterResults);
    }

    private boolean parsePreContingencyResult(JsonParser parser, DeserializationContext deserializationContext,
                                               ParsingContext parsingContext, String finalVersion,
                                               AbstractContingencyResultDeserializer.ParsingContext commonParsingContext,
                                               String name) throws IOException {
        boolean found = deserializeCommonAttributes(parser, commonParsingContext, name, deserializationContext,
                finalVersion, CONTEXT_NAME);
        if (found) {
            return true;
        }
        if (parser.currentName().equals("status")) {
            parser.nextToken();
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: status", finalVersion, "1.3");
            parsingContext.status = JsonUtil.readValue(deserializationContext, parser,
                    LoadFlowResult.ComponentResult.Status.class);
            return true;
        }
        if (parser.currentName().equals("phaseShifterResults")) {
            parser.nextToken();
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(
                    CONTEXT_NAME, "Tag: phaseShifterResults", finalVersion, "2.0");
            parsingContext.phaseShifterResults = readPhaseShifterResults(parser, deserializationContext);
            return true;
        }
        return false;
    }

    private static Map<String, MovedPhaseShifterResult> readPhaseShifterResults(
            JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        Map<String, MovedPhaseShifterResult> results = new LinkedHashMap<>();
        java.util.List<?> list = JsonUtil.readList(deserializationContext, parser, MovedPhaseShifterResult.class);
        for (Object obj : list) {
            if (obj instanceof MovedPhaseShifterResult result) {
                results.put(result.transformerId(), result);
            }
        }
        return results;
    }
}
