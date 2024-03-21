/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.action.json.ActionJsonModule;
import com.powsybl.commons.extensions.*;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.NetworkMetadata;
import com.powsybl.security.results.*;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.results.OperatorStrategyResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.it>}
 */
public class SecurityAnalysisResultDeserializer extends StdDeserializer<SecurityAnalysisResult> {

    private static final String CONTEXT_NAME = "SecurityAnalysisResult";
    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));

    public static final String SOURCE_VERSION_ATTRIBUTE = "sourceVersionAttribute";

    SecurityAnalysisResultDeserializer() {
        super(SecurityAnalysisResult.class);
    }

    @Override
    public SecurityAnalysisResult deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String version = null;
        NetworkMetadata networkMetadata = null;
        LimitViolationsResult limitViolationsResult = null;
        List<PostContingencyResult> postContingencyResults = Collections.emptyList();
        List<Extension<SecurityAnalysisResult>> extensions = Collections.emptyList();
        PreContingencyResult preContingencyResult = null;
        List<OperatorStrategyResult> operatorStrategyResults = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // skip
                    version = parser.getValueAsString();
                    JsonUtil.setSourceVersion(ctx, version, SOURCE_VERSION_ATTRIBUTE);
                    break;

                case "network":
                    parser.nextToken();
                    networkMetadata = JsonUtil.readValue(ctx, parser, NetworkMetadata.class);
                    break;

                case "preContingencyResult":
                    parser.nextToken();
                    if (version != null && version.equals("1.0")) {
                        limitViolationsResult = JsonUtil.readValue(ctx, parser, LimitViolationsResult.class);
                    } else {
                        preContingencyResult = JsonUtil.readValue(ctx, parser, PreContingencyResult.class);
                    }
                    break;

                case "postContingencyResults":
                    parser.nextToken();
                    postContingencyResults = JsonUtil.readList(ctx, parser, PostContingencyResult.class);
                    break;

                case "operatorStrategyResults":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: operatorStrategyResults", version, "1.2");
                    parser.nextToken();
                    operatorStrategyResults = JsonUtil.readList(ctx, parser, OperatorStrategyResult.class);
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, ctx, SUPPLIER.get());
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        SecurityAnalysisResult result = null;
        if (preContingencyResult == null) {
            LoadFlowResult.ComponentResult.Status status = null;
            if (limitViolationsResult != null && version.equals("1.0")) {
                status = limitViolationsResult.isComputationOk() ? LoadFlowResult.ComponentResult.Status.CONVERGED : LoadFlowResult.ComponentResult.Status.FAILED;
            } else {
                status = LoadFlowResult.ComponentResult.Status.CONVERGED;
            }
            result = new SecurityAnalysisResult(limitViolationsResult, status, postContingencyResults, Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), operatorStrategyResults);
        } else {
            result = new SecurityAnalysisResult(preContingencyResult, postContingencyResults, operatorStrategyResults);
        }
        result.setNetworkMetadata(networkMetadata);
        SUPPLIER.get().addExtensions(result, extensions);

        return result;
    }

    public static SecurityAnalysisResult read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static SecurityAnalysisResult read(InputStream is) {
        Objects.requireNonNull(is);

        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule())
                .registerModule(new ActionJsonModule());
        try {
            return objectMapper.readValue(is, SecurityAnalysisResult.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
