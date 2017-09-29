/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.contingency.ContingencyElement;
import eu.itesla_project.contingency.json.ContingencyDeserializer;
import eu.itesla_project.contingency.json.ContingencyElementDeserializer;
import eu.itesla_project.security.LimitViolation;
import eu.itesla_project.security.LimitViolationsResult;
import eu.itesla_project.security.PostContingencyResult;
import eu.itesla_project.security.SecurityAnalysisResult;

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
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class SecurityAnalysisResultDeserializer extends StdDeserializer<SecurityAnalysisResult> {

    SecurityAnalysisResultDeserializer() {
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
            module.addDeserializer(SecurityAnalysisResult.class, new SecurityAnalysisResultDeserializer());
            module.addDeserializer(PostContingencyResult.class, new PostContingencyResultDeserializer());
            module.addDeserializer(LimitViolationsResult.class, new LimitViolationResultDeserializer());
            module.addDeserializer(LimitViolation.class, new LimitViolationDeserializer());
            module.addDeserializer(Contingency.class, new ContingencyDeserializer());
            module.addDeserializer(ContingencyElement.class, new ContingencyElementDeserializer());
            objectMapper.registerModule(module);

            return objectMapper.readValue(is, SecurityAnalysisResult.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
