/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import java.util.Objects;

import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.commons.io.table.CsvTableFormatterFactory;
import eu.itesla_project.commons.io.table.TableFormatterFactory;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowParameters;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class ValidationConfig {

    public static final float THRESHOLD_DEFAULT = 0.0f;
    public static final boolean VERBOSE_DEFAULT = false;
    public static final Class<? extends TableFormatterFactory> TABLE_FORMATTER_FACTORY_DEFAULT = CsvTableFormatterFactory.class;
    public static final float EPSILON_X_DEFAULT = 0.1f;
    public static final boolean APPLY_REACTANCE_CORRECTION_DEFAULT = false;
    public static final ValidationOutputWriter VALIDATION_OUTPUT_WRITER_DEFAULT = ValidationOutputWriter.CSV_MULTILINE;

    private float threshold;
    private boolean verbose;
    private Class<? extends LoadFlowFactory> loadFlowFactory;
    private Class<? extends TableFormatterFactory> tableFormatterFactory;
    private float epsilonX;
    private boolean applyReactanceCorrection;
    private ValidationOutputWriter validationOutputWriter;
    private LoadFlowParameters loadFlowParameters;
    public static ValidationConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ValidationConfig load(PlatformConfig platformConfig) {
        float threshold = THRESHOLD_DEFAULT;
        boolean verbose = VERBOSE_DEFAULT;
        ComponentDefaultConfig componentDefaultConfig = ComponentDefaultConfig.load(platformConfig);
        Class<? extends LoadFlowFactory> loadFlowFactory = componentDefaultConfig.findFactoryImplClass(LoadFlowFactory.class);
        Class<? extends TableFormatterFactory> tableFormatterFactory = TABLE_FORMATTER_FACTORY_DEFAULT;
        float epsilonX = EPSILON_X_DEFAULT;
        boolean applyReactanceCorrection = APPLY_REACTANCE_CORRECTION_DEFAULT;
        ValidationOutputWriter validationOutputWriter = VALIDATION_OUTPUT_WRITER_DEFAULT;
        LoadFlowParameters loadFlowParameter = LoadFlowParameters.load();
        if (platformConfig.moduleExists("loadflow-validation")) {
            ModuleConfig config = platformConfig.getModuleConfig("loadflow-validation");
            threshold = config.getFloatProperty("threshold", THRESHOLD_DEFAULT);
            verbose = config.getBooleanProperty("verbose", VERBOSE_DEFAULT);
            if (config.hasProperty("load-flow-factory")) {
                loadFlowFactory = config.getClassProperty("load-flow-factory", LoadFlowFactory.class, componentDefaultConfig.findFactoryImplClass(LoadFlowFactory.class));
            }
            tableFormatterFactory = config.getClassProperty("table-formatter-factory", TableFormatterFactory.class, TABLE_FORMATTER_FACTORY_DEFAULT);
            epsilonX = config.getFloatProperty("epsilon-x", EPSILON_X_DEFAULT);
            applyReactanceCorrection = config.getBooleanProperty("apply-reactance-correction", APPLY_REACTANCE_CORRECTION_DEFAULT);
            validationOutputWriter = config.getEnumProperty("output-writer", ValidationOutputWriter.class, VALIDATION_OUTPUT_WRITER_DEFAULT);
        }
        return new ValidationConfig(threshold, verbose, loadFlowFactory, tableFormatterFactory, epsilonX, applyReactanceCorrection, validationOutputWriter, loadFlowParameter);
    }

    public ValidationConfig(float threshold, boolean verbose, Class<? extends LoadFlowFactory> loadFlowFactory, 
                            Class<? extends TableFormatterFactory> tableFormatterFactory, float epsilonX, 
                            boolean applyReactanceCorrection, ValidationOutputWriter validationOutputWriter, LoadFlowParameters loadFlowParameters) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Negative values for threshold not permitted");
        }
        if (epsilonX < 0) {
            throw new IllegalArgumentException("Negative values for epsilonX not permitted");
        }
        this.threshold = threshold;
        this.verbose = verbose;
        this.loadFlowFactory = Objects.requireNonNull(loadFlowFactory);
        this.tableFormatterFactory = Objects.requireNonNull(tableFormatterFactory);
        this.epsilonX = epsilonX;
        this.applyReactanceCorrection = applyReactanceCorrection;
        this.validationOutputWriter = Objects.requireNonNull(validationOutputWriter);
        this.loadFlowParameters = Objects.requireNonNull(loadFlowParameters);
    }

    public float getThreshold() {
        return threshold;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public Class<? extends LoadFlowFactory> getLoadFlowFactory() {
        return loadFlowFactory;
    }

    public Class<? extends TableFormatterFactory> getTableFormatterFactory() {
        return tableFormatterFactory;
    }

    public float getEpsilonX() {
        return epsilonX;
    }

    public ValidationOutputWriter getValidationOutputWriter() {
        return validationOutputWriter;
    }

    public boolean applyReactanceCorrection() {
        return applyReactanceCorrection;
    }

    public LoadFlowParameters getLoadFlowParameters() {
        return loadFlowParameters;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setLoadFlowFactory(Class<? extends LoadFlowFactory> loadFlowFactory) {
        this.loadFlowFactory = Objects.requireNonNull(loadFlowFactory);
    }

    public void setTableFormatterFactory(Class<? extends TableFormatterFactory> tableFormatterFactory) {
        this.tableFormatterFactory = Objects.requireNonNull(tableFormatterFactory);
    }

    public void setEpsilonX(float epsilonX) {
        this.epsilonX = epsilonX;
    }

    public void setApplyReactanceCorrection(boolean applyReactanceCorrection) {
        this.applyReactanceCorrection = applyReactanceCorrection;
    }

    public void setValidationOutputWriter(ValidationOutputWriter validationOutputWriter) {
        this.validationOutputWriter = Objects.requireNonNull(validationOutputWriter);
    }

    public void setLoadFlowParameters(LoadFlowParameters loadFlowParameters) {
        this.loadFlowParameters = Objects.requireNonNull(loadFlowParameters);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + 
                "threshold=" + threshold +
                ", verbose=" + verbose +
                ", loadFlowFactory=" + loadFlowFactory +
                ", tableFormatterFactory=" + tableFormatterFactory +
                ", epsilonX=" + epsilonX +
                ", applyReactanceCorrection=" + applyReactanceCorrection +
                ", validationOutputWriter=" + validationOutputWriter +
                ", loadFlowParameters=" + loadFlowParameters +
                "]";
    }
}
