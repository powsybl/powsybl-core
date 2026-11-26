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
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.results.ChangedPhaseTapChanger;
import com.powsybl.security.results.NetworkResult;
import com.powsybl.security.results.PreContingencyResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PreContingencyResultDeserializer extends AbstractContingencyResultDeserializer<PreContingencyResult> {

    private static final String CONTEXT_NAME = "PreContingencyResult";

    private static final class ParsingContext {
        LoadFlowResult.ComponentResult.Status status = null;
        List<ChangedPhaseTapChanger> changedPhaseTapChangers = Collections.emptyList();
    }

    public PreContingencyResultDeserializer() {
        super(PreContingencyResult.class);
    }

    public PreContingencyResultDeserializer(JsonDeserializer<Object> limitViolationsResultDeserializer,
                                             JsonDeserializer<Object> networkResultDeserializer,
                                             JsonDeserializer<Object> busResultDeserializer,
                                             JsonDeserializer<Object> branchResultDeserializer,
                                             JsonDeserializer<Object> threeWindingsTransformerResultDeserializer) {
        super(PreContingencyResult.class,
            limitViolationsResultDeserializer,
            networkResultDeserializer,
            busResultDeserializer,
            branchResultDeserializer,
            threeWindingsTransformerResultDeserializer);
    }

    @Override
    protected PreContingencyResultDeserializer create(JsonDeserializer<Object> limitViolationsResultDeserializer,
                                         JsonDeserializer<Object> networkResultDeserializer,
                                         JsonDeserializer<Object> busResultDeserializer,
                                         JsonDeserializer<Object> branchResultDeserializer,
                                         JsonDeserializer<Object> threeWindingsTransformerResultDeserializer) {
        return new PreContingencyResultDeserializer(
            limitViolationsResultDeserializer,
            networkResultDeserializer,
            busResultDeserializer,
            branchResultDeserializer,
            threeWindingsTransformerResultDeserializer);
    }

    @Override
    public PreContingencyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String version = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        if (version == null) {  // assuming current version when version is not specified
            version = SecurityAnalysisResultSerializer.VERSION;
        }
        final String finalVersion = version;
        ParsingContext parsingContext = new ParsingContext();
        AbstractContingencyResultDeserializer.ParsingContext commonParsingContext =
                new AbstractContingencyResultDeserializer.ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> parsePreContingencyResult(
                parser, deserializationContext, parsingContext, finalVersion, commonParsingContext, name));
        if (JsonUtil.compareVersions(finalVersion, "1.3") < 0) {
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
                parsingContext.changedPhaseTapChangers);
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
        if ("status".equals(parser.currentName())) {
            parser.nextToken();
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: status", finalVersion, "1.3");
            parsingContext.status = JsonUtil.readValue(deserializationContext, parser,
                    LoadFlowResult.ComponentResult.Status.class);
            return true;
        } else if ("changedPhaseTapChangers".equals(parser.currentName())) {
            parser.nextToken();
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(
                    CONTEXT_NAME, "Tag: changedPhaseTapChangers", finalVersion, "1.10");
            parsingContext.changedPhaseTapChangers = ChangedPhaseTapChangerSerializerUtil.readChangedPhaseTapChangers(parser, deserializationContext);
            return true;
        }
        return false;
    }
}
