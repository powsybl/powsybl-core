/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.FaultResult;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author Teofil-Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class ShortCircuitAnalysisResultDeserializer extends StdDeserializer<ShortCircuitAnalysisResult> {

    private static final String CONTEXT_NAME = "ShortCircuitAnalysisResult";

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
        Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "short-circuit-analysis"));

    ShortCircuitAnalysisResultDeserializer() {
        super(ShortCircuitAnalysisResult.class);
    }

    @Override
    public ShortCircuitAnalysisResult deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String version = null;
        List<FaultResult> faultResults = null;
        List<Extension<ShortCircuitAnalysisResult>> extensions = Collections.emptyList();
        FaultResult.Status globalStatus = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    version = parser.readValueAs(String.class);
                    break;

                case "faultResults":
                    faultResults = new FaultResultDeserializer().deserialize(parser, ctx, version);
                    break;

                case "globalStatus":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: " + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    globalStatus = FaultResult.Status.valueOf(parser.getValueAsString());
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, ctx, SUPPLIER.get());
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }

        ShortCircuitAnalysisResult shortCircuitAnalysisResult = null;
        if (globalStatus == null) {
            JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "No status", version, "1.1");
            if (faultResults != null) {
                boolean solverFailure = false;
                boolean noShortCircuitData = false;
                for (FaultResult result : faultResults) {
                    if (result.getStatus() == FaultResult.Status.FAILURE) {
                        shortCircuitAnalysisResult = new ShortCircuitAnalysisResult(faultResults, FaultResult.Status.FAILURE);
                        break;
                    } else if (result.getStatus() == FaultResult.Status.SOLVER_FAILURE) {
                        solverFailure = true;
                    } else if (result.getStatus() == FaultResult.Status.NO_SHORT_CIRCUIT_DATA) {
                        noShortCircuitData = true;
                    }
                    if (solverFailure) {
                        shortCircuitAnalysisResult = new ShortCircuitAnalysisResult(faultResults, FaultResult.Status.SOLVER_FAILURE);
                    } else if (noShortCircuitData) {
                        shortCircuitAnalysisResult = new ShortCircuitAnalysisResult(faultResults, FaultResult.Status.NO_SHORT_CIRCUIT_DATA);
                    } else {
                        shortCircuitAnalysisResult = new ShortCircuitAnalysisResult(faultResults, FaultResult.Status.SUCCESS);
                    }
                }
            } else {
                throw new PowsyblException("Fault result is null.");
            }
        } else {
            shortCircuitAnalysisResult = new ShortCircuitAnalysisResult(faultResults, globalStatus);
        }

        SUPPLIER.get().addExtensions(shortCircuitAnalysisResult, extensions);

        return shortCircuitAnalysisResult;
    }

    public static ShortCircuitAnalysisResult read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static ShortCircuitAnalysisResult read(InputStream is) {
        Objects.requireNonNull(is);

        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new ShortCircuitAnalysisJsonModule());
        try {
            return objectMapper.readValue(is, ShortCircuitAnalysisResult.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
