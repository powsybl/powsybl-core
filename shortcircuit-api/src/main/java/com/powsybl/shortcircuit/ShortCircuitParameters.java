/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.Objects;

import static com.powsybl.shortcircuit.ShortCircuitConstants.*;

/**
 * Generic parameters for short circuit-computations.
 * May contain extensions for implementation-specific parameters.
 *
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParameters extends AbstractExtendable<ShortCircuitParameters> {

    // VERSION = 1.0 withLimitViolations, withVoltageMap, withFeederResult, studyType and minVoltageDropProportionalThreshold
    // VERSION = 1.1 withVoltageMap -> withFortescueResult and withVoltageResult
    // VERSION = 1.2 subTransientCoefficient, withLoads, withShuntCompensators, withVSCConverterStations, withNeutralPosition
    public static final String VERSION = "1.2";

    private boolean withLimitViolations = DEFAULT_WITH_LIMIT_VIOLATIONS;
    private boolean withFortescueResult = DEFAULT_WITH_FORTESCUE_RESULT;
    private boolean withFeederResult = DEFAULT_WITH_FEEDER_RESULT;
    private StudyType studyType = DEFAULT_STUDY_TYPE;
    private boolean withVoltageResult = DEFAULT_WITH_VOLTAGE_RESULT;
    private double minVoltageDropProportionalThreshold = DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD;
    private double subTransientCoefficient = DEFAULT_SUB_TRANSIENT_COEFFICIENT;
    private boolean withLoads = DEFAULT_WITH_LOADS;
    private boolean withShuntCompensators = DEFAULT_WITH_SHUNT_COMPENSATORS;
    private boolean withVSCConverterStations = DEFAULT_WITH_VSC_CONVERTER_STATIONS;
    private boolean withNeutralPosition = DEFAULT_WITH_NEUTRAL_POSITION;

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
                        .setWithVoltageResult(config.getBooleanProperty("with-voltage-result", DEFAULT_WITH_VOLTAGE_RESULT))
                        .setWithFeederResult(config.getBooleanProperty("with-feeder-result", DEFAULT_WITH_FEEDER_RESULT))
                        .setStudyType(config.getEnumProperty("study-type", StudyType.class, DEFAULT_STUDY_TYPE))
                        .setWithFortescueResult(config.getBooleanProperty("with-fortescue-result", DEFAULT_WITH_FORTESCUE_RESULT))
                        .setMinVoltageDropProportionalThreshold(config.getDoubleProperty("min-voltage-drop-proportional-threshold", DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD))
                        .setSubTransientCoefficient(config.getDoubleProperty("sub-transient-coefficient", DEFAULT_SUB_TRANSIENT_COEFFICIENT))
                        .setWithLoads(config.getBooleanProperty("with-loads", DEFAULT_WITH_LOADS))
                        .setWithShuntCompensators(config.getBooleanProperty("with-shunt-compensators", DEFAULT_WITH_SHUNT_COMPENSATORS))
                        .setWithVSCConverterStations(config.getBooleanProperty("with-vsc-converter-stations", DEFAULT_WITH_VSC_CONVERTER_STATIONS))
                        .setWithNeutralPosition(config.getBooleanProperty("with-neutral-position", DEFAULT_WITH_NEUTRAL_POSITION)));

        parameters.readExtensions(platformConfig);

        return parameters;
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (ShortCircuitAnalysisProvider provider : new ServiceLoaderCache<>(ShortCircuitAnalysisProvider.class).getServices()) {
            provider.loadSpecificParameters(platformConfig).ifPresent(shortCircuitParametersExtension ->
                    addExtension((Class) shortCircuitParametersExtension.getClass(), shortCircuitParametersExtension));
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
     * @deprecated Use {@link #isWithVoltageResult()} instead. Used for backward compatibility.
     */
    @Deprecated
    public boolean isWithVoltageMap() {
        return isWithVoltageResult();
    }

    /**
     * @deprecated Use {@link #setWithVoltageResult(boolean)} instead. Used for backward compatibility.
     */
    @Deprecated
    public ShortCircuitParameters setWithVoltageMap(boolean withVoltageMap) {
        this.withVoltageResult = withVoltageMap;
        return this;
    }

    /** Whether faultResults, feederResults and shortCircuitBusResults should be detailed for each phase as FortescueValues
     * or if only the three-phase magnitude for currents and voltages should be given. **/
    public boolean isWithFortescueResult() {
        return withFortescueResult;
    }

    public ShortCircuitParameters setWithFortescueResult(boolean withFortescueResult) {
        this.withFortescueResult = withFortescueResult;
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
    public boolean isWithVoltageResult() {
        return withVoltageResult;
    }

    public ShortCircuitParameters setWithVoltageResult(boolean withVoltageResult) {
        this.withVoltageResult = withVoltageResult;
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

    /** In case of a subtransient study, a multiplicative coefficient to obtain the subtransient reactance of the generators
     * from the transient reactance.
     */
    public double getSubTransientCoefficient() {
        return subTransientCoefficient;
    }

    public ShortCircuitParameters setSubTransientCoefficient(double subTransientCoefficient) {
        this.subTransientCoefficient = subTransientCoefficient;
        return this;
    }

    /**
     * TODO
     * @return
     */
    public boolean isWithLoads() {
        return withLoads;
    }

    public ShortCircuitParameters setWithLoads(boolean withLoads) {
        this.withLoads = withLoads;
        return this;
    }

    /**
     * TODO
     * @return
     */
    public boolean isWithShuntCompensators() {
        return withShuntCompensators;
    }

    public ShortCircuitParameters setWithShuntCompensators(boolean withShuntCompensators) {
        this.withShuntCompensators = withShuntCompensators;
        return this;
    }

    /**
     * TODO
     * @return
     */
    public boolean isWithVSCConverterStations() {
        return withVSCConverterStations;
    }

    public ShortCircuitParameters setWithVSCConverterStations(boolean withVSCConverterStations) {
        this.withVSCConverterStations = withVSCConverterStations;
        return this;
    }

    /**
     * TODO
     * @return
     */
    public boolean isWithNeutralPosition() {
        return withNeutralPosition;
    }

    public ShortCircuitParameters setWithNeutralPosition(boolean withNeutralPosition) {
        this.withNeutralPosition = withNeutralPosition;
        return this;
    }
}
