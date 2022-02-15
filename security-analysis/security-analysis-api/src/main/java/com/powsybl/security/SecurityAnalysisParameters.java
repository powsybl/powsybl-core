/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
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

    public static final String VERSION = "1.1";

    static final double DEFAULT_INCREASED_FLOW_VIOLATIONS_THRESHOLD = 0.1; // 10.0 %
    static final double DEFAULT_INCREASED_LOW_VOLTAGE_VIOLATIONS_THRESHOLD = 0.0; // 0.0 %
    static final double DEFAULT_INCREASED_HIGH_VOLTAGE_VIOLATIONS_THRESHOLD = 0.0; // 0.0 %

    static final double DEFAULT_INCREASED_LOW_VOLTAGE_VIOLATIONS_DELTA = 0.0; // 0.0 kV
    static final double DEFAULT_INCREASED_HIGH_VOLTAGE_VIOLATIONS_DELTA = 0.0; // 0.0 kV

    private double increasedFlowViolationsThreshold = DEFAULT_INCREASED_FLOW_VIOLATIONS_THRESHOLD;
    private double increasedLowVoltageViolationsThreshold = DEFAULT_INCREASED_LOW_VOLTAGE_VIOLATIONS_THRESHOLD;
    private double increasedHighVoltageViolationsThreshold = DEFAULT_INCREASED_HIGH_VOLTAGE_VIOLATIONS_THRESHOLD;

    private double increasedLowVoltageViolationsDelta = DEFAULT_INCREASED_LOW_VOLTAGE_VIOLATIONS_DELTA;
    private double increasedHighVoltageViolationsDelta = DEFAULT_INCREASED_HIGH_VOLTAGE_VIOLATIONS_DELTA;

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
                    parameters.setIncreasedFlowViolationsThreshold(config.getDoubleProperty("increasedFlowViolationsThreshold", DEFAULT_INCREASED_FLOW_VIOLATIONS_THRESHOLD));
                    parameters.setIncreasedLowVoltageViolationsThreshold(config.getDoubleProperty("increasedLowVoltageViolationsThreshold", DEFAULT_INCREASED_LOW_VOLTAGE_VIOLATIONS_THRESHOLD));
                    parameters.setIncreasedHighVoltageViolationsThreshold(config.getDoubleProperty("increasedHighVoltageViolationsThreshold", DEFAULT_INCREASED_HIGH_VOLTAGE_VIOLATIONS_THRESHOLD));
                    parameters.setIncreasedLowVoltageViolationsDelta(config.getDoubleProperty("increasedLowVoltageViolationsDelta", DEFAULT_INCREASED_LOW_VOLTAGE_VIOLATIONS_DELTA));
                    parameters.setIncreasedHighVoltageViolationsDelta(config.getDoubleProperty("increasedHighVoltageViolationsDelta", DEFAULT_INCREASED_HIGH_VOLTAGE_VIOLATIONS_DELTA));
                });
    }

    public SecurityAnalysisParameters(double increasedFlowViolationsThreshold, double increasedLowVoltageViolationsThreshold, double increasedHighVoltageViolationsThreshold,
                                      double increasedLowVoltageViolationsDelta, double increasedHighVoltageViolationsDelta) {
        this.increasedFlowViolationsThreshold = increasedFlowViolationsThreshold;
        this.increasedLowVoltageViolationsThreshold = increasedLowVoltageViolationsThreshold;
        this.increasedHighVoltageViolationsThreshold = increasedHighVoltageViolationsThreshold;
        this.increasedLowVoltageViolationsDelta = increasedLowVoltageViolationsDelta;
        this.increasedHighVoltageViolationsDelta = increasedHighVoltageViolationsDelta;
    }

    public SecurityAnalysisParameters() {
        this(DEFAULT_INCREASED_FLOW_VIOLATIONS_THRESHOLD, DEFAULT_INCREASED_LOW_VOLTAGE_VIOLATIONS_THRESHOLD, DEFAULT_INCREASED_HIGH_VOLTAGE_VIOLATIONS_THRESHOLD,
                DEFAULT_INCREASED_LOW_VOLTAGE_VIOLATIONS_DELTA, DEFAULT_INCREASED_HIGH_VOLTAGE_VIOLATIONS_DELTA);
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (ExtensionConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

    /**
     * After a contingency, only low voltage violations that are increased of more than the delta value (in kV) compared to the pre-contingency state,
     * are listed in the limit violations, the other ones are filtered. This method gets the low voltage violations delta value (in kV, should be positive).
     * The default value is 0.0, meaning that only violations that are increased of more than 0.0 kV are listed in the limit violations (note that for low voltage violation,
     * it means that the voltage in post-contingency state is lower than the voltage in pre-contingency state).
     */
    public double getIncreasedLowVoltageViolationsDelta() {
        return increasedLowVoltageViolationsDelta;
    }

    public SecurityAnalysisParameters setIncreasedLowVoltageViolationsDelta(double increasedLowVoltageViolationsDelta) {
        if (increasedLowVoltageViolationsDelta < 0) {
            throw new PowsyblException("Increased low voltage violations delta parameter should be positive");
        }
        this.increasedLowVoltageViolationsDelta = increasedLowVoltageViolationsDelta;
        return this;
    }

    /**
     * After a contingency, only low voltage violations that are increased of more than the threshold (without unit) compared to the pre-contingency state,
     * are listed in the limit violations, the other ones are filtered. This method gets the low voltage violations threshold value (without unit, should be positive).
     * The default value is 0.0, meaning that only violations that are increased of more than 0.0 % are listed in the limit violations (note that for low voltage violation,
     * it means that the voltage in post-contingency state is lower than the voltage in pre-contingency state).
     */
    public double getIncreasedLowVoltageViolationsThreshold() {
        return increasedLowVoltageViolationsThreshold;
    }

    public SecurityAnalysisParameters setIncreasedLowVoltageViolationsThreshold(double increasedLowVoltageViolationsThreshold) {
        if (increasedLowVoltageViolationsThreshold < 0) {
            throw new PowsyblException("Increased low voltage violations threshold parameter should be positive");
        }
        this.increasedLowVoltageViolationsThreshold = increasedLowVoltageViolationsThreshold;
        return this;
    }

    /**
     * After a contingency, only high voltage violations that are increased of more than the delta value (in kV) compared to the pre-contingency state,
     * are listed in the limit violations, the other ones are filtered. This method gets the high voltage violations delta value (in kV, should be positive).
     * The default value is 0.0, meaning that only violations that are increased of more than 0.0 kV are listed in the limit violations.
     */
    public double getIncreasedHighVoltageViolationsDelta() {
        return increasedHighVoltageViolationsDelta;
    }

    public SecurityAnalysisParameters setIncreasedHighVoltageViolationsDelta(double increasedHighVoltageViolationsDelta) {
        if (increasedHighVoltageViolationsDelta < 0) {
            throw new PowsyblException("Increased high voltage violations delta parameter should be positive");
        }
        this.increasedHighVoltageViolationsDelta = increasedHighVoltageViolationsDelta;
        return this;
    }

    /**
     * After a contingency, only high voltage violations that are increased of more than the delta value (in kV) compared to the pre-contingency state,
     * are listed in the limit violations, the other ones are filtered. This method gets the high voltage violations delta value (in kV, should be positive).
     * The default value is 0.0, meaning that only violations that are increased of more than 0.0 kV are listed in the limit violations.
     */
    public double getIncreasedHighVoltageViolationsThreshold() {
        return increasedHighVoltageViolationsThreshold;
    }

    public SecurityAnalysisParameters setIncreasedHighVoltageViolationsThreshold(double increasedHighVoltageViolationsThreshold) {
        if (increasedHighVoltageViolationsThreshold < 0) {
            throw new PowsyblException("Increased high voltage violations threshold parameter should be positive");
        }
        this.increasedHighVoltageViolationsThreshold = increasedHighVoltageViolationsThreshold;
        return this;
    }

    /**
     * After a contingency, only flow violations (either current, active power or apparent power violations) that are increased in absolute value,
     * compared to the pre-contingency state, than the threshold value (without unit, should be positive) are listed in the limit violations,
     * the other ones are filtered. This method gets the flow violations threshold value. The default value is 0.1, meaning that only violations that
     * are increased of more than 10% are listed in the limit violations.
     */
    public double getIncreasedFlowViolationsThreshold() {
        return increasedFlowViolationsThreshold;
    }

    public SecurityAnalysisParameters setIncreasedFlowViolationsThreshold(double increasedFlowViolationsThreshold) {
        if (increasedFlowViolationsThreshold < 0) {
            throw new PowsyblException("Increased flow violations threshold parameter should be positive");
        }
        this.increasedFlowViolationsThreshold = increasedFlowViolationsThreshold;
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
