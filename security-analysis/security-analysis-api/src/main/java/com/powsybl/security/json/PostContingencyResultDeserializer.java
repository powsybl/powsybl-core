/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.PostContingencyComputationStatus;
import com.powsybl.security.results.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class PostContingencyResultDeserializer extends AbstractContingencyResultDeserializer<PostContingencyResult> {

    protected static final String CONTEXT_NAME = "PostContingencyResult";

    PostContingencyResultDeserializer() {
        super(PostContingencyResult.class);
    }

    private static class ParsingContext {
        Contingency contingency = null;
        PostContingencyComputationStatus status = null;
        ConnectivityResult connectivityResult = null;
    }

    @Override
    public PostContingencyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
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
            switch (parser.getCurrentName()) {
                case "contingency":
                    parser.nextToken();
                    parsingContext.contingency = JsonUtil.readValue(deserializationContext, parser, Contingency.class);
                    return true;
                case "status":
                    parser.nextToken();
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: status",
                            finalVersion, "1.3");
                    parsingContext.status = JsonUtil.readValue(deserializationContext, parser, PostContingencyComputationStatus.class);
                    return true;
                case "connectivityResult":
                    parser.nextToken();
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: connectivityResult",
                            finalVersion, "1.4");
                    parsingContext.connectivityResult = JsonUtil.readValue(deserializationContext, parser, ConnectivityResult.class);
                    return true;
                default:
                    return false;
            }
        });

        if (parsingContext.connectivityResult == null) {
            parsingContext.connectivityResult = new ConnectivityResult(0, 0, 0.0, 0.0, Collections.emptySet());
        }

        if (version.compareTo("1.3") < 0) {
            Objects.requireNonNull(commonParsingContext.limitViolationsResult);
            parsingContext.status = commonParsingContext.limitViolationsResult.isComputationOk() ? PostContingencyComputationStatus.CONVERGED : PostContingencyComputationStatus.FAILED;
        }
        if (commonParsingContext.networkResult != null) {
            return new PostContingencyResult(parsingContext.contingency, parsingContext.status, commonParsingContext.limitViolationsResult,
                    commonParsingContext.networkResult, parsingContext.connectivityResult);
        } else {
            return new PostContingencyResult(parsingContext.contingency, parsingContext.status, commonParsingContext.limitViolationsResult,
                    commonParsingContext.branchResults, commonParsingContext.busResults, commonParsingContext.threeWindingsTransformerResults,
                    parsingContext.connectivityResult);
        }
    }
}
