/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.loadflow.LoadFlowParameters;

import java.util.Objects;

/**
 * Parameters for sensitivity analysis.
 * Extensions may be added, for instance for implementation-specific parameters.
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class SensitivityAnalysisParameters extends AbstractExtendable<SensitivityAnalysisParameters> {

    // VERSION = 1.0
    // VERSION = 1.1 flowFlowSensitivityValueThreshold, voltageVoltageSensitivityValueThreshold, flowVoltageSensitivityValueThreshold, angleFlowSensitivityValueThreshold
    // VERSION = 1.2 operatorStrategiesCalculationMode
    // VERSION = 1.3 debugDir
    public static final String VERSION = "1.3";

    static final double FLOW_FLOW_SENSITIVITY_VALUE_THRESHOLD_DEFAULT_VALUE = 0.0;
    static final double VOLTAGE_VOLTAGE_SENSITIVITY_VALUE_THRESHOLD_DEFAULT_VALUE = 0.0;
    static final double FLOW_VOLTAGE_SENSITIVITY_VALUE_THRESHOLD_DEFAULT_VALUE = 0.0;
    static final double ANGLE_FLOW_SENSITIVITY_VALUE_THRESHOLD_DEFAULT_VALUE = 0.0;
    static final SensitivityOperatorStrategiesCalculationMode SENSITIVITY_OPERATOR_STRATEGY_CALCULATION_MODE_DEFAULT_VALUE = SensitivityOperatorStrategiesCalculationMode.NONE;
    static final String DEFAULT_DEBUG_DIR = null;

    private double flowFlowSensitivityValueThreshold = FLOW_FLOW_SENSITIVITY_VALUE_THRESHOLD_DEFAULT_VALUE;
    private double voltageVoltageSensitivityValueThreshold = VOLTAGE_VOLTAGE_SENSITIVITY_VALUE_THRESHOLD_DEFAULT_VALUE;
    private double flowVoltageSensitivityValueThreshold = FLOW_VOLTAGE_SENSITIVITY_VALUE_THRESHOLD_DEFAULT_VALUE;
    private double angleFlowSensitivityValueThreshold = ANGLE_FLOW_SENSITIVITY_VALUE_THRESHOLD_DEFAULT_VALUE;

    private LoadFlowParameters loadFlowParameters = new LoadFlowParameters();

    private SensitivityOperatorStrategiesCalculationMode operatorStrategiesCalculationMode = SENSITIVITY_OPERATOR_STRATEGY_CALCULATION_MODE_DEFAULT_VALUE;
    private String debugDir = DEFAULT_DEBUG_DIR;

    /**
     * Load parameters from platform default config.
     */
    public static SensitivityAnalysisParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    /**
     * Load parameters from a provided platform config.
     */
    public static SensitivityAnalysisParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        parameters.readExtensions(platformConfig);
        parameters.setLoadFlowParameters(LoadFlowParameters.load(platformConfig));
        platformConfig.getOptionalModuleConfig("sensitivity-analysis-default-parameters")
                .ifPresent(config -> {
                    config.getOptionalDoubleProperty("flow-flow-sensitivity-value-threshold").ifPresent(parameters::setFlowFlowSensitivityValueThreshold);
                    config.getOptionalDoubleProperty("flow-voltage-sensitivity-value-threshold").ifPresent(parameters::setFlowVoltageSensitivityValueThreshold);
                    config.getOptionalDoubleProperty("voltage-voltage-sensitivity-value-threshold").ifPresent(parameters::setVoltageVoltageSensitivityValueThreshold);
                    config.getOptionalDoubleProperty("angle-flow-sensitivity-value-threshold").ifPresent(parameters::setAngleFlowSensitivityValueThreshold);
                    config.getOptionalEnumProperty("sensitivity-operator-strategies-calculation-mode", SensitivityOperatorStrategiesCalculationMode.class)
                        .ifPresent(parameters::setOperatorStrategiesCalculationMode);
                    config.getOptionalStringProperty("debug-dir").ifPresent(parameters::setDebugDir);
                });

        return parameters;
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (SensitivityAnalysisProvider provider : new ServiceLoaderCache<>(SensitivityAnalysisProvider.class).getServices()) {
            provider.loadSpecificParameters(platformConfig).ifPresent(sensitivityAnalysisParametersExtension ->
                    addExtension((Class) sensitivityAnalysisParametersExtension.getClass(), sensitivityAnalysisParametersExtension));
        }
    }

    public LoadFlowParameters getLoadFlowParameters() {
        return loadFlowParameters;
    }

    public SensitivityAnalysisParameters setLoadFlowParameters(LoadFlowParameters loadFlowParameters) {
        this.loadFlowParameters = Objects.requireNonNull(loadFlowParameters);
        return this;
    }

    public SensitivityAnalysisParameters setFlowFlowSensitivityValueThreshold(double threshold) {
        flowFlowSensitivityValueThreshold = threshold;
        return this;
    }

    /**
     * FlowFlowSensitivityValueThreshold is the threshold under which sensitivity values having
     * variable type among INJECTION_ACTIVE_POWER, INJECTION_REACTIVE_POWER and HVDC_LINE_ACTIVE_POWER
     * and function type among BRANCH_ACTIVE_POWER_1/2/3, BRANCH_REACTIVE_POWER_1/2/3 and BRANCH_CURRENT_1/2/3
     * will be filtered from the analysis results.
     * @return The threshold
     */
    public double getFlowFlowSensitivityValueThreshold() {
        return flowFlowSensitivityValueThreshold;
    }

    public SensitivityAnalysisParameters setVoltageVoltageSensitivityValueThreshold(double threshold) {
        voltageVoltageSensitivityValueThreshold = threshold;
        return this;
    }

    /**
     * VoltageVoltageSensitivityValueThreshold is the threshold under which sensitivity values having
     * variable type BUS_TARGET_VOLTAGE
     * and function type BUS_VOLTAGE will be filtered from the analysis results.
     * @return The threshold
     */
    public double getVoltageVoltageSensitivityValueThreshold() {
        return voltageVoltageSensitivityValueThreshold;
    }

    public SensitivityAnalysisParameters setFlowVoltageSensitivityValueThreshold(double threshold) {
        flowVoltageSensitivityValueThreshold = threshold;
        return this;
    }

    /**
     * FlowVoltageSensitivityValueThreshold is the threshold under which sensitivity values
     * having variable type among INJECTION_REACTIVE_POWER and function type among BUS_VOLTAGE
     * or variable type among BUS_TARGET_VOLTAGE and function type among
     * BRANCH_REACTIVE_POWER_1/2/3 and BRANCH_CURRENT_1/2/3  will be filtered from the analysis results.
     * @return The threshold
     */
    public double getFlowVoltageSensitivityValueThreshold() {
        return flowVoltageSensitivityValueThreshold;
    }

    public SensitivityAnalysisParameters setAngleFlowSensitivityValueThreshold(double threshold) {
        angleFlowSensitivityValueThreshold = threshold;
        return this;
    }

    /**
     *  AngleFlowSensitivityValueThreshold is the threshold under which sensitivity values having
     *  variable type among TRANSFORMER_PHASE and TRANSFORMER_PHASE_1/2/3
     *  and function type among BRANCH_ACTIVE_POWER_1/2/3, BRANCH_REACTIVE_POWER_1/2/3 and BRANCH_CURRENT_1/2/3
     *  will be filtered from the analysis results.
     * @return The threshold
     */
    public double getAngleFlowSensitivityValueThreshold() {
        return angleFlowSensitivityValueThreshold;
    }

    /**
     * Retrieves the calculation mode used for operator strategies sensitivities.
     *
     * @return The current {@code SensitivityOperatorStrategyCalculationMode} defining how
     *         operator strategies sensitivities are calculated. Possible values include:
     *         NONE, ALL_CONTINGENCIES, or ONLY_OPERATOR_STRATEGIES.
     */
    public SensitivityOperatorStrategiesCalculationMode getOperatorStrategiesCalculationMode() {
        return operatorStrategiesCalculationMode;
    }

    public SensitivityAnalysisParameters setOperatorStrategiesCalculationMode(SensitivityOperatorStrategiesCalculationMode operatorStrategiesCalculationMode) {
        this.operatorStrategiesCalculationMode = Objects.requireNonNull(operatorStrategiesCalculationMode);
        return this;
    }

    public String getDebugDir() {
        return debugDir;
    }

    public SensitivityAnalysisParameters setDebugDir(String debugDir) {
        this.debugDir = debugDir;
        return this;
    }
}
