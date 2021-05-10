/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Country;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowParameters.VoltageInitMode;
import com.powsybl.loadflow.LoadFlowParameters.BalanceType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class LoadFlowParametersDeserializer extends StdDeserializer<LoadFlowParameters> {

    private static final String CONTEXT_NAME = "LoadFlowParameters";

    LoadFlowParametersDeserializer() {
        super(LoadFlowParameters.class);
    }

    @Override
    public LoadFlowParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new LoadFlowParameters());
    }

    @Override
    public LoadFlowParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, LoadFlowParameters parameters) throws IOException {
        String version = null;
        List<Extension<LoadFlowParameters>> extensions = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;

                case "voltageInitMode":
                    parser.nextToken();
                    parameters.setVoltageInitMode(parser.readValueAs(VoltageInitMode.class));
                    break;

                case "transformerVoltageControlOn":
                    parser.nextToken();
                    parameters.setTransformerVoltageControlOn(parser.readValueAs(Boolean.class));
                    break;

                case "noGeneratorReactiveLimits":
                    parser.nextToken();
                    parameters.setNoGeneratorReactiveLimits(parser.readValueAs(Boolean.class));
                    break;

                case "phaseShifterRegulationOn":
                    parser.nextToken();
                    parameters.setPhaseShifterRegulationOn(parser.readValueAs(Boolean.class));
                    break;

                case "specificCompatibility":
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: specificCompatibility", version, "1.0");
                    parser.nextToken();
                    parameters.setTwtSplitShuntAdmittance(parser.readValueAs(Boolean.class));
                    break;

                case "t2wtSplitShuntAdmittance":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: t2wtSplitShuntAdmittance", version, "1.0");
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: t2wtSplitShuntAdmittance", version, "1.1");
                    parser.nextToken();
                    parameters.setTwtSplitShuntAdmittance(parser.readValueAs(Boolean.class));
                    break;

                case "twtSplitShuntAdmittance":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: twtSplitShuntAdmittance", version, "1.1");
                    parser.nextToken();
                    parameters.setTwtSplitShuntAdmittance(parser.readValueAs(Boolean.class));
                    break;

                case "simulShunt":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: simulShunt", version, "1.2");
                    parser.nextToken();
                    parameters.setSimulShunt(parser.readValueAs(Boolean.class));
                    break;

                case "readSlackBus":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: readSlackBus", version, "1.2");
                    parser.nextToken();
                    parameters.setReadSlackBus(parser.readValueAs(Boolean.class));
                    break;

                case "writeSlackBus":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: writeSlackBus", version, "1.2");
                    parser.nextToken();
                    parameters.setWriteSlackBus(parser.readValueAs(Boolean.class));
                    break;

                case "dc":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: dc", version, "1.3");
                    parser.nextToken();
                    parameters.setDc(parser.readValueAs(Boolean.class));
                    break;

                case "distributedSlack":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: distributedSlack", version, "1.3");
                    parser.nextToken();
                    parameters.setDistributedSlack(parser.readValueAs(Boolean.class));
                    break;

                case "balanceType":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: distributedSlack", version, "1.3");
                    parser.nextToken();
                    parameters.setBalanceType(parser.readValueAs(BalanceType.class));
                    break;

                case "dcUseTransformerRatio":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: countriesToBalance", version, "1.4");
                    parser.nextToken();
                    parameters.setDcUseTransformerRatio(parser.readValueAs(Boolean.class));
                    break;

                case "countriesToBalance":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: countriesToBalance", version, "1.4");
                    parser.nextToken();
                    Set<Country> countries = parser.readValueAs(new TypeReference<Set<Country>>() {});
                    parameters.setCountriesToBalance(countries);
                    break;

                case "computedConnectedComponent":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: countriesToBalance", version, "1.4");
                    parser.nextToken();
                    parameters.setComputedConnectedComponent(parser.readValueAs(LoadFlowParameters.ComputedConnectedComponentType.class));
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, JsonLoadFlowParameters.getExtensionSerializers(), parameters);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        JsonLoadFlowParameters.getExtensionSerializers().addExtensions(parameters, extensions);
        return parameters;
    }
}
