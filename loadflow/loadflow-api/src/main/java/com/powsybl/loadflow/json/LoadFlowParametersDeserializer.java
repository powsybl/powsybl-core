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

    LoadFlowParametersDeserializer() {
        super(LoadFlowParameters.class);
    }

    protected static class ParsingContext {
        String version = null;
        Boolean useReactiveLimits = null;
        Boolean noGeneratorReactiveLimits = null;
        Boolean specificCompatibility = null;
        Boolean t2wtSplitShuntAdmittance = null;
        Boolean twtSplitShuntAdmittance = null;
        Boolean simulShunt = null;
        Boolean shuntCompensatorVoltageControlOn = null;
        Boolean readSlackBus = null;
        Boolean writeSlackBus = null;
        Boolean dc = null;
        Boolean distributedSlack = null;
        BalanceType balanceType = null;
        Boolean dcUseTransformerRatio = null;
        Set<Country> countriesToBalance = null;
        LoadFlowParameters.ComponentMode connectedComponentMode = null;
        LoadFlowParameters.ComponentMode componentMode = null;
        Boolean hvdcAcEmulation = null;
        Double dcPowerFactor = null;
    }

    @Override
    public LoadFlowParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new LoadFlowParameters());
    }

    @Override
    public LoadFlowParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, LoadFlowParameters parameters) throws IOException {
        ParsingContext parsingContext = new ParsingContext();
        List<Extension<LoadFlowParameters>> extensions = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version":
                    parser.nextToken();
                    parsingContext.version = parser.getValueAsString();
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
                    parser.nextToken();
                    parsingContext.useReactiveLimits = parser.readValueAs(Boolean.class);
                    break;

                case "noGeneratorReactiveLimits":
                    parser.nextToken();
                    parsingContext.noGeneratorReactiveLimits = parser.readValueAs(Boolean.class);
                    break;

                case "phaseShifterRegulationOn":
                    parser.nextToken();
                    parameters.setPhaseShifterRegulationOn(parser.readValueAs(Boolean.class));
                    break;

                case "specificCompatibility":
                    parser.nextToken();
                    parsingContext.specificCompatibility = parser.readValueAs(Boolean.class);
                    break;

                case "t2wtSplitShuntAdmittance":
                    parser.nextToken();
                    parsingContext.t2wtSplitShuntAdmittance = parser.readValueAs(Boolean.class);
                    break;

                case "twtSplitShuntAdmittance":
                    parser.nextToken();
                    parsingContext.twtSplitShuntAdmittance = parser.readValueAs(Boolean.class);
                    break;

                case "simulShunt":
                    parser.nextToken();
                    parsingContext.simulShunt = parser.readValueAs(Boolean.class);
                    break;

                case "shuntCompensatorVoltageControlOn":
                    parser.nextToken();
                    parsingContext.shuntCompensatorVoltageControlOn = parser.readValueAs(Boolean.class);
                    break;

                case "readSlackBus":
                    parser.nextToken();
                    parsingContext.readSlackBus = parser.readValueAs(Boolean.class);
                    break;

                case "writeSlackBus":
                    parser.nextToken();
                    parsingContext.writeSlackBus = parser.readValueAs(Boolean.class);
                    break;

                case "dc":
                    parser.nextToken();
                    parsingContext.dc = parser.readValueAs(Boolean.class);
                    break;

                case "distributedSlack":
                    parser.nextToken();
                    parsingContext.distributedSlack = parser.readValueAs(Boolean.class);
                    break;

                case "balanceType":
                    parser.nextToken();
                    parsingContext.balanceType = JsonUtil.readValue(deserializationContext, parser, BalanceType.class);
                    break;

                case "dcUseTransformerRatio":
                    parser.nextToken();
                    parsingContext.dcUseTransformerRatio = parser.readValueAs(Boolean.class);
                    break;

                case "countriesToBalance":
                    parser.nextToken();
                    Set<Country> countries = JsonUtil.readSet(deserializationContext, parser, Country.class);
                    parsingContext.countriesToBalance = countries;
                    break;

                case "connectedComponentMode":
                    parser.nextToken();
                    parsingContext.connectedComponentMode = JsonUtil.readValue(deserializationContext, parser, LoadFlowParameters.ComponentMode.class);
                    break;

                case "componentMode":
                    parser.nextToken();
                    parsingContext.componentMode = JsonUtil.readValue(deserializationContext, parser, LoadFlowParameters.ComponentMode.class);
                    break;

                case "hvdcAcEmulation":
                    parser.nextToken();
                    parsingContext.hvdcAcEmulation = parser.readValueAs(Boolean.class);
                    break;

                case "dcPowerFactor":
                    parser.nextToken();
                    parsingContext.dcPowerFactor = parser.readValueAs(Double.class);
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, getExtensionSerializers()::get, parameters);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        checkAndFillVersionedParameters(parameters, parsingContext);
        extensions.forEach(extension -> parameters.addExtension((Class) extension.getClass(), extension));
        return parameters;
    }

    private static void checkAndFillVersionedParameters(LoadFlowParameters parameters, ParsingContext parsingContext) {
        String version = parsingContext.version;
        if (parsingContext.useReactiveLimits != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: useReactiveLimits", version, "1.8");
            parameters.setUseReactiveLimits(parsingContext.useReactiveLimits);
        }
        if (parsingContext.noGeneratorReactiveLimits != null) {
            JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: noGeneratorReactiveLimits", version, "1.7");
            parameters.setUseReactiveLimits(!parsingContext.noGeneratorReactiveLimits);
        }
        if (parsingContext.specificCompatibility != null) {
            JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: specificCompatibility", version, "1.0");
            parameters.setTwtSplitShuntAdmittance(parsingContext.specificCompatibility);
        }
        if (parsingContext.t2wtSplitShuntAdmittance != null) {
            JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: t2wtSplitShuntAdmittance", version, "1.0");
            JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: t2wtSplitShuntAdmittance", version, "1.1");
            parameters.setTwtSplitShuntAdmittance(parsingContext.t2wtSplitShuntAdmittance);
        }
        if (parsingContext.twtSplitShuntAdmittance != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: twtSplitShuntAdmittance", version, "1.2");
            parameters.setTwtSplitShuntAdmittance(parsingContext.twtSplitShuntAdmittance);
        }
        if (parsingContext.simulShunt != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: simulShunt", version, "1.3");
            JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, "Tag: simulShunt", version, "1.6");
            parameters.setShuntCompensatorVoltageControlOn(parsingContext.simulShunt);
        }
        if (parsingContext.shuntCompensatorVoltageControlOn != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: shuntCompensatorVoltageControlOn", version, "1.6");
            parameters.setShuntCompensatorVoltageControlOn(parsingContext.shuntCompensatorVoltageControlOn);
        }
        if (parsingContext.readSlackBus != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: readSlackBus", version, "1.3");
            parameters.setReadSlackBus(parsingContext.readSlackBus);
        }
        if (parsingContext.writeSlackBus != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: writeSlackBus", version, "1.3");
            parameters.setWriteSlackBus(parsingContext.writeSlackBus);
        }
        if (parsingContext.dc != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: dc", version, "1.4");
            parameters.setDc(parsingContext.dc);
        }
        if (parsingContext.distributedSlack != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: distributedSlack", version, "1.4");
            parameters.setDistributedSlack(parsingContext.distributedSlack);
        }
        if (parsingContext.balanceType != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: balanceType", version, "1.4");
            parameters.setBalanceType(parsingContext.balanceType);
        }
        if (parsingContext.dcUseTransformerRatio != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: dcUseTransformerRatio", version, "1.5");
            parameters.setDcUseTransformerRatio(parsingContext.dcUseTransformerRatio);
        }
        if (parsingContext.countriesToBalance != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: countriesToBalance", version, "1.5");
            parameters.setCountriesToBalance(parsingContext.countriesToBalance);
        }
        if (parsingContext.connectedComponentMode != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: connectedComponentMode", version, "1.5");
            JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, "Tag: connectedComponentMode", version, "1.10");
            parameters.setComponentMode(parsingContext.connectedComponentMode);
        }
        if (parsingContext.componentMode != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: componentMode", version, "1.10");
            parameters.setComponentMode(parsingContext.componentMode);
        }
        if (parsingContext.hvdcAcEmulation != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: hvdcAcEmulation", version, "1.7");
            parameters.setHvdcAcEmulation(parsingContext.hvdcAcEmulation);
        }
        if (parsingContext.dcPowerFactor != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: dcPowerFactor", version, "1.9");
            parameters.setDcPowerFactor(parsingContext.dcPowerFactor);
        }
    }
}
