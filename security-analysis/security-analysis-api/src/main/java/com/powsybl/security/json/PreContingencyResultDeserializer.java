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
import com.powsybl.security.results.*;

import java.io.IOException;
import java.util.Objects;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class PreContingencyResultDeserializer extends AbstractContingencyResultDeserializer<PreContingencyResult> {

    private static final String CONTEXT_NAME = "PreContingencyResult";

    private static class ParsingContext {
        LoadFlowResult.ComponentResult.Status status = null;
    }

    PreContingencyResultDeserializer() {
        super(PreContingencyResult.class);
    }

    @Override
    public PreContingencyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {

        String version = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        if (version == null) {  // assuming current version...
            version = SecurityAnalysisResultSerializer.VERSION;
        }
        final String finalVersion = version;
        ParsingContext parsingContext = new ParsingContext();
        AbstractContingencyResultDeserializer.ParsingContext commonParsingContext = new AbstractContingencyResultDeserializer.ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            boolean found = deserializeCommonAttributes(parser, commonParsingContext, name, deserializationContext, finalVersion, CONTEXT_NAME);
            if (found) {
                return true;
            }
            if (parser.getCurrentName().equals("status")) {
                parser.nextToken();
                JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: status",
                        finalVersion, "1.3");
                parsingContext.status = JsonUtil.readValue(deserializationContext, parser, LoadFlowResult.ComponentResult.Status.class);
                return true;
            }
            return false;
        });
        if (finalVersion.compareTo("1.3") < 0) {
            Objects.requireNonNull(commonParsingContext.limitViolationsResult);
            parsingContext.status = commonParsingContext.limitViolationsResult.isComputationOk() ? LoadFlowResult.ComponentResult.Status.CONVERGED : LoadFlowResult.ComponentResult.Status.FAILED;
        }
        if (commonParsingContext.networkResult != null) {
            return new PreContingencyResult(parsingContext.status, commonParsingContext.limitViolationsResult, commonParsingContext.networkResult);
        } else {
            return new PreContingencyResult(parsingContext.status, commonParsingContext.limitViolationsResult,
                    commonParsingContext.branchResults, commonParsingContext.busResults, commonParsingContext.threeWindingsTransformerResults);
        }
    }
}
