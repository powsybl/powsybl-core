/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public class LoadFlowParametersTest {

    InMemoryPlatformConfig platformConfig;
    FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    private void checkValues(LoadFlowParameters parameters, LoadFlowParameters.VoltageInitMode voltageInitMode,
                             boolean transformerVoltageControlOn, boolean noGeneratorReactiveLimits,
                             boolean phaseShifterRegulationOn, boolean t2wtSplitShuntAdmittance) {
        assertEquals(parameters.getVoltageInitMode(), voltageInitMode);
        assertEquals(parameters.isTransformerVoltageControlOn(), transformerVoltageControlOn);
        assertEquals(parameters.isPhaseShifterRegulationOn(), phaseShifterRegulationOn);
        assertEquals(parameters.isNoGeneratorReactiveLimits(), noGeneratorReactiveLimits);
        assertEquals(parameters.isT2wtSplitShuntAdmittance(), t2wtSplitShuntAdmittance);
    }

    @Test
    public void testNoConfig() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE,
                LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
                LoadFlowParameters.DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE);
    }

    @Test
    public void checkConfig() throws Exception {
        boolean transformerVoltageControlOn = true;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = true;
        boolean t2wtSplitShuntAdmittance = true;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES;

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("voltageInitMode", "UNIFORM_VALUES");
        moduleConfig.setStringProperty("transformerVoltageControlOn", Boolean.toString(transformerVoltageControlOn));
        moduleConfig.setStringProperty("noGeneratorReactiveLimits", Boolean.toString(noGeneratorReactiveLimits));
        moduleConfig.setStringProperty("phaseShifterRegulationOn", Boolean.toString(phaseShifterRegulationOn));
        moduleConfig.setStringProperty("t2wtSplitShuntAdmittance", Boolean.toString(t2wtSplitShuntAdmittance));
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, voltageInitMode, transformerVoltageControlOn,
                noGeneratorReactiveLimits, phaseShifterRegulationOn, t2wtSplitShuntAdmittance);
    }

    @Test
    public void checkIncompleteConfig() throws Exception {
        boolean transformerVoltageControlOn = true;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("transformerVoltageControlOn", Boolean.toString(transformerVoltageControlOn));
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE,
                transformerVoltageControlOn, LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE);
    }

    @Test
    public void checkDefaultPlatformConfig() throws Exception {
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters);
        checkValues(parameters, LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE,
            LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE);
    }

    @Test
    public void checkConstructorByVoltageInitMode() throws Exception {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        LoadFlowParameters parameters = new LoadFlowParameters(voltageInitMode);
        checkValues(parameters, voltageInitMode, LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
            LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
            LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE);
    }

    @Test
    public void checkConstructorByVoltageInitModeAndTransformerVoltageControlOn() throws Exception {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        boolean transformerVoltageControlOn = true;
        LoadFlowParameters parameters = new LoadFlowParameters(voltageInitMode, transformerVoltageControlOn);
        checkValues(parameters, voltageInitMode, true, LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
            LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
            LoadFlowParameters.DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE);
    }

    @Test
    public void checkConstructorByLoadFlowParameters() throws Exception {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        LoadFlowParameters parameters = new LoadFlowParameters(voltageInitMode);
        checkValues(parameters, voltageInitMode, LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
            LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
            LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
            LoadFlowParameters.DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE);
        LoadFlowParameters parameters1 = new LoadFlowParameters(parameters);
        checkValues(parameters1, voltageInitMode, LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
            LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
            LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
            LoadFlowParameters.DEFAULT_T2WT_SPLIT_SHUNT_ADMITTANCE);
    }

    @Test
    public void checkSetters() throws Exception {
        boolean transformerVoltageControlOn = true;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = true;
        boolean t2wtSplitShuntAdmittance = true;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;

        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        parameters.setNoGeneratorReactiveLimits(noGeneratorReactiveLimits);
        parameters.setPhaseShifterRegulationOn(phaseShifterRegulationOn);
        parameters.setTransformerVoltageControlOn(transformerVoltageControlOn);
        parameters.setVoltageInitMode(voltageInitMode);
        parameters.setT2wtSplitShuntAdmittance(t2wtSplitShuntAdmittance);

        checkValues(parameters, voltageInitMode, transformerVoltageControlOn, noGeneratorReactiveLimits,
                phaseShifterRegulationOn, t2wtSplitShuntAdmittance);
    }

    @Test
    public void checkClone() throws Exception {
        boolean transformerVoltageControlOn = true;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = true;
        boolean t2wtSplitShuntAdmittance = true;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES;
        LoadFlowParameters parameters = new LoadFlowParameters(voltageInitMode, transformerVoltageControlOn,
                noGeneratorReactiveLimits, phaseShifterRegulationOn, t2wtSplitShuntAdmittance);
        LoadFlowParameters parametersCloned = parameters.copy();
        checkValues(parametersCloned, parameters.getVoltageInitMode(), parameters.isTransformerVoltageControlOn(),
                parameters.isNoGeneratorReactiveLimits(), parameters.isPhaseShifterRegulationOn(), parameters.isT2wtSplitShuntAdmittance());
    }

    @Test
    public void testExtensions() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertTrue(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertTrue(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    public void testCopyWithExtension() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        LoadFlowParameters copy = parameters.copy();
        assertEquals(1, copy.getExtensions().size());
        Extension<LoadFlowParameters> copiedExt = copy.getExtensionByName("dummyExtension");
        assertSame(parameters, dummyExtension.getExtendable());
        assertSame(copy, copiedExt.getExtendable());
    }

    @Test
    public void testNoExtensions() {
        LoadFlowParameters parameters = new LoadFlowParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertFalse(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertFalse(parameters.getExtension(DummyExtension.class) instanceof DummyExtension);
    }

    @Test
    public void testExtensionFromConfig() {
        LoadFlowParameters parameters = LoadFlowParameters.load(platformConfig);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensionByName("dummyExtension") instanceof DummyExtension);
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    private static class DummyExtension extends AbstractExtension<LoadFlowParameters> {

        @Override
        public String getName() {
            return "dummyExtension";
        }
    }

    @AutoService(LoadFlowParameters.ConfigLoader.class)
    public static class DummyLoader implements LoadFlowParameters.ConfigLoader<DummyExtension> {

        @Override
        public DummyExtension load(PlatformConfig platformConfig) {
            return new DummyExtension();
        }

        @Override
        public String getExtensionName() {
            return "dummyExtension";
        }

        @Override
        public String getCategoryName() {
            return "loadflow-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

    @AutoService(JsonLoadFlowParameters.ExtensionSerializer.class)
    public static class DummySerializer implements JsonLoadFlowParameters.ExtensionSerializer<DummyExtension> {

        private interface SerializationSpec {
            @JsonIgnore
            String getName();

            @JsonIgnore
            LoadFlowParameters getExtendable();
        }

        private static ObjectMapper createMapper() {
            return JsonUtil.createObjectMapper()
                    .addMixIn(JsonLoadFlowParametersTest.DummyExtension.class, JsonLoadFlowParametersTest.DummySerializer.SerializationSpec.class);
        }

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }

        @Override
        public DummyExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return new DummyExtension();
        }

        @Override
        public DummyExtension deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext, DummyExtension parameters) throws IOException {
            ObjectMapper objectMapper = createMapper();
            ObjectReader objectReader = objectMapper.readerForUpdating(parameters);
            DummyExtension updatedParameters = objectReader.readValue(jsonParser, DummyExtension.class);
            return updatedParameters;
        }

        @Override
        public String getExtensionName() {
            return "dummyExtension";
        }

        @Override
        public String getCategoryName() {
            return "loadflow-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }
}
