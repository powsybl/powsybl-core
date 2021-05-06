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
import java.util.ArrayList;
import java.util.List;

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
                             boolean phaseShifterRegulationOn, boolean twtSplitShuntAdmittance,
                             boolean simulShunt, boolean readSlackBus, boolean writeSlackBus,
                             boolean dc, boolean distributedSlack, LoadFlowParameters.BalanceType balanceType,
                             boolean dcUseTransformerRatio, List<String> countriesToBalance,
                             LoadFlowParameters.ComputedConnectedComponentType computedConnectedComponent) {
        assertEquals(parameters.getVoltageInitMode(), voltageInitMode);
        assertEquals(parameters.isTransformerVoltageControlOn(), transformerVoltageControlOn);
        assertEquals(parameters.isPhaseShifterRegulationOn(), phaseShifterRegulationOn);
        assertEquals(parameters.isNoGeneratorReactiveLimits(), noGeneratorReactiveLimits);
        assertEquals(parameters.isTwtSplitShuntAdmittance(), twtSplitShuntAdmittance);
        assertEquals(parameters.isSimulShunt(), simulShunt);
        assertEquals(parameters.isReadSlackBus(), readSlackBus);
        assertEquals(parameters.isWriteSlackBus(), writeSlackBus);
        assertEquals(parameters.isDc(), dc);
        assertEquals(parameters.isDistributedSlack(), distributedSlack);
        assertEquals(parameters.getBalanceType(), balanceType);
        assertEquals(parameters.getDcUseTransformerRatio(), dcUseTransformerRatio);
        assertEquals(parameters.getCountriesToBalance(), countriesToBalance);
        assertEquals(parameters.getComputedConnectedComponent(), computedConnectedComponent);
    }

    @Test
    public void testNoConfig() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE,
                LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
                LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SIMUL_SHUNT,
                LoadFlowParameters.DEFAULT_READ_SLACK_BUS,
                LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC,
                LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK,
                LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT,
                LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_COMPUTED_CONNECTED_COMPONENT);
    }

    @Test
    public void checkConfig() {
        boolean transformerVoltageControlOn = true;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = true;
        boolean twtSplitShuntAdmittance = true;
        boolean simulShunt = true;
        boolean readSlackBus = true;
        boolean writeSlackBus = true;
        boolean voltageRemoteControl = true;
        boolean dc = true;
        boolean distributedSlack = true;
        LoadFlowParameters.BalanceType balanceType = LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES;
        boolean dcUseTransformerRatio = true;
        List<String> countriesToBalance = new ArrayList<>();
        LoadFlowParameters.ComputedConnectedComponentType computedConnectedComponent = LoadFlowParameters.ComputedConnectedComponentType.MAIN;

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("voltageInitMode", "UNIFORM_VALUES");
        moduleConfig.setStringProperty("transformerVoltageControlOn", Boolean.toString(transformerVoltageControlOn));
        moduleConfig.setStringProperty("noGeneratorReactiveLimits", Boolean.toString(noGeneratorReactiveLimits));
        moduleConfig.setStringProperty("phaseShifterRegulationOn", Boolean.toString(phaseShifterRegulationOn));
        moduleConfig.setStringProperty("twtSplitShuntAdmittance", Boolean.toString(twtSplitShuntAdmittance));
        moduleConfig.setStringProperty("simulShunt", Boolean.toString(simulShunt));
        moduleConfig.setStringProperty("readSlackBus", Boolean.toString(readSlackBus));
        moduleConfig.setStringProperty("writeSlackBus", Boolean.toString(writeSlackBus));
        moduleConfig.setStringProperty("voltageRemoteControl", Boolean.toString(voltageRemoteControl));
        moduleConfig.setStringProperty("dc", Boolean.toString(dc));
        moduleConfig.setStringProperty("distributedSlack", Boolean.toString(dc));
        moduleConfig.setStringProperty("balanceType", balanceType.name());
        moduleConfig.setStringProperty("dcUseTransformerRatio", Boolean.toString(dc));
        moduleConfig.setStringListProperty("countriesToBalance", countriesToBalance);
        moduleConfig.setStringProperty("computedConnectedComponent", computedConnectedComponent.name());

        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, voltageInitMode, transformerVoltageControlOn,
                    noGeneratorReactiveLimits, phaseShifterRegulationOn, twtSplitShuntAdmittance, simulShunt, readSlackBus, writeSlackBus,
                    dc, distributedSlack, balanceType, dcUseTransformerRatio, countriesToBalance, computedConnectedComponent);
    }

    @Test
    public void checkIncompleteConfig() {
        boolean transformerVoltageControlOn = true;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("transformerVoltageControlOn", Boolean.toString(transformerVoltageControlOn));
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE,
                transformerVoltageControlOn, LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SIMUL_SHUNT, LoadFlowParameters.DEFAULT_READ_SLACK_BUS, LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC, LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK, LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT, LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_COMPUTED_CONNECTED_COMPONENT);
    }

    @Test
    public void checkDefaultPlatformConfig() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters);
        checkValues(parameters, LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE,
                LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON, LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SIMUL_SHUNT, LoadFlowParameters.DEFAULT_READ_SLACK_BUS, LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC, LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK, LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT, LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_COMPUTED_CONNECTED_COMPONENT);
    }

    @Test
    public void checkConstructorByVoltageInitMode() {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        LoadFlowParameters parameters = new LoadFlowParameters(voltageInitMode);
        checkValues(parameters, voltageInitMode, LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SIMUL_SHUNT, LoadFlowParameters.DEFAULT_READ_SLACK_BUS, LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC, LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK, LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT, LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_COMPUTED_CONNECTED_COMPONENT);
    }

    @Test
    public void checkConstructorByVoltageInitModeAndTransformerVoltageControlOn() {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        boolean transformerVoltageControlOn = true;
        LoadFlowParameters parameters = new LoadFlowParameters(voltageInitMode, transformerVoltageControlOn);
        checkValues(parameters, voltageInitMode, true, LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
                LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SIMUL_SHUNT,
                LoadFlowParameters.DEFAULT_READ_SLACK_BUS,
                LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC,
                LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK,
                LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT,
                LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_COMPUTED_CONNECTED_COMPONENT);
    }

    @Test
    public void checkConstructorByLoadFlowParameters() {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        LoadFlowParameters parameters = new LoadFlowParameters(voltageInitMode);
        checkValues(parameters, voltageInitMode, LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
                LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SIMUL_SHUNT,
                LoadFlowParameters.DEFAULT_READ_SLACK_BUS,
                LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                LoadFlowParameters.DEFAULT_DC,
                LoadFlowParameters.DEFAULT_DISTRIBUTED_SLACK,
                LoadFlowParameters.DEFAULT_BALANCE_TYPE,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT,
                LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_COMPUTED_CONNECTED_COMPONENT);

        LoadFlowParameters parameters1 = new LoadFlowParameters(parameters);
        parameters1.setDc(true);
        parameters1.setDistributedSlack(false);
        parameters1.setBalanceType(LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD);
        checkValues(parameters1, voltageInitMode,
                LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
                LoadFlowParameters.DEFAULT_TWT_SPLIT_SHUNT_ADMITTANCE,
                LoadFlowParameters.DEFAULT_SIMUL_SHUNT,
                LoadFlowParameters.DEFAULT_READ_SLACK_BUS,
                LoadFlowParameters.DEFAULT_WRITE_SLACK_BUS,
                true,
                false,
                LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD,
                LoadFlowParameters.DEFAULT_DC_USE_TRANSFORMER_RATIO_DEFAULT,
                LoadFlowParameters.DEFAULT_COUNTRIES_TO_BALANCE,
                LoadFlowParameters.DEFAULT_COMPUTED_CONNECTED_COMPONENT);
    }

    @Test
    public void checkSetters() {
        boolean transformerVoltageControlOn = true;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = true;
        boolean twtSplitShuntAdmittance = true;
        boolean simulShunt = true;
        boolean readSlackBus = true;
        boolean writeSlackBus = true;
        boolean dc = true;
        boolean distributedSlack = false;
        LoadFlowParameters.BalanceType balanceType = LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        boolean dcUseTransformerRatio = true;
        List<String> countriesToBalance = new ArrayList<>();
        LoadFlowParameters.ComputedConnectedComponentType computedConnectedComponent = LoadFlowParameters.ComputedConnectedComponentType.MAIN;

        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        parameters.setNoGeneratorReactiveLimits(noGeneratorReactiveLimits)
                .setPhaseShifterRegulationOn(phaseShifterRegulationOn)
                .setTransformerVoltageControlOn(transformerVoltageControlOn)
                .setVoltageInitMode(voltageInitMode)
                .setTwtSplitShuntAdmittance(twtSplitShuntAdmittance)
                .setSimulShunt(simulShunt)
                .setReadSlackBus(readSlackBus)
                .setWriteSlackBus(writeSlackBus)
                .setDc(dc)
                .setDistributedSlack(distributedSlack)
                .setBalanceType(balanceType);

        checkValues(parameters, voltageInitMode, transformerVoltageControlOn, noGeneratorReactiveLimits,
                    phaseShifterRegulationOn, twtSplitShuntAdmittance, simulShunt, readSlackBus, writeSlackBus,
                    dc, distributedSlack, balanceType, dcUseTransformerRatio, countriesToBalance, computedConnectedComponent);
    }

    @Test
    public void checkClone() {
        boolean transformerVoltageControlOn = true;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = true;
        boolean twtSplitShuntAdmittance = true;
        boolean simulShunt = true;
        boolean readSlackBus = true;
        boolean writeSlackBus = true;
        boolean dc = true;
        boolean distributedSlack = false;
        LoadFlowParameters.BalanceType balanceType = LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES;
        boolean dcUseTransformerRatio = true;
        List<String> countriesToBalance = new ArrayList<>();
        LoadFlowParameters.ComputedConnectedComponentType computedConnectedComponent = LoadFlowParameters.ComputedConnectedComponentType.MAIN;
        LoadFlowParameters parameters = new LoadFlowParameters(voltageInitMode, transformerVoltageControlOn,
                                                               noGeneratorReactiveLimits, phaseShifterRegulationOn, twtSplitShuntAdmittance, simulShunt, readSlackBus, writeSlackBus,
                                                               dc, distributedSlack, balanceType, dcUseTransformerRatio, countriesToBalance, computedConnectedComponent);
        LoadFlowParameters parametersCloned = parameters.copy();
        checkValues(parametersCloned, parameters.getVoltageInitMode(), parameters.isTransformerVoltageControlOn(),
                parameters.isNoGeneratorReactiveLimits(), parameters.isPhaseShifterRegulationOn(), parameters.isTwtSplitShuntAdmittance(),
                parameters.isSimulShunt(), parameters.isReadSlackBus(), parameters.isWriteSlackBus(),
                parameters.isDc(), parameters.isDistributedSlack(), parameters.getBalanceType(), parameters.getDcUseTransformerRatio(),
                parameters.getCountriesToBalance(), parameters.getComputedConnectedComponent());
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
