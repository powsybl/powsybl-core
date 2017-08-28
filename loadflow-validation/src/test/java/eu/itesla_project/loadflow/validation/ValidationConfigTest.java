/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import eu.itesla_project.commons.config.InMemoryPlatformConfig;
import eu.itesla_project.commons.config.MapModuleConfig;
import eu.itesla_project.commons.io.table.AsciiTableFormatterFactory;
import eu.itesla_project.commons.io.table.TableFormatterFactory;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.loadflow.api.mock.LoadFlowFactoryMock;
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
                    ValidationConfig.EPSILON_X_DEFAULT, ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationConfig.VALIDATION_OUTPUT_WRITER_DEFAULT);
    }

    @Test
    public void checkIncompleteConfig() throws Exception {
        float threshold = 0.1f;
        boolean verbose = true;
        float epsilonX = 0.1f;
        boolean applyReactanceCorrection = true;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("loadflow-validation");
        moduleConfig.setStringProperty("threshold", Float.toString(threshold));
        moduleConfig.setStringProperty("verbose", Boolean.toString(verbose));
        moduleConfig.setStringProperty("epsilon-x", Float.toString(epsilonX));
        moduleConfig.setStringProperty("apply-reactance-correction", Boolean.toString(applyReactanceCorrection));
        ValidationConfig config = ValidationConfig.load(platformConfig);
        checkValues(config, threshold, verbose, loadFlowFactory, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, epsilonX, applyReactanceCorrection,
                    ValidationConfig.VALIDATION_OUTPUT_WRITER_DEFAULT);
    }

    @Test
    public void checkCompleteConfig() throws Exception {
        float threshold = 0.1f;
        boolean verbose = true;
        Class<? extends TableFormatterFactory> tableFormatterFactory = AsciiTableFormatterFactory.class;
        float epsilonX = 0.1f;
        boolean applyReactanceCorrection = true;
        ValidationOutputWriter validationOutputWriter = ValidationOutputWriter.CSV;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("loadflow-validation");
        moduleConfig.setStringProperty("threshold", Float.toString(threshold));
        moduleConfig.setStringProperty("verbose", Boolean.toString(verbose));
        moduleConfig.setStringProperty("load-flow-factory", loadFlowFactory.getCanonicalName());
        moduleConfig.setStringProperty("table-formatter-factory", tableFormatterFactory.getCanonicalName());
        moduleConfig.setStringProperty("epsilon-x", Float.toString(epsilonX));
        moduleConfig.setStringProperty("apply-reactance-correction", Boolean.toString(applyReactanceCorrection));
        moduleConfig.setStringProperty("output-writer", validationOutputWriter.name());
        ValidationConfig config = ValidationConfig.load(platformConfig);
        checkValues(config, threshold, verbose, loadFlowFactory, tableFormatterFactory, epsilonX, applyReactanceCorrection, validationOutputWriter);
    }

    @Test
    public void checkSetters() throws Exception {
        float threshold = 0.1f;
        boolean verbose = true;
        Class<? extends TableFormatterFactory> tableFormatterFactory = AsciiTableFormatterFactory.class;
        float epsilonX = 0.1f;
        boolean applyReactanceCorrection = true;
        ValidationOutputWriter validationOutputWriter = ValidationOutputWriter.CSV;
        ValidationConfig config = ValidationConfig.load(platformConfig);
        config.setThreshold(threshold);
        config.setVerbose(verbose);
        config.setLoadFlowFactory(loadFlowFactory);
        config.setTableFormatterFactory(tableFormatterFactory);
        config.setEpsilonX(epsilonX);
        config.setApplyReactanceCorrection(applyReactanceCorrection);
        config.setValidationOutputWriter(validationOutputWriter);
        checkValues(config, threshold, verbose, loadFlowFactory, tableFormatterFactory, epsilonX, applyReactanceCorrection, validationOutputWriter);
    }

    private void checkValues(ValidationConfig config, float threshold, boolean verbose, Class<? extends LoadFlowFactory> loadFlowFactory,
                             Class<? extends TableFormatterFactory> tableFormatterFactory, float epsilonX, boolean applyReactanceCorrection,
                             ValidationOutputWriter validationOutputWriter) {
        assertEquals(threshold, config.getThreshold(), 0f);
        assertEquals(verbose, config.isVerbose());
        assertEquals(loadFlowFactory, config.getLoadFlowFactory());
        assertEquals(tableFormatterFactory, config.getTableFormatterFactory());
        assertEquals(epsilonX, config.getEpsilonX(), 0f);
        assertEquals(applyReactanceCorrection, config.applyReactanceCorrection());
        assertEquals(validationOutputWriter, config.getValidationOutputWriter());
    }

    @Test
    public void testWrongConfig() {
        try {
            new ValidationConfig(-1, false, loadFlowFactory, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, 1,
                                 ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters());
            fail();
        } catch (Exception ignored) {
        }
        try {
            new ValidationConfig(1, false, null, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, 1,
                                 ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters());
            fail();
        } catch (Exception ignored) {
        }
        try {
            new ValidationConfig(1, false, loadFlowFactory, null, ValidationConfig.EPSILON_X_DEFAULT,
                                ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters());
            fail();
        } catch (Exception ignored) {
        }
        try {
            new ValidationConfig(1, false, loadFlowFactory, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, -1,
                                 ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationOutputWriter.CSV_MULTILINE, new LoadFlowParameters());
            fail();
        } catch (Exception ignored) {
        }
        try {
            new ValidationConfig(1, false, loadFlowFactory, ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, 1,
                                 ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, null, new LoadFlowParameters());
            fail();
        } catch (Exception ignored) {
        }
    }

}
