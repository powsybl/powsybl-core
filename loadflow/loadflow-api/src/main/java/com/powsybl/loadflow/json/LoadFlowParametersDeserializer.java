/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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

import static com.powsybl.loadflow.json.JsonLoadFlowParameters.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class LoadFlowParametersDeserializer extends StdDeserializer<LoadFlowParameters> {

    private static final String CONTEXT_NAME = "LoadFlowParameters";
    private static final String TAGS = "Tag: ";

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
                    parameters.setVoltageInitMode(JsonUtil.readValue(deserializationContext, parser, VoltageInitMode.class));
                    break;

                case "transformerVoltageControlOn":
                    parser.nextToken();
                    parameters.setTransformerVoltageControlOn(parser.readValueAs(Boolean.class));
                    break;

                case "useReactiveLimits":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: useReactiveLimits", version, "1.8");
                    parser.nextToken();
                    parameters.setUseReactiveLimits(parser.readValueAs(Boolean.class));
                    break;

                case "noGeneratorReactiveLimits":
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: noGeneratorReactiveLimits", version, "1.7");
                    parser.nextToken();
                    parameters.setUseReactiveLimits(!parser.readValueAs(Boolean.class));
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
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: twtSplitShuntAdmittance", version, "1.2");
                    parser.nextToken();
                    parameters.setTwtSplitShuntAdmittance(parser.readValueAs(Boolean.class));
                    break;

                case "simulShunt":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: simulShunt", version, "1.3");
                    JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, "Tag: simulShunt", version, "1.6");
                    parser.nextToken();
                    parameters.setShuntCompensatorVoltageControlOn(parser.readValueAs(Boolean.class));
                    break;

                case "shuntCompensatorVoltageControlOn":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: shuntCompensatorVoltageControlOn", version, "1.6");
                    parser.nextToken();
                    parameters.setShuntCompensatorVoltageControlOn(parser.readValueAs(Boolean.class));
                    break;

                case "readSlackBus":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: readSlackBus", version, "1.3");
                    parser.nextToken();
                    parameters.setReadSlackBus(parser.readValueAs(Boolean.class));
                    break;

                case "writeSlackBus":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: writeSlackBus", version, "1.3");
                    parser.nextToken();
                    parameters.setWriteSlackBus(parser.readValueAs(Boolean.class));
                    break;

                case "dc":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: dc", version, "1.4");
                    parser.nextToken();
                    parameters.setDc(parser.readValueAs(Boolean.class));
                    break;

                case "distributedSlack":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: distributedSlack", version, "1.4");
                    parser.nextToken();
                    parameters.setDistributedSlack(parser.readValueAs(Boolean.class));
                    break;

                case "balanceType":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAGS + parser.getCurrentName(), version, "1.4");
                    parser.nextToken();
                    parameters.setBalanceType(JsonUtil.readValue(deserializationContext, parser, BalanceType.class));
                    break;

                case "dcUseTransformerRatio":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAGS + parser.getCurrentName(), version, "1.5");
                    parser.nextToken();
                    parameters.setDcUseTransformerRatio(parser.readValueAs(Boolean.class));
                    break;

                case "countriesToBalance":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAGS + parser.getCurrentName(), version, "1.5");
                    parser.nextToken();
                    Set<Country> countries = JsonUtil.readSet(deserializationContext, parser, Country.class);
                    parameters.setCountriesToBalance(countries);
                    break;

                case "connectedComponentMode":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAGS + parser.getCurrentName(), version, "1.5");
                    parser.nextToken();
                    parameters.setConnectedComponentMode(JsonUtil.readValue(deserializationContext, parser, LoadFlowParameters.ConnectedComponentMode.class));
                    break;

                case "hvdcAcEmulation":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: hvdcAcEmulation" + parser.getCurrentName(), version, "1.7");
                    parser.nextToken();
                    parameters.setHvdcAcEmulation(parser.readValueAs(Boolean.class));
                    break;

                case "dcPowerFactor":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: dcPowerFactor" + parser.getCurrentName(), version, "1.9");
                    parser.nextToken();
                    parameters.setDcPowerFactor(parser.readValueAs(Double.class));
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, getExtensionSerializers()::get, parameters);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        extensions.forEach(extension -> parameters.addExtension((Class) extension.getClass(), extension));
        return parameters;
    }
}
