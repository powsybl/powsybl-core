/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Suppliers;
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

import static com.powsybl.security.json.LimitViolationDeserializer.VIOLATION_LOCATION_SUPPORT;

/**
 *
 * @author Teofil-Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class ShortCircuitAnalysisResultDeserializer extends StdDeserializer<ShortCircuitAnalysisResult> {

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

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version" -> {
                    parser.nextToken();
                    version = parser.readValueAs(String.class);
                    ctx.setAttribute(VIOLATION_LOCATION_SUPPORT, version.compareTo("1.3") >= 0);
                }
                case "faultResults" -> faultResults = new FaultResultDeserializer().deserialize(parser, ctx, version);
                case "extensions" -> {
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, ctx, SUPPLIER.get());
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        ShortCircuitAnalysisResult result = new ShortCircuitAnalysisResult(faultResults);

        SUPPLIER.get().addExtensions(result, extensions);

        return result;
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
