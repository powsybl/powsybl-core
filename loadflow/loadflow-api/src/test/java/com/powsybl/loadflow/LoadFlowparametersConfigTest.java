/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.extensions.AbstractExtension;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class LoadFlowparametersConfigTest {

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
                             boolean phaseShifterRegulationOn, boolean specificCompatibility) {
        assertEquals(parameters.getVoltageInitMode(), voltageInitMode);
        assertEquals(parameters.isTransformerVoltageControlOn(), transformerVoltageControlOn);
        assertEquals(parameters.isPhaseShifterRegulationOn(), phaseShifterRegulationOn);
        assertEquals(parameters.isNoGeneratorReactiveLimits(), noGeneratorReactiveLimits);
        assertEquals(parameters.isSpecificCompatibility(), specificCompatibility);
    }

    @Test
    public void testNoConfig() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, LoadFlowParameters.DEFAULT_VOLTAGE_INIT_MODE,
                LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON,
                LoadFlowParameters.DEFAULT_SPECIFIC_COMPATIBILITY);
    }

    @Test
    public void testExtensions() {
        LoadFlowParameters parameters = new LoadFlowParameters();
        ExtensionLoadFlowParameters extensionLoadFlowParameters = new ExtensionLoadFlowParameters();
        parameters.addExtension(ExtensionLoadFlowParameters.class, extensionLoadFlowParameters);

        assertEquals(parameters.getExtensions().size(), 1);
        assertEquals(parameters.getExtensions().contains(extensionLoadFlowParameters), true);
        assertEquals(parameters.getExtensionByName("extensionLoadFlowParameters") instanceof ExtensionLoadFlowParameters, true);
        assertEquals(parameters.getExtension(ExtensionLoadFlowParameters.class) instanceof ExtensionLoadFlowParameters, true);
    }

    @Test
    public void testNoExtensions() {
        LoadFlowParameters parameters = LoadFlowParameters.load();

        assertEquals(parameters.getExtensions().size(), 0);
        assertEquals(parameters.getExtensions().contains(new ExtensionLoadFlowParameters()), false);
        assertEquals(parameters.getExtensionByName("extensionLoadFlowParameters") instanceof ExtensionLoadFlowParameters, false);
        assertEquals(parameters.getExtension(ExtensionLoadFlowParameters.class) instanceof ExtensionLoadFlowParameters, false);
    }


    @Test
    public void checkConfig() throws Exception {
        boolean transformerVoltageControlOn = true;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = true;
        boolean specificCompatibility = true;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES;

        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("load-flow-default-parameters");
        moduleConfig.setStringProperty("voltageInitMode", "UNIFORM_VALUES");
        moduleConfig.setStringProperty("transformerVoltageControlOn", Boolean.toString(transformerVoltageControlOn));
        moduleConfig.setStringProperty("noGeneratorReactiveLimits", Boolean.toString(noGeneratorReactiveLimits));
        moduleConfig.setStringProperty("phaseShifterRegulationOn", Boolean.toString(phaseShifterRegulationOn));
        moduleConfig.setStringProperty("specificCompatibility", Boolean.toString(specificCompatibility));
        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        checkValues(parameters, voltageInitMode, transformerVoltageControlOn,
                noGeneratorReactiveLimits, phaseShifterRegulationOn, specificCompatibility);
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
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_SPECIFIC_COMPATIBILITY);
    }

    @Test
    public void checkConstructorByVoltageInitMode() throws Exception {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        LoadFlowParameters parameters = new LoadFlowParameters(voltageInitMode);
        checkValues(parameters, voltageInitMode, LoadFlowParameters.DEFAULT_TRANSFORMER_VOLTAGE_CONTROL_ON,
                LoadFlowParameters.DEFAULT_NO_GENERATOR_REACTIVE_LIMITS,
                LoadFlowParameters.DEFAULT_PHASE_SHIFTER_REGULATION_ON, LoadFlowParameters.DEFAULT_SPECIFIC_COMPATIBILITY);
    }


    @Test
    public void checkSetters() throws Exception {
        boolean transformerVoltageControlOn = true;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = true;
        boolean specificCompatibility = true;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;

        LoadFlowParameters parameters = new LoadFlowParameters();
        LoadFlowParameters.load(parameters, platformConfig);
        parameters.setNoGeneratorReactiveLimits(noGeneratorReactiveLimits);
        parameters.setPhaseShifterRegulationOn(phaseShifterRegulationOn);
        parameters.setTransformerVoltageControlOn(transformerVoltageControlOn);
        parameters.setVoltageInitMode(voltageInitMode);
        parameters.setSpecificCompatibility(specificCompatibility);

        checkValues(parameters, voltageInitMode, transformerVoltageControlOn, noGeneratorReactiveLimits,
                phaseShifterRegulationOn, specificCompatibility);
    }

    @Test
    public void checkClone() throws Exception {
        boolean transformerVoltageControlOn = true;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = true;
        boolean specificCompatibility = true;
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES;
        LoadFlowParameters parameters = new LoadFlowParameters(voltageInitMode, transformerVoltageControlOn,
                noGeneratorReactiveLimits, phaseShifterRegulationOn, specificCompatibility);
        LoadFlowParameters parametersCloned = parameters.copy();
        checkValues(parametersCloned, parameters.getVoltageInitMode(), parameters.isTransformerVoltageControlOn(),
                parameters.isNoGeneratorReactiveLimits(), parameters.isPhaseShifterRegulationOn(), parameters.isSpecificCompatibility());
    }

    @Test
    public void checkWithFile() throws IOException, URISyntaxException {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        boolean transformerVoltageControlOn = false;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = false;
        boolean specificCompatibility = true;

        Path testConfigFile = Paths.get(this.getClass().getResource("/testConfig.xml").toURI());

        LoadFlowParameters loadFlowParameters = LoadFlowParameters.load(testConfigFile);

        checkValues(loadFlowParameters, voltageInitMode, transformerVoltageControlOn, noGeneratorReactiveLimits,
                phaseShifterRegulationOn, specificCompatibility);
    }

    @Test
    public void checkWithInputStream() throws IOException, URISyntaxException {
        LoadFlowParameters.VoltageInitMode voltageInitMode = LoadFlowParameters.VoltageInitMode.DC_VALUES;
        boolean transformerVoltageControlOn = false;
        boolean noGeneratorReactiveLimits = true;
        boolean phaseShifterRegulationOn = false;
        boolean specificCompatibility = true;

        Path testConfigFile = Paths.get(this.getClass().getResource("/testConfig.xml").toURI());

        LoadFlowParameters loadFlowParameters = LoadFlowParameters.load(Files.newInputStream(testConfigFile));

        checkValues(loadFlowParameters, voltageInitMode, transformerVoltageControlOn, noGeneratorReactiveLimits,
                phaseShifterRegulationOn, specificCompatibility);
    }

    class ExtensionLoadFlowParameters extends AbstractExtension<LoadFlowParameters> {

        @Override
        public String getName() {
            return "extensionLoadFlowParameters";
        }

        public ExtensionLoadFlowParameters() {
        }

        @Override
        public String toString() {
            return "";
        }
    }
}
