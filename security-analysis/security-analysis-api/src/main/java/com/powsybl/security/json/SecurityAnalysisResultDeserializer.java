/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.*;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.NetworkMetadata;
import com.powsybl.security.results.*;
import com.powsybl.security.SecurityAnalysisResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class SecurityAnalysisResultDeserializer extends StdDeserializer<SecurityAnalysisResult> {

    private static final String CONTEXT_NAME = "SecurityAnalysisResult";

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
        Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));

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
        List<BranchResult> branchResults = Collections.emptyList();
        List<BusResults> busResults = Collections.emptyList();
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // skip
                    version = parser.getValueAsString();
                    break;

                case "network":
                    parser.nextToken();
                    networkMetadata = parser.readValueAs(NetworkMetadata.class);
                    break;

                case "preContingencyResult":
                    parser.nextToken();
                    if (version != null && version.equals("1.0")) {
                        limitViolationsResult = parser.readValueAs(LimitViolationsResult.class);
                    } else {
                        preContingencyResult = parser.readValueAs(PreContingencyResult.class);
                    }
                    break;

                case "postContingencyResults":
                    parser.nextToken();
                    postContingencyResults = parser.readValueAs(new TypeReference<ArrayList<PostContingencyResult>>() {
                    });
                    break;

                case "preContingencyBranchResults":
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: preContingencyBranchResults", version, "1.0");
                    parser.nextToken();
                    branchResults = parser.readValueAs(new TypeReference<ArrayList<BranchResult>>() {
                    });
                    break;
                case "preContingencyBusResults":
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: preContingencyBusResults", version, "1.0");
                    parser.nextToken();
                    busResults = parser.readValueAs(new TypeReference<ArrayList<BusResults>>() {
                    });
                    break;
                case "preContingencyThreeWindingsTransformerResults":
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: preContingencyThreeWindingsTransformerResults", version, "1.0");
                    parser.nextToken();
                    threeWindingsTransformerResults = parser.readValueAs(new TypeReference<ArrayList<ThreeWindingsTransformerResult>>() {
                    });
                    break;
                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, ctx, SUPPLIER.get());
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        SecurityAnalysisResult result = null;
        if (preContingencyResult == null) {
            result = new SecurityAnalysisResult(limitViolationsResult, postContingencyResults, branchResults, busResults, threeWindingsTransformerResults);
        } else {
            result = new SecurityAnalysisResult(preContingencyResult, postContingencyResults);
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
            .registerModule(new SecurityAnalysisJsonModule());
        try {
            return objectMapper.readValue(is, SecurityAnalysisResult.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
