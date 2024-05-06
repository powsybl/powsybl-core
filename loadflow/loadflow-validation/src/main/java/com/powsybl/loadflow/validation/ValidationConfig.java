/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.loadflow.LoadFlowParameters;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.it>}
 */
public class ValidationConfig {

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

    private double threshold;
    private boolean verbose;
    private String loadFlowName;
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
        LoadFlowParameters loadFlowParameter = LoadFlowParameters.load(platformConfig);
        Optional<ModuleConfig> config = platformConfig.getOptionalModuleConfig("loadflow-validation");
        double threshold = config.map(c -> c.getOptionalDoubleProperty("threshold").orElse(THRESHOLD_DEFAULT)).orElse(THRESHOLD_DEFAULT);
        boolean verbose = config.flatMap(c -> c.getOptionalBooleanProperty("verbose")).orElse(VERBOSE_DEFAULT);
        String loadFlowName = config.flatMap(c -> c.getOptionalStringProperty("load-flow-name")).orElse(null);
        Class<? extends TableFormatterFactory> tableFormatterFactory = config.flatMap(c -> c.getOptionalClassProperty("table-formatter-factory", TableFormatterFactory.class)).orElse(TABLE_FORMATTER_FACTORY_DEFAULT);
        double epsilonX = config.map(c -> c.getOptionalDoubleProperty("epsilon-x").orElse(EPSILON_X_DEFAULT)).orElse(EPSILON_X_DEFAULT);
        boolean applyReactanceCorrection = config.flatMap(c -> c.getOptionalBooleanProperty("apply-reactance-correction")).orElse(APPLY_REACTANCE_CORRECTION_DEFAULT);
        ValidationOutputWriter validationOutputWriter = config.flatMap(c -> c.getOptionalEnumProperty("output-writer", ValidationOutputWriter.class)).orElse(VALIDATION_OUTPUT_WRITER_DEFAULT);
        boolean okMissingValues = config.flatMap(c -> c.getOptionalBooleanProperty("ok-missing-values")).orElse(OK_MISSING_VALUES_DEFAULT);
        boolean noRequirementIfReactiveBoundInversion = config.flatMap(c -> c.getOptionalBooleanProperty("no-requirement-if-reactive-bound-inversion")).orElse(NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT);
        boolean compareResults = config.flatMap(c -> c.getOptionalBooleanProperty("compare-results")).orElse(COMPARE_RESULTS_DEFAULT);
        boolean checkMainComponentOnly = config.flatMap(c -> c.getOptionalBooleanProperty("check-main-component-only")).orElse(CHECK_MAIN_COMPONENT_ONLY_DEFAULT);
        boolean noRequirementIfSetpointOutsidePowerBounds = config.flatMap(c -> c.getOptionalBooleanProperty("no-requirement-if-setpoint-outside-power-bounds")).orElse(NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);

        return new ValidationConfig(threshold, verbose, loadFlowName, tableFormatterFactory, epsilonX, applyReactanceCorrection, validationOutputWriter, loadFlowParameter,
                                    okMissingValues, noRequirementIfReactiveBoundInversion, compareResults, checkMainComponentOnly, noRequirementIfSetpointOutsidePowerBounds);
    }

    public ValidationConfig(double threshold, boolean verbose, String loadFlowName,
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
        this.loadFlowName = loadFlowName;
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

    public double getThreshold() {
        return threshold;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public Optional<String> getLoadFlowName() {
        return Optional.ofNullable(loadFlowName);
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

    public void setLoadFlowName(String loadFlowName) {
        this.loadFlowName = loadFlowName;
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
                ", loadFlowName=" + loadFlowName +
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

}
