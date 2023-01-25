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
    // VERSION = 1.1 withVoltageMap -> withFortescueResults and withVoltageResults
    public static final String VERSION = "1.1";

    private static final Supplier<ExtensionProviders<ConfigLoader>> SUPPLIER = Suppliers
            .memoize(() -> ExtensionProviders.createProvider(ConfigLoader.class, "short-circuit-parameters"));

    private boolean withLimitViolations = DEFAULT_WITH_LIMIT_VIOLATIONS;
    private boolean withFortescueResults = DEFAULT_WITH_FORTESCUE_RESULT;
    private boolean withFeederResult = DEFAULT_WITH_FEEDER_RESULT;
    private StudyType studyType = DEFAULT_STUDY_TYPE;
    private boolean withVoltageResults = DEFAULT_WITH_VOLTAGE_RESULTS;
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
                        .setWithVoltageResults(config.getBooleanProperty("with-voltage-profile-result", DEFAULT_WITH_VOLTAGE_RESULTS))
                        .setWithFeederResult(config.getBooleanProperty("with-feeder-result", DEFAULT_WITH_FEEDER_RESULT))
                        .setStudyType(config.getEnumProperty("study-type", StudyType.class, DEFAULT_STUDY_TYPE))
                        .setMinVoltageDropProportionalThreshold(config.getDoubleProperty("min-voltage-drop-proportional-threshold", DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD))
                        .setWithFortescueResults(config.getBooleanProperty("with-voltage-drop-profile-result", DEFAULT_WITH_FORTESCUE_RESULT)));

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
     * @deprecated Use {@link #isWithVoltageResults()} instead. Used for backward compatibility.
     */
    @Deprecated
    public boolean isWithVoltageMap() {
        return withVoltageResults;
    }

    /**
     * @deprecated Use {@link #setWithVoltageResults(boolean)} instead. Used for backward compatibility.
     */
    @Deprecated
    public ShortCircuitParameters setWithVoltageMap(boolean withVoltageMap) {
        this.withVoltageResults = withVoltageMap;
        return this;
    }

    /** Whether faultResults, feederResults and shortCircuitBusResults should be detailed for each phase as FortescueValues
     * or if only the three-phase magnitude for currents and voltages should be given. **/
    public boolean isWithFortescueResults() {
        return withFortescueResults;
    }

    public ShortCircuitParameters setWithFortescueResults(boolean withFortescueResults) {
        this.withFortescueResults = withFortescueResults;
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
    public boolean isWithVoltageResults() {
        return withVoltageResults;
    }

    public ShortCircuitParameters setWithVoltageResults(boolean withVoltageResults) {
        this.withVoltageResults = withVoltageResults;
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
