/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.SensitivityFunction;
import com.powsybl.sensitivity.SensitivityVariable;
import com.powsybl.sensitivity.factors.BranchFlowPerInjectionIncrease;
import com.powsybl.sensitivity.factors.BranchFlowPerLinearGlsk;
import com.powsybl.sensitivity.factors.BranchFlowPerPSTAngle;
import com.powsybl.sensitivity.factors.BranchIntensityPerPSTAngle;
import com.powsybl.sensitivity.factors.functions.BranchFlow;
import com.powsybl.sensitivity.factors.functions.BranchIntensity;
import com.powsybl.sensitivity.factors.variables.InjectionIncrease;
import com.powsybl.sensitivity.factors.variables.LinearGlsk;
import com.powsybl.sensitivity.factors.variables.PhaseTapChangerAngle;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

/**
 * JSON Serialization utility class for sensitivity factors input
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class SensitivityFactorsJsonSerializer {

    /**
     * Read sensitivity factors input in JSON format from reader
     * @param reader The reader to read from
     * @return The sensitivity factors input created
     * @throws IOException
     */
    public static Map<String, List<SensitivityFactor>> read(Reader reader) throws IOException {
        Objects.requireNonNull(reader);
        ObjectMapper mapper = new ObjectMapper();

        JsonParser jsonParser = mapper.getFactory().createParser(reader);
        JsonToken jsonToken;
        jsonToken = jsonParser.nextToken();
        jsonToken = jsonParser.nextToken();

        Map<String, SensitivityFunction> sensitivityFunctionMap = mapper
            .readValue(jsonParser, new TypeReference<LinkedHashMap<String, SensitivityFunction>>() { });

        jsonToken = jsonParser.nextToken();
        Map<String, SensitivityVariable> sensitivityVariableMap = mapper
            .readValue(jsonParser, new TypeReference<LinkedHashMap<String, SensitivityVariable>>() { });

        jsonToken = jsonParser.nextToken();
        Map<String, Set<String>> sensitivityFunctionStringMap = mapper
            .readValue(jsonParser, new TypeReference<LinkedHashMap<String, Set<String>>>() { });
        jsonParser.close();

        Map<String, List<SensitivityFactor>> sensitivityFactorsMap = new HashMap<>();
        for (String contingencyId : sensitivityFunctionStringMap.keySet()) {
            List<SensitivityFactor> sensitivityFactors = new ArrayList<>();
            for (String sensitivityFunctionString : sensitivityFunctionStringMap.get(contingencyId)) {
                SensitivityFunction function = sensitivityFunctionMap.get(sensitivityFunctionString);
                for (SensitivityVariable variable : sensitivityVariableMap.values()) {
                    sensitivityFactors.add(makeSensitivityFactor(function, variable));
                }
            }
            sensitivityFactorsMap.put(contingencyId, sensitivityFactors);
        }
        return sensitivityFactorsMap;
    }

    private static SensitivityFactor makeSensitivityFactor(SensitivityFunction function, SensitivityVariable variable) {
        if (function instanceof BranchFlow) {
            if (variable instanceof LinearGlsk) {
                return new BranchFlowPerLinearGlsk((BranchFlow) function, (LinearGlsk) variable);
            } else if (variable instanceof PhaseTapChangerAngle) {
                return new BranchFlowPerPSTAngle((BranchFlow) function, (PhaseTapChangerAngle) variable);
            } else if (variable instanceof InjectionIncrease) {
                return new BranchFlowPerInjectionIncrease((BranchFlow) function, (InjectionIncrease) variable);
            }
        } else if (function instanceof BranchIntensity) {
            if (variable instanceof PhaseTapChangerAngle) {
                return new BranchIntensityPerPSTAngle((BranchIntensity) function, (PhaseTapChangerAngle) variable);
            }
        }
        throw new PowsyblException("Unable to parse JsonSensitivityFactorProvider: unrecognizable sensitivity factor");
    }

    /**
     * Write sensitivity factors input in JSON format to writer
     * @param sensitivityFactorsMap The sensitivity factors input to export
     * @param writer The writer to write to
     * @throws IOException
     */
    public static void write(Map<String, List<SensitivityFactor>> sensitivityFactorsMap, Writer writer) throws IOException {
        Objects.requireNonNull(sensitivityFactorsMap);
        Objects.requireNonNull(writer);
        ObjectMapper mapper = JsonUtil.createObjectMapper();
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        JsonGenerator jsonGenerator = mapper.getFactory().createGenerator(writer);

        Map<String, SensitivityVariable> sensitivityVariableMap = new LinkedHashMap<>();
        Map<String, SensitivityFunction> sensitivityFunctionMap = new LinkedHashMap<>();
        Map<String, Set<String>> sensitivityFunctionStringMap = new LinkedHashMap<>();
        for (String contingencyId : sensitivityFactorsMap.keySet()) {
            Set<String> sensitivityFunctionsString = new LinkedHashSet<>();
            for (SensitivityFactor sensitivityFactor : sensitivityFactorsMap.get(contingencyId)) {
                sensitivityFunctionsString.add(sensitivityFactor.getFunction().getId());
                sensitivityVariableMap.put(sensitivityFactor.getVariable().getId(), sensitivityFactor.getVariable());
                sensitivityFunctionMap.put(sensitivityFactor.getFunction().getId(), sensitivityFactor.getFunction());
            }
            sensitivityFunctionStringMap.put(contingencyId, sensitivityFunctionsString);
        }

        jsonGenerator.writeStartArray();
        objectWriter.forType(new TypeReference<Map<String, SensitivityFunction>>() { }).writeValue(jsonGenerator, sensitivityFunctionMap);
        objectWriter.forType(new TypeReference<Map<String, SensitivityVariable>>() { }).writeValue(jsonGenerator, sensitivityVariableMap);
        objectWriter.forType(new TypeReference<Map<String, Set<String>>>() { }).writeValue(jsonGenerator, sensitivityFunctionStringMap);
        jsonGenerator.writeEndArray();
        jsonGenerator.close();
    }

    private SensitivityFactorsJsonSerializer() {
    }
}
