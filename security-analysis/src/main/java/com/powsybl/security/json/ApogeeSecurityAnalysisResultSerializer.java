/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.json.ContingencyElementSerializer;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Olivier Bretteville <olivier.bretteville@rte-france.com>
 */
public class ApogeeSecurityAnalysisResultSerializer extends StdSerializer<SecurityAnalysisResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApogeeSecurityAnalysisResultSerializer.class);

    enum TypeSitu {
        PF,
        MIXTE,
        PREVISIONNEL,
        INCONNU
    };

    private static final String VERSION = "1.0";
    private final Network network;
    private final String computationTimestamp;
    private final String caseTimestamp;
    private TypeSituation typeSitu;

    public class TypeSituation {
        public String computationTimestamp;
        public String caseTimestamp;
        public TypeSitu typeSitu;

        public TypeSituation() {
            computationTimestamp = null;
            caseTimestamp = null;
            typeSitu = TypeSitu.INCONNU;
        }

        public boolean doAnalyse(String input) {
            // String situ="PtPrevEcct-20170601-0930-20170531-2030";
            String regex = "(\\w+)-(\\d+-\\d+)-(\\d+-\\d+)";
            String regex2 = "(\\w+)-(\\d+-\\d+)";

            // compilation de la regex et recherche
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(input);

            if (m.matches()) {
                if ("PtPrevEcct".compareTo(m.group(1)) == 0) {
                    typeSitu = TypeSitu.PREVISIONNEL;
                } else if ("PtPrevPf".compareTo(m.group(1)) == 0) {
                    typeSitu = TypeSitu.MIXTE;
                } else {
                    typeSitu = TypeSitu.INCONNU;
                }

                caseTimestamp = m.group(2);
                computationTimestamp = m.group(3);

                return true;
            } else {
                p = Pattern.compile(regex2);
                m = p.matcher(input);
                if (m.matches()) {
                    if ("PtFige".compareTo(m.group(1)) == 0) {
                        typeSitu = TypeSitu.PF;
                    } else {
                        typeSitu = TypeSitu.INCONNU;
                    }

                    caseTimestamp = m.group(2);
                    computationTimestamp = caseTimestamp;

                    return true;
                }
            }
            return false;
        }

        public String getComputationTimestamp() {
            return computationTimestamp;
        }

        public String getCaseTimestamp() {
            return caseTimestamp;
        }

        public TypeSitu getTypeSitu() {
            return typeSitu;
        }
    }

    ApogeeSecurityAnalysisResultSerializer(Network network) {
        super(SecurityAnalysisResult.class);

        this.network = network;
        this.computationTimestamp = null;
        this.caseTimestamp = null;
        this.typeSitu = null;

        TypeSituation result = new TypeSituation();
        if (result.doAnalyse(network.getName())) {
            this.typeSitu = result;
        }
    }

    @Override
    public void serialize(SecurityAnalysisResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", VERSION);
        if (typeSitu != null) {
            jsonGenerator.writeStringField("type", typeSitu.getTypeSitu().name());
            jsonGenerator.writeStringField("caseTimestamp", typeSitu.getCaseTimestamp());
            jsonGenerator.writeStringField("computationTimestamp", typeSitu.getComputationTimestamp());
        } else {
            jsonGenerator.writeStringField("type", null);
            jsonGenerator.writeStringField("caseTimestamp", null);
            jsonGenerator.writeStringField("computationTimestamp", null);
        }

        // Sérialisation des seuils (TODO)
        jsonGenerator.writeObjectField("preContingencyResult", result.getPreContingencyResult());
        jsonGenerator.writeObjectField("postContingencyResults", result.getPostContingencyResults());
        jsonGenerator.writeEndObject();
    }

    public static void write(Network network, SecurityAnalysisResult result, Writer writer) throws IOException {
        // Objects.requireNonNull(caseFile);

        write(network, result, new LimitViolationFilter(), writer);
    }

    public static void write(Network network, SecurityAnalysisResult result, LimitViolationFilter filter, Writer writer) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);

        LOGGER.info("Serializing " + network.getName());
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(SecurityAnalysisResult.class, new ApogeeSecurityAnalysisResultSerializer(network));
        module.addSerializer(PostContingencyResult.class, new PostContingencyResultSerializer());
        module.addSerializer(LimitViolationsResult.class, new LimitViolationsResultSerializer());
        module.addSerializer(LimitViolation.class, new ApogeeLimitViolationSerializer(network));
        module.addSerializer(ContingencyElement.class, new ContingencyElementSerializer());
        objectMapper.registerModule(module);

        ObjectWriter ow = objectMapper.writerWithDefaultPrettyPrinter();
        ow.writeValue(writer, result);
    }
}
