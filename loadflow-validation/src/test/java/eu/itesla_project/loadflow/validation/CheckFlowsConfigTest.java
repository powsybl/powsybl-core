/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.file.FileSystem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import eu.itesla_project.commons.config.InMemoryPlatformConfig;
import eu.itesla_project.commons.config.MapModuleConfig;
import eu.itesla_project.commons.io.table.AsciiTableFormatterFactory;
import eu.itesla_project.commons.io.table.TableFormatterFactory;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.mock.LoadFlowFactoryMock;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class CheckFlowsConfigTest {

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
        CheckFlowsConfig config = CheckFlowsConfig.load(platformConfig);
        checkValues(config, CheckFlowsConfig.THRESHOLD_DEFAULT, CheckFlowsConfig.VERBOSE_DEFAULT, loadFlowFactory, CheckFlowsConfig.TABLE_FORMATTER_FACTORY_DEFAULT,
                    CheckFlowsConfig.EPSILON_X_DEFAULT, CheckFlowsConfig.APPLY_REACTANCE_CORRECTION_DEFAULT);
    }

    @Test
    public void checkIncompleteConfig() throws Exception {
        float threshold = 0.1f;
        boolean verbose = true;
        float epsilonX = 0.1f;
        boolean applyReactanceCorrection = true;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("check-flows");
        moduleConfig.setStringProperty("threshold", Float.toString(threshold));
        moduleConfig.setStringProperty("verbose", Boolean.toString(verbose));
        moduleConfig.setStringProperty("epsilon-x", Float.toString(epsilonX));
        moduleConfig.setStringProperty("apply-reactance-correction", Boolean.toString(applyReactanceCorrection));
        CheckFlowsConfig config = CheckFlowsConfig.load(platformConfig);
        checkValues(config, threshold, verbose, loadFlowFactory, CheckFlowsConfig.TABLE_FORMATTER_FACTORY_DEFAULT, epsilonX, applyReactanceCorrection);
    }

    @Test
    public void checkCompleteConfig() throws Exception {
        float threshold = 0.1f;
        boolean verbose = true;
        Class<? extends TableFormatterFactory> tableFormatterFactory = AsciiTableFormatterFactory.class;
        float epsilonX = 0.1f;
        boolean applyReactanceCorrection = true;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("check-flows");
        moduleConfig.setStringProperty("threshold", Float.toString(threshold));
        moduleConfig.setStringProperty("verbose", Boolean.toString(verbose));
        moduleConfig.setStringProperty("load-flow-factory", loadFlowFactory.getCanonicalName());
        moduleConfig.setStringProperty("table-formatter-factory", tableFormatterFactory.getCanonicalName());
        moduleConfig.setStringProperty("epsilon-x", Float.toString(epsilonX));
        moduleConfig.setStringProperty("apply-reactance-correction", Boolean.toString(applyReactanceCorrection));
        CheckFlowsConfig config = CheckFlowsConfig.load(platformConfig);
        checkValues(config, threshold, verbose, loadFlowFactory, tableFormatterFactory, epsilonX, applyReactanceCorrection);
    }
    
    @Test
    public void checkSetters() throws Exception {
        float threshold = 0.1f;
        boolean verbose = true;
        Class<? extends TableFormatterFactory> tableFormatterFactory = AsciiTableFormatterFactory.class;
        float epsilonX = 0.1f;
        boolean applyReactanceCorrection = true;
        CheckFlowsConfig config = CheckFlowsConfig.load(platformConfig);
        config.setThreshold(threshold);
        config.setVerbose(verbose);
        config.setLoadFlowFactory(loadFlowFactory);
        config.setTableFormatterFactory(tableFormatterFactory);
        config.setEpsilonX(epsilonX);
        config.setApplyReactanceCorrection(applyReactanceCorrection);
        checkValues(config, threshold, verbose, loadFlowFactory, tableFormatterFactory, epsilonX, applyReactanceCorrection);
    }

    private void checkValues(CheckFlowsConfig config, float threshold, boolean verbose, Class<? extends LoadFlowFactory> loadFlowFactory,
                             Class<? extends TableFormatterFactory> tableFormatterFactory, float epsilonX, boolean applyReactanceCorrection) {
        assertEquals(threshold, config.getThreshold(), 0f);
        assertEquals(verbose, config.isVerbose());
        assertEquals(loadFlowFactory, config.getLoadFlowFactory());
        assertEquals(tableFormatterFactory, config.getTableFormatterFactory());
        assertEquals(epsilonX, config.getEpsilonX(), 0f);
        assertEquals(applyReactanceCorrection, config.applyReactanceCorrection());
    }
    
    @Test
    public void testWrongConfig() {
        try {
            new CheckFlowsConfig(-1, false, loadFlowFactory, CheckFlowsConfig.TABLE_FORMATTER_FACTORY_DEFAULT, 1, 
                                 CheckFlowsConfig.APPLY_REACTANCE_CORRECTION_DEFAULT);
            fail();
        } catch(Exception ignored) {
        }
        try {
            new CheckFlowsConfig(1, false, null, CheckFlowsConfig.TABLE_FORMATTER_FACTORY_DEFAULT, 1, 
                                 CheckFlowsConfig.APPLY_REACTANCE_CORRECTION_DEFAULT);
            fail();
        } catch(Exception ignored) {
        }
        try {
            new CheckFlowsConfig(1, false, loadFlowFactory, null, CheckFlowsConfig.EPSILON_X_DEFAULT, 
                                CheckFlowsConfig.APPLY_REACTANCE_CORRECTION_DEFAULT);
            fail();
        } catch(Exception ignored) {
        }
        try {
            new CheckFlowsConfig(1, false, loadFlowFactory, CheckFlowsConfig.TABLE_FORMATTER_FACTORY_DEFAULT, -1, 
                                 CheckFlowsConfig.APPLY_REACTANCE_CORRECTION_DEFAULT);
            fail();
        } catch(Exception ignored) {
        }
    }

}
