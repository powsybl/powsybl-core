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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.json.ContingencyDeserializer;
import com.powsybl.contingency.json.ContingencyElementDeserializer;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyResult;
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
 *
 * @author Olivier Bretteville <olivier.bretteville@rte-france.com>
 */
public class ApogeeSecurityAnalysisResultDeserializer extends StdDeserializer<SecurityAnalysisResult> {

    ApogeeSecurityAnalysisResultDeserializer() {
        super(SecurityAnalysisResult.class);
    }

    @Override
    public SecurityAnalysisResult deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        LimitViolationsResult preContingencyResult = null;
        List<PostContingencyResult> postContingencyResults = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // skip
                    break;

                case "preContingencyResult":
                    parser.nextToken();
                    preContingencyResult = parser.readValueAs(LimitViolationsResult.class);
                    break;

                case "postContingencyResults":
                    parser.nextToken();
                    postContingencyResults = parser.readValueAs(new TypeReference<ArrayList<PostContingencyResult>>() {
                    });
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new SecurityAnalysisResult(preContingencyResult, postContingencyResults);
    }

    public static SecurityAnalysisResult read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

            SimpleModule module = new SimpleModule();
            module.addDeserializer(SecurityAnalysisResult.class, new ApogeeSecurityAnalysisResultDeserializer());
            module.addDeserializer(PostContingencyResult.class, new PostContingencyResultDeserializer());
            module.addDeserializer(LimitViolationsResult.class, new LimitViolationResultDeserializer());
            module.addDeserializer(LimitViolation.class, new ApogeeLimitViolationDeserializer());
            module.addDeserializer(Contingency.class, new ContingencyDeserializer());
            module.addDeserializer(ContingencyElement.class, new ContingencyElementDeserializer());
            objectMapper.registerModule(module);

            return objectMapper.readValue(is, SecurityAnalysisResult.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
