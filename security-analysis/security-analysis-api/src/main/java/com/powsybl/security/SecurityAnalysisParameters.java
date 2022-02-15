/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.loadflow.LoadFlowParameters;

import java.util.Objects;

/**
 * Parameters for security analysis computation.
 * Extensions may be added, for instance for implementation-specific parameters.
 *
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 * @author Sylvain LECLERC <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisParameters extends AbstractExtendable<SecurityAnalysisParameters> {

    /**
     * A configuration loader interface for the SecurityAnalysisParameters extensions loaded from the platform configuration
     * @param <E> The extension class
     */
    public static interface ConfigLoader<E extends Extension<SecurityAnalysisParameters>> extends ExtensionConfigLoader<SecurityAnalysisParameters, E> {
    }

    public static final String VERSION = "1.0";

    static final double DEFAULT_WORSENED_FLOW_CONSTRAINTS_THRESHOLD = 0.1; // 10%
    static final double DEFAULT_WORSENED_LOW_VOLTAGE_CONSTRAINTS_DELTA = 0.0; // 0.0 kV
    static final double DEFAULT_WORSENED_HIGH_VOLTAGE_CONSTRAINTS_DELTA = 0.0; // 0.0 kV

    private double worsenedFlowConstraintsThreshold = DEFAULT_WORSENED_FLOW_CONSTRAINTS_THRESHOLD;
    private double worsenedLowVoltageConstraintsDelta = DEFAULT_WORSENED_LOW_VOLTAGE_CONSTRAINTS_DELTA;
    private double worsenedHighVoltageConstraintsDelta = DEFAULT_WORSENED_HIGH_VOLTAGE_CONSTRAINTS_DELTA;

    private static final Supplier<ExtensionProviders<ConfigLoader>> SUPPLIER =
        Suppliers.memoize(() -> ExtensionProviders.createProvider(ConfigLoader.class, "security-analysis-parameters"));

    private LoadFlowParameters loadFlowParameters = new LoadFlowParameters();

    /**
     * Load parameters from platform default config.
     */
    public static SecurityAnalysisParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    /**
     * Load parameters from a provided platform config.
     */
    public static SecurityAnalysisParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        parameters.readExtensions(platformConfig);

        parameters.setLoadFlowParameters(LoadFlowParameters.load(platformConfig));

        return parameters;
    }

    protected static void load(SecurityAnalysisParameters parameters) {
        load(parameters, PlatformConfig.defaultConfig());
    }

    protected static void load(SecurityAnalysisParameters parameters, PlatformConfig platformConfig) {
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(platformConfig);

        platformConfig.getOptionalModuleConfig("security-analysis-default-parameters")
                .ifPresent(config -> {
                    parameters.setWorsenedFlowConstraintsThreshold(config.getDoubleProperty("worsenedFlowConstraintsThreshold", DEFAULT_WORSENED_FLOW_CONSTRAINTS_THRESHOLD));
                    parameters.setWorsenedLowVoltageConstraintsDelta(config.getDoubleProperty("worsenedLowVoltageConstraintsDelta", DEFAULT_WORSENED_LOW_VOLTAGE_CONSTRAINTS_DELTA));
                    parameters.setWorsenedHighVoltageConstraintsDelta(config.getDoubleProperty("worsenedHighVoltageConstraintsDelta", DEFAULT_WORSENED_HIGH_VOLTAGE_CONSTRAINTS_DELTA));
                });
    }

    public SecurityAnalysisParameters(double worsenedFlowConstraintsThreshold, double worsenedLowVoltageConstraintsDelta,
                                      double worsenedHighVoltageConstraintsDelta) {
        this.worsenedFlowConstraintsThreshold = worsenedFlowConstraintsThreshold;
        this.worsenedLowVoltageConstraintsDelta = worsenedLowVoltageConstraintsDelta;
        this.worsenedHighVoltageConstraintsDelta = worsenedHighVoltageConstraintsDelta;
    }

    public SecurityAnalysisParameters() {
        this(DEFAULT_WORSENED_FLOW_CONSTRAINTS_THRESHOLD, DEFAULT_WORSENED_LOW_VOLTAGE_CONSTRAINTS_DELTA, DEFAULT_WORSENED_HIGH_VOLTAGE_CONSTRAINTS_DELTA);
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (ExtensionConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

    /**
     * After a contingency, only low voltage constraints that are worst than the delta value (in kV) compared to the pre-contingency state,
     * are listed in the limit violations. This method gets the low voltage constraints delta value (in kV, should be positive).
     * The default value is 0.0, meaning that only constraints that are worsened of more than 0.0 kV are listed in the limit violations.
     */
    public double getWorsenedLowVoltageConstraintsDelta() {
        return worsenedLowVoltageConstraintsDelta;
    }

    public SecurityAnalysisParameters setWorsenedLowVoltageConstraintsDelta(double worsenedLowVoltageConstraintsDelta) {
        this.worsenedLowVoltageConstraintsDelta = worsenedLowVoltageConstraintsDelta;
        return this;
    }

    /**
     * After a contingency, only high voltage constraints that are worst that the delta value (in kV) compared to the pre-contingency state,
     * are listed in the limit violations. This method gets the high voltage constraints delta value (in kV, should be positive).
     * The default value is 0.0, meaning that only constraints that are worsened of more than 0.0 kV are listed in the limit violations.
     */
    public double getWorsenedHighVoltageConstraintsDelta() {
        return worsenedHighVoltageConstraintsDelta;
    }

    public SecurityAnalysisParameters setWorsenedHighVoltageConstraintsDelta(double worsenedHighVoltageConstraintsDelta) {
        this.worsenedHighVoltageConstraintsDelta = worsenedHighVoltageConstraintsDelta;
        return this;
    }

    /**
     * After a contingency, only flow constraints (current, active power or apparent power constraints) that are worst, compared
     * to the pre-contingency state, than the threshold value (without unit) are listed in the limit violations. This method
     * gets the flow constraints threshold value (without unit). The default value is 0.1, meaning that only constraints that
     * are worsened of more than 10% are listed in the limit violations.
     */
    public double getWorsenedFlowConstraintsThreshold() {
        return worsenedFlowConstraintsThreshold;
    }

    public SecurityAnalysisParameters setWorsenedFlowConstraintsThreshold(double worsenedFlowConstraintsThreshold) {
        this.worsenedFlowConstraintsThreshold = worsenedFlowConstraintsThreshold;
        return this;
    }

    public LoadFlowParameters getLoadFlowParameters() {
        return loadFlowParameters;
    }

    public SecurityAnalysisParameters setLoadFlowParameters(LoadFlowParameters loadFlowParameters) {
        this.loadFlowParameters = Objects.requireNonNull(loadFlowParameters);
        return this;
    }
}
