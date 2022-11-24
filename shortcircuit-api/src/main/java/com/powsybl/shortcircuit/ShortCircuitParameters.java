/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;

import java.util.Objects;
import java.util.function.Supplier;

import static com.powsybl.shortcircuit.ShortCircuitConstants.*;

/**
 * Generic parameters for short circuit-computations.
 * May contain extensions for implementation-specific parameters.
 *
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParameters extends AbstractExtendable<ShortCircuitParameters> {

    public interface ConfigLoader<E extends Extension<ShortCircuitParameters>>
            extends ExtensionConfigLoader<ShortCircuitParameters, E> {
    }

    // VERSION = 1.0 withLimitViolations, withVoltageMap, withFeederResult, studyType and minVoltageDropProportionalThreshold
    // VERSION = 1.1 initialVoltageProfileType, initialNominalVoltageProfileType, withVoltageMap -> withVoltageProfileResult
    public static final String VERSION = "1.1";

    private static final Supplier<ExtensionProviders<ConfigLoader>> SUPPLIER = Suppliers
            .memoize(() -> ExtensionProviders.createProvider(ConfigLoader.class, "short-circuit-parameters"));

    private boolean withLimitViolations = DEFAULT_WITH_LIMIT_VIOLATIONS;
    private boolean withVoltageProfileResult = DEFAULT_WITH_VOLTAGE_PROFILE_RESULT;
    private boolean withFeederResult = DEFAULT_WITH_FEEDER_RESULT;
    private StudyType studyType = DEFAULT_STUDY_TYPE;
    private double minVoltageDropProportionalThreshold = DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD;
    private InitialVoltageProfileType initialVoltageProfileType = DEFAULT_INITIAL_VOLTAGE_PROFILE_TYPE;
    private InitialNominalVoltageProfileType initialNominalVoltageProfileType = DEFAULT_INITIAL_NOMINAL_VOLTAGE_PROFILE_TYPE;

    /**
     * Load parameters from platform default config.
     */
    public static ShortCircuitParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ShortCircuitParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        ShortCircuitParameters parameters = new ShortCircuitParameters();

        platformConfig.getOptionalModuleConfig("short-circuit-parameters").ifPresent(config ->
                parameters.setWithLimitViolations(config.getBooleanProperty("with-limit-violations", DEFAULT_WITH_LIMIT_VIOLATIONS))
                        .setWithVoltageProfileResult(config.getBooleanProperty("with-voltage-map", DEFAULT_WITH_VOLTAGE_PROFILE_RESULT))
                        .setWithFeederResult(config.getBooleanProperty("with-feeder-result", DEFAULT_WITH_FEEDER_RESULT))
                        .setStudyType(config.getEnumProperty("study-type", StudyType.class, DEFAULT_STUDY_TYPE))
                        .setMinVoltageDropProportionalThreshold(config.getDoubleProperty("min-voltage-drop-proportional-threshold", DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD))
                        .setInitialVoltageProfileType(config.getEnumProperty("voltage-profile-type", InitialVoltageProfileType.class, DEFAULT_INITIAL_VOLTAGE_PROFILE_TYPE))
                        .setInitialNominalVoltageProfileType(config.getEnumProperty("nominal-voltage-profile-type", InitialNominalVoltageProfileType.class, DEFAULT_INITIAL_NOMINAL_VOLTAGE_PROFILE_TYPE)));

        parameters.readExtensions(platformConfig);

        return parameters;
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (ConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

    /** Whether the result should indicate a limit violation */
    public boolean isWithLimitViolations() {
        return withLimitViolations;
    }

    public ShortCircuitParameters setWithLimitViolations(boolean withLimitViolations) {
        this.withLimitViolations = withLimitViolations;
        return this;
    }

    /** Whether the results should include the voltages on every bus of the network */
    public boolean isWithVoltageProfileResult() {
        return withVoltageProfileResult;
    }

    public ShortCircuitParameters setWithVoltageProfileResult(boolean withVoltageProfileResult) {
        this.withVoltageProfileResult = withVoltageProfileResult;
        return this;
    }

    /** Whether the results should include information on every feeder of the fault **/
    public boolean isWithFeederResult() {
        return withFeederResult;
    }

    public ShortCircuitParameters setWithFeederResult(boolean withFeederResult) {
        this.withFeederResult = withFeederResult;
        return this;
    }

    /** The type of analysis : Transient, subtransient or steady-state calculation **/
    public StudyType getStudyType() {
        return studyType;
    }

    public ShortCircuitParameters setStudyType(StudyType studyType) {
        this.studyType = studyType;
        return this;
    }

    /** The maximum voltage drop threshold in %. */
    public double getMinVoltageDropProportionalThreshold() {
        return minVoltageDropProportionalThreshold;
    }

    public ShortCircuitParameters setMinVoltageDropProportionalThreshold(double minVoltageDropProportionalThreshold) {
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        return this;
    }

    /** The type of voltage map for the calculation: nominal or previous (voltage values after the loadflow) **/
    public InitialVoltageProfileType getInitialVoltageProfileType() {
        return initialVoltageProfileType;
    }

    public ShortCircuitParameters setInitialVoltageProfileType(InitialVoltageProfileType initialVoltageProfileType) {
        this.initialVoltageProfileType = initialVoltageProfileType;
        return this;
    }

    /** If the voltage map used for the calculation is nominal, specifies the values taken **/
    public InitialNominalVoltageProfileType getInitialNominalVoltageProfileType() {
        return initialNominalVoltageProfileType;
    }

    public ShortCircuitParameters setInitialNominalVoltageProfileType(InitialNominalVoltageProfileType initialNominalVoltageProfileType) {
        this.initialNominalVoltageProfileType = initialNominalVoltageProfileType;
        return this;
    }
}
