/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.mock.LoadFlowFactoryMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class ValidationConfigTest {

    InMemoryPlatformConfig platformConfig;
    FileSystem fileSystem;
    Class<? extends LoadFlowFactory> loadFlowFactory = LoadFlowFactoryMock.class;
    double threshold = 0.1;
    boolean verbose = true;
    Class<? extends TableFormatterFactory> tableFormatterFactory = AsciiTableFormatterFactory.class;
    double epsilonX = 0.1;
    boolean applyReactanceCorrection = true;
    ValidationOutputWriter validationOutputWriter = ValidationOutputWriter.CSV;
    boolean okMissingValues = true;
    boolean noRequirementIfReactiveBoundInversion = true;
    boolean compareResults = true;
    boolean checkMainComponentOnly = false;
    boolean noRequirementIfSetpointOutsidePowerBounds = true;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig defaultConfig = platformConfig.createModuleConfig("componentDefaultConfig");
        defaultConfig.setStringProperty("LoadFlowFactory", loadFlowFactory.getCanonicalName());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void testNoConfig() {
        ValidationConfig config = ValidationConfig.load(platformConfig);
        checkValues(config, ValidationConfig.THRESHOLD_DEFAULT, ValidationConfig.VERBOSE_DEFAULT, loadFlowFactory, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                    ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationConfig.VALIDATION_OUTPUT_WRITER_DEFAULT,
                    ValidationConfig.OK_MISSING_VALUES_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT, ValidationConfig.COMPARE_RESULTS_DEFAULT,
                    ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
    }

    @Test
    public void checkIncompleteConfig() throws Exception {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("loadflow-validation");
        moduleConfig.setStringProperty("threshold", Double.toString(threshold));
        moduleConfig.setStringProperty("verbose", Boolean.toString(verbose));
        moduleConfig.setStringProperty("epsilon-x", Double.toString(epsilonX));
        moduleConfig.setStringProperty("apply-reactance-correction", Boolean.toString(applyReactanceCorrection));
        ValidationConfig config = ValidationConfig.load(platformConfig);
        checkValues(config, threshold, verbose, loadFlowFactory, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, epsilonX, applyReactanceCorrection,
                    ValidationConfig.VALIDATION_OUTPUT_WRITER_DEFAULT, ValidationConfig.OK_MISSING_VALUES_DEFAULT,
                    ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT, ValidationConfig.COMPARE_RESULTS_DEFAULT,
                    ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
    }

    @Test
    public void checkCompleteConfig() throws Exception {
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("loadflow-validation");
        moduleConfig.setStringProperty("threshold", Double.toString(threshold));
        moduleConfig.setStringProperty("verbose", Boolean.toString(verbose));
        moduleConfig.setStringProperty("load-flow-factory", loadFlowFactory.getCanonicalName());
        moduleConfig.setStringProperty("table-formatter-factory", tableFormatterFactory.getCanonicalName());
        moduleConfig.setStringProperty("epsilon-x", Double.toString(epsilonX));
        moduleConfig.setStringProperty("apply-reactance-correction", Boolean.toString(applyReactanceCorrection));
        moduleConfig.setStringProperty("output-writer", validationOutputWriter.name());
        moduleConfig.setStringProperty("ok-missing-values", Boolean.toString(okMissingValues));
        moduleConfig.setStringProperty("no-requirement-if-reactive-bound-inversion", Boolean.toString(noRequirementIfReactiveBoundInversion));
        moduleConfig.setStringProperty("compare-results", Boolean.toString(compareResults));
        moduleConfig.setStringProperty("check-main-component-only", Boolean.toString(checkMainComponentOnly));
        moduleConfig.setStringProperty("no-requirement-if-setpoint-outside-power-bounds", Boolean.toString(noRequirementIfSetpointOutsidePowerBounds));
        ValidationConfig config = ValidationConfig.load(platformConfig);
        checkValues(config, threshold, verbose, loadFlowFactory, tableFormatterFactory, epsilonX, applyReactanceCorrection, validationOutputWriter, okMissingValues,
                    noRequirementIfReactiveBoundInversion, compareResults, checkMainComponentOnly, noRequirementIfSetpointOutsidePowerBounds);
    }

    @Test
    public void checkSetters() throws Exception {
        ValidationConfig config = ValidationConfig.load(platformConfig);
        config.setThreshold(threshold);
        config.setVerbose(verbose);
        config.setLoadFlowFactory(loadFlowFactory);
        config.setTableFormatterFactory(tableFormatterFactory);
        config.setEpsilonX(epsilonX);
        config.setApplyReactanceCorrection(applyReactanceCorrection);
        config.setValidationOutputWriter(validationOutputWriter);
        config.setOkMissingValues(okMissingValues);
        config.setNoRequirementIfReactiveBoundInversion(noRequirementIfReactiveBoundInversion);
        config.setCompareResults(compareResults);
        config.setCheckMainComponentOnly(checkMainComponentOnly);
        config.setNoRequirementIfSetpointOutsidePowerBounds(noRequirementIfSetpointOutsidePowerBounds);
        checkValues(config, threshold, verbose, loadFlowFactory, tableFormatterFactory, epsilonX, applyReactanceCorrection, validationOutputWriter, okMissingValues,
                    noRequirementIfReactiveBoundInversion, compareResults, checkMainComponentOnly, noRequirementIfSetpointOutsidePowerBounds);
    }

    private void checkValues(ValidationConfig config, double threshold, boolean verbose, Class<? extends LoadFlowFactory> loadFlowFactory,
                             Class<? extends TableFormatterFactory> tableFormatterFactory, double epsilonX, boolean applyReactanceCorrection,
                             ValidationOutputWriter validationOutputWriter, boolean okMissingValues, boolean noRequirementIfReactiveBoundInversion,
                             boolean compareResults, boolean checkMainComponentOnly, boolean noRequirementIfSetpointOutsidePowerBounds) {
        assertEquals(threshold, config.getThreshold(), 0.0);
        assertEquals(verbose, config.isVerbose());
        assertEquals(loadFlowFactory, config.getLoadFlowFactory());
        assertEquals(tableFormatterFactory, config.getTableFormatterFactory());
        assertEquals(epsilonX, config.getEpsilonX(), 0.0);
        assertEquals(applyReactanceCorrection, config.applyReactanceCorrection());
        assertEquals(validationOutputWriter, config.getValidationOutputWriter());
        assertEquals(okMissingValues, config.areOkMissingValues());
        assertEquals(noRequirementIfReactiveBoundInversion, config.isNoRequirementIfReactiveBoundInversion());
        assertEquals(compareResults, config.isCompareResults());
        assertEquals(checkMainComponentOnly, config.isCheckMainComponentOnly());
        assertEquals(noRequirementIfSetpointOutsidePowerBounds, config.isNoRequirementIfSetpointOutsidePowerBounds());
    }

    @Test
    public void testWrongConfig() {
        try {
            new ValidationConfig(-1, false, loadFlowFactory, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, 1,
                                 ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters(),
                                 ValidationConfig.OK_MISSING_VALUES_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT,
                                 ValidationConfig.COMPARE_RESULTS_DEFAULT, ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT,
                                 ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
            fail();
        } catch (Exception ignored) {
        }
        try {
            new ValidationConfig(1, false, null, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, 1,
                                 ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters(),
                                 ValidationConfig.OK_MISSING_VALUES_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT,
                                 ValidationConfig.COMPARE_RESULTS_DEFAULT, ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT,
                                 ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
            fail();
        } catch (Exception ignored) {
        }
        try {
            new ValidationConfig(1, false, loadFlowFactory, null, ValidationConfig.EPSILON_X_DEFAULT,
                                ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters(),
                                ValidationConfig.OK_MISSING_VALUES_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT,
                                ValidationConfig.COMPARE_RESULTS_DEFAULT, ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT,
                                ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
            fail();
        } catch (Exception ignored) {
        }
        try {
            new ValidationConfig(1, false, loadFlowFactory, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, -1,
                                 ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters(),
                                 ValidationConfig.OK_MISSING_VALUES_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT,
                                 ValidationConfig.COMPARE_RESULTS_DEFAULT, ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT,
                                 ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
            fail();
        } catch (Exception ignored) {
        }
        try {
            new ValidationConfig(1, false, loadFlowFactory, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, 1,
                                 ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, null, new LoadFlowParameters(),
                                 ValidationConfig.OK_MISSING_VALUES_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT,
                                 ValidationConfig.COMPARE_RESULTS_DEFAULT, ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT,
                                 ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
            fail();
        } catch (Exception ignored) {
        }
    }

}
