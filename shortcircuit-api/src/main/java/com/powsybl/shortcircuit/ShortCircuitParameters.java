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
    // VERSION = 1.1 withVoltageMap -> withSimpleResult and withVoltageAndVoltageDropProfileResult
    public static final String VERSION = "1.1";

    private static final Supplier<ExtensionProviders<ConfigLoader>> SUPPLIER = Suppliers
            .memoize(() -> ExtensionProviders.createProvider(ConfigLoader.class, "short-circuit-parameters"));

    private boolean withLimitViolations = DEFAULT_WITH_LIMIT_VIOLATIONS;
    private boolean withSimpleResult = DEFAULT_WITH_SIMPLE_RESULT;
    private boolean withFeederResult = DEFAULT_WITH_FEEDER_RESULT;
    private StudyType studyType = DEFAULT_STUDY_TYPE;
    private boolean withVoltageAndVoltageDropProfileResult = DEFAULT_WITH_VOLTAGE_AND_VOLTAGE_DROP_PROFILE_RESULT;
    private double minVoltageDropProportionalThreshold = DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD;

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
                        .setWithVoltageAndVoltageDropProfileResult(config.getBooleanProperty("with-voltage-profile-result", DEFAULT_WITH_VOLTAGE_AND_VOLTAGE_DROP_PROFILE_RESULT))
                        .setWithFeederResult(config.getBooleanProperty("with-feeder-result", DEFAULT_WITH_FEEDER_RESULT))
                        .setStudyType(config.getEnumProperty("study-type", StudyType.class, DEFAULT_STUDY_TYPE))
                        .setMinVoltageDropProportionalThreshold(config.getDoubleProperty("min-voltage-drop-proportional-threshold", DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD))
                        .setWithSimpleResult(config.getBooleanProperty("with-voltage-drop-profile-result", DEFAULT_WITH_SIMPLE_RESULT)));

        parameters.readExtensions(platformConfig);

        return parameters;
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (ConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

    public boolean isWithLimitViolations() {
        return withLimitViolations;
    }

    public ShortCircuitParameters setWithLimitViolations(boolean withLimitViolations) {
        this.withLimitViolations = withLimitViolations;
        return this;
    }

    /**
     * @deprecated Use {@link #isWithVoltageAndVoltageDropProfileResult()} instead. Used for backward compatibility.
     */
    @Deprecated
    public boolean isWithVoltageMap() {
        return withVoltageAndVoltageDropProfileResult;
    }

    /**
     * @deprecated Use {@link #setWithVoltageAndVoltageDropProfileResult(boolean)} instead. Used for backward compatibility.
     */
    @Deprecated
    public ShortCircuitParameters setWithVoltageMap(boolean withVoltageMap) {
        this.withVoltageAndVoltageDropProfileResult = withVoltageMap;
        return this;
    }

    /** Whether results should be given for every phase or in a simple form. **/
    public boolean isWithSimpleResult() {
        return withSimpleResult;
    }

    public ShortCircuitParameters setWithSimpleResult(boolean withSimpleResult) {
        this.withSimpleResult = withSimpleResult;
        return this;
    }

    /**
     * The type of study: transient, subtransient or steady state.
     */
    public StudyType getStudyType() {
        return studyType;
    }

    public ShortCircuitParameters setStudyType(StudyType studyType) {
        this.studyType = studyType;
        return this;
    }

    /**
     * Whether the results should include the currents on each feeder of the fault point.
     */
    public boolean isWithFeederResult() {
        return withFeederResult;
    }

    public ShortCircuitParameters setWithFeederResult(boolean withFeederResult) {
        this.withFeederResult = withFeederResult;
        return this;
    }

    /**
     * Whether the results should include the voltages and voltage drops on every bus of the network.
     */
    public boolean isWithVoltageAndVoltageDropProfileResult() {
        return withVoltageAndVoltageDropProfileResult;
    }

    public ShortCircuitParameters setWithVoltageAndVoltageDropProfileResult(boolean withVoltageAndVoltageDropProfileResult) {
        this.withVoltageAndVoltageDropProfileResult = withVoltageAndVoltageDropProfileResult;
        return this;
    }

    /** The maximum voltage drop threshold in %, to filter the results. */
    public double getMinVoltageDropProportionalThreshold() {
        return minVoltageDropProportionalThreshold;
    }

    public ShortCircuitParameters setMinVoltageDropProportionalThreshold(double minVoltageDropProportionalThreshold) {
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        return this;
    }
}
