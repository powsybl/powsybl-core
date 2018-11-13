/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import java.util.Objects;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.ConfigVersion;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.LoadFlowParameters;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class ValidationConfig implements Versionable {

    private static final String CONFIG_MODULE_NAME = "loadflow-validation";
    static final String DEFAULT_CONFIG_VERSION = "1.1";

    public static final double THRESHOLD_DEFAULT = 0.0;
    public static final boolean VERBOSE_DEFAULT = false;
    public static final Class<? extends TableFormatterFactory> TABLE_FORMATTER_FACTORY_DEFAULT = CsvTableFormatterFactory.class;
    public static final double EPSILON_X_DEFAULT = 0.1;
    public static final boolean APPLY_REACTANCE_CORRECTION_DEFAULT = false;
    public static final ValidationOutputWriter VALIDATION_OUTPUT_WRITER_DEFAULT = ValidationOutputWriter.CSV_MULTILINE;
    public static final boolean OK_MISSING_VALUES_DEFAULT = false;
    public static final boolean NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT = false;
    public static final boolean COMPARE_RESULTS_DEFAULT = false;
    public static final boolean CHECK_MAIN_COMPONENT_ONLY_DEFAULT = true;
    public static final boolean NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS = false;

    private ConfigVersion version = new ConfigVersion(DEFAULT_CONFIG_VERSION);
    private double threshold;
    private boolean verbose;
    private Class<? extends LoadFlowFactory> loadFlowFactory;
    private Class<? extends TableFormatterFactory> tableFormatterFactory;
    private double epsilonX;
    private boolean applyReactanceCorrection;
    private ValidationOutputWriter validationOutputWriter;
    private LoadFlowParameters loadFlowParameters;
    private boolean okMissingValues;
    private boolean noRequirementIfReactiveBoundInversion;
    private boolean compareResults;
    private boolean checkMainComponentOnly;
    private boolean noRequirementIfSetpointOutsidePowerBounds;

    public static ValidationConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ValidationConfig load(PlatformConfig platformConfig) {
        ComponentDefaultConfig componentDefaultConfig = ComponentDefaultConfig.load(platformConfig);
        LoadFlowParameters loadFlowParameter = LoadFlowParameters.load(platformConfig);
        return platformConfig.getOptionalModuleConfig(CONFIG_MODULE_NAME)
                .map(config -> {
                    double threshold = config.getDoubleProperty("threshold", THRESHOLD_DEFAULT);
                    boolean verbose = config.getBooleanProperty("verbose", VERBOSE_DEFAULT);
                    Class<? extends LoadFlowFactory> loadFlowFactory = config.getClassProperty("load-flow-factory", LoadFlowFactory.class, componentDefaultConfig.findFactoryImplClass(LoadFlowFactory.class));
                    Class<? extends TableFormatterFactory> tableFormatterFactory = config.getClassProperty("table-formatter-factory", TableFormatterFactory.class, TABLE_FORMATTER_FACTORY_DEFAULT);
                    double epsilonX = config.getDoubleProperty("epsilon-x", EPSILON_X_DEFAULT);
                    boolean applyReactanceCorrection = config.getBooleanProperty("apply-reactance-correction", APPLY_REACTANCE_CORRECTION_DEFAULT);
                    ValidationOutputWriter validationOutputWriter = config.getEnumProperty("output-writer", ValidationOutputWriter.class, VALIDATION_OUTPUT_WRITER_DEFAULT);
                    boolean okMissingValues = config.getBooleanProperty("ok-missing-values", OK_MISSING_VALUES_DEFAULT);
                    boolean noRequirementIfReactiveBoundInversion = config.getBooleanProperty("no-requirement-if-reactive-bound-inversion", NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT);
                    boolean compareResults = config.getBooleanProperty("compare-results", COMPARE_RESULTS_DEFAULT);
                    boolean checkMainComponentOnly = config.getBooleanProperty("check-main-component-only", CHECK_MAIN_COMPONENT_ONLY_DEFAULT);
                    boolean noRequirementIfSetpointOutsidePowerBounds = config.getBooleanProperty("no-requirement-if-setpoint-outside-power-bounds", NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);

                    return config.getOptionalStringProperty("version")
                            .map(v -> new ValidationConfig(new ConfigVersion(v), threshold, verbose, loadFlowFactory, tableFormatterFactory, epsilonX, applyReactanceCorrection,
                                    validationOutputWriter, loadFlowParameter, okMissingValues, noRequirementIfReactiveBoundInversion, compareResults, checkMainComponentOnly, noRequirementIfSetpointOutsidePowerBounds))
                            .orElseGet(() -> new ValidationConfig(threshold, verbose, loadFlowFactory, tableFormatterFactory, epsilonX, applyReactanceCorrection,
                                    validationOutputWriter, loadFlowParameter, okMissingValues, noRequirementIfReactiveBoundInversion, compareResults, checkMainComponentOnly, noRequirementIfSetpointOutsidePowerBounds));
                })
                .orElseGet(() -> new ValidationConfig(THRESHOLD_DEFAULT, VERBOSE_DEFAULT, componentDefaultConfig.findFactoryImplClass(LoadFlowFactory.class),
                        TABLE_FORMATTER_FACTORY_DEFAULT, EPSILON_X_DEFAULT, APPLY_REACTANCE_CORRECTION_DEFAULT, VALIDATION_OUTPUT_WRITER_DEFAULT, loadFlowParameter,
                        OK_MISSING_VALUES_DEFAULT, NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT, COMPARE_RESULTS_DEFAULT, CHECK_MAIN_COMPONENT_ONLY_DEFAULT, NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS));


    }

    public ValidationConfig(double threshold, boolean verbose, Class<? extends LoadFlowFactory> loadFlowFactory,
                            Class<? extends TableFormatterFactory> tableFormatterFactory, double epsilonX,
                            boolean applyReactanceCorrection, ValidationOutputWriter validationOutputWriter, LoadFlowParameters loadFlowParameters,
                            boolean okMissingValues, boolean noRequirementIfReactiveBoundInversion, boolean compareResults, boolean checkMainComponentOnly,
                            boolean noRequirementIfSetpointOutsidePowerBounds) {
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
        this.okMissingValues = okMissingValues;
        this.noRequirementIfReactiveBoundInversion = noRequirementIfReactiveBoundInversion;
        this.compareResults = compareResults;
        this.checkMainComponentOnly = checkMainComponentOnly;
        this.noRequirementIfSetpointOutsidePowerBounds = noRequirementIfSetpointOutsidePowerBounds;
    }

    public ValidationConfig(ConfigVersion version, double threshold, boolean verbose, Class<? extends LoadFlowFactory> loadFlowFactory,
                            Class<? extends TableFormatterFactory> tableFormatterFactory, double epsilonX,
                            boolean applyReactanceCorrection, ValidationOutputWriter validationOutputWriter, LoadFlowParameters loadFlowParameters,
                            boolean okMissingValues, boolean noRequirementIfReactiveBoundInversion, boolean compareResults, boolean checkMainComponentOnly,
                            boolean noRequirementIfSetpointOutsidePowerBounds) {
        this(threshold, verbose, loadFlowFactory, tableFormatterFactory, epsilonX, applyReactanceCorrection, validationOutputWriter, loadFlowParameters,
                okMissingValues, noRequirementIfReactiveBoundInversion, compareResults, checkMainComponentOnly, noRequirementIfSetpointOutsidePowerBounds);
        this.version = version;
    }

    public double getThreshold() {
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

    public double getEpsilonX() {
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

    public boolean areOkMissingValues() {
        return okMissingValues;
    }

    public boolean isNoRequirementIfReactiveBoundInversion() {
        return noRequirementIfReactiveBoundInversion;
    }

    public boolean isCompareResults() {
        return compareResults;
    }

    public boolean isCheckMainComponentOnly() {
        return checkMainComponentOnly;
    }

    public boolean isNoRequirementIfSetpointOutsidePowerBounds() {
        return noRequirementIfSetpointOutsidePowerBounds;
    }

    public void setThreshold(double threshold) {
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

    public void setEpsilonX(double epsilonX) {
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

    public void setOkMissingValues(boolean okMissingValues) {
        this.okMissingValues = okMissingValues;
    }

    public void setNoRequirementIfReactiveBoundInversion(boolean noRequirementIfReactiveBoundInversion) {
        this.noRequirementIfReactiveBoundInversion = noRequirementIfReactiveBoundInversion;
    }

    public void setCompareResults(boolean compareResults) {
        this.compareResults = compareResults;
    }

    public void setCheckMainComponentOnly(boolean checkMainComponentOnly) {
        this.checkMainComponentOnly = checkMainComponentOnly;
    }

    public void setNoRequirementIfSetpointOutsidePowerBounds(boolean noRequirementIfSetpointOutsidePowerBounds) {
        this.noRequirementIfSetpointOutsidePowerBounds = noRequirementIfSetpointOutsidePowerBounds;
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
                ", okMissingValues=" + okMissingValues +
                ", noRequirementIfReactiveBoundInversion=" + noRequirementIfReactiveBoundInversion +
                ", compareResults=" + compareResults +
                ", checkMainComponentOnly=" + checkMainComponentOnly +
                ", noRequirementIfSetpointOutsidePowerBounds=" + noRequirementIfSetpointOutsidePowerBounds +
                "]";
    }

    @Override
    public String getName() {
        return CONFIG_MODULE_NAME;
    }

    @Override
    public String getVersion() {
        return version.toString();
    }
}
