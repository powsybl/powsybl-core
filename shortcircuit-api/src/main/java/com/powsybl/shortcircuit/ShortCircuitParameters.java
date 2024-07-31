/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.shortcircuit.json.ShortCircuitAnalysisJsonModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.powsybl.commons.json.JsonUtil.createObjectMapper;
import static com.powsybl.shortcircuit.ShortCircuitConstants.*;
import static com.powsybl.shortcircuit.VoltageRange.checkVoltageRange;

/**
 * Generic parameters for short-circuit computations.
 * May contain extensions for implementation-specific parameters.
 *
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParameters extends AbstractExtendable<ShortCircuitParameters> {

    // VERSION = 1.0 withLimitViolations, withVoltageMap, withFeederResult, studyType and minVoltageDropProportionalThreshold
    // VERSION = 1.1 withVoltageMap -> withFortescueResult and withVoltageResult
    // VERSION = 1.2 subTransientCoefficient, withLoads, withShuntCompensators, withVSCConverterStations, withNeutralPosition,
    //                initialVoltageProfileMode, voltageRange
    // VERSION = 1.3 detailedLog, voltage in voltageRange
    public static final String VERSION = "1.3";

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
    private InitialVoltageProfileMode initialVoltageProfileMode = DEFAULT_INITIAL_VOLTAGE_PROFILE_MODE;
    private List<VoltageRange> voltageRanges;
    private boolean detailedReport = DEFAULT_DETAILED_REPORT;

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
                        .setWithNeutralPosition(config.getBooleanProperty("with-neutral-position", DEFAULT_WITH_NEUTRAL_POSITION))
                        .setInitialVoltageProfileMode(config.getEnumProperty("initial-voltage-profile-mode", InitialVoltageProfileMode.class, DEFAULT_INITIAL_VOLTAGE_PROFILE_MODE))
                        .setVoltageRanges(getVoltageRangesFromConfig(config))
                        .setDetailedReport(config.getBooleanProperty("detailed-report", DEFAULT_DETAILED_REPORT)));

        parameters.validate();
        parameters.readExtensions(platformConfig);

        return parameters;
    }

    private static List<VoltageRange> getVoltageRangesFromConfig(ModuleConfig config) {
        return config.getOptionalPathProperty("voltage-ranges").map((Function<Path, List<VoltageRange>>) voltageRangePath -> {
            ObjectMapper mapper = createObjectMapper().registerModule(new ShortCircuitAnalysisJsonModule());
            try (InputStream is = Files.newInputStream(voltageRangePath)) {
                return mapper.readValue(is, new TypeReference<>() {
                });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).orElse(Collections.emptyList());
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (ShortCircuitAnalysisProvider provider : new ServiceLoaderCache<>(ShortCircuitAnalysisProvider.class).getServices()) {
            provider.loadSpecificParameters(platformConfig).ifPresent(shortCircuitParametersExtension ->
                    addExtension((Class) shortCircuitParametersExtension.getClass(), shortCircuitParametersExtension));
        }
    }

    /**
     * Whether limit violations should be returned after the calculation.
     * They indicate whether the maximum or minimum allowable current has been reached.
     */
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
    @Deprecated(since = "5.2.0")
    public boolean isWithVoltageMap() {
        return isWithVoltageResult();
    }

    /**
     * @deprecated Use {@link #setWithVoltageResult(boolean)} instead. Used for backward compatibility.
     */
    @Deprecated(since = "5.2.0")
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
     * The type of study: transient, sub-transient or steady state.
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

    /** In the case of a sub-transient study, a multiplicative coefficient to obtain the sub-transient reactance of the generators
     * from the transient reactance. By default, X''d = 0.7 * X'd.
     */
    public double getSubTransientCoefficient() {
        return subTransientCoefficient;
    }

    public ShortCircuitParameters setSubTransientCoefficient(double subTransientCoefficient) {
        this.subTransientCoefficient = checkSubTransientCoefficient(subTransientCoefficient);
        return this;
    }

    /**
     * Whether the loads should be taken into account for the computation.
     * If false, the loads P0 and Q0 are considered to be set to 0.
     */
    public boolean isWithLoads() {
        return withLoads;
    }

    public ShortCircuitParameters setWithLoads(boolean withLoads) {
        this.withLoads = withLoads;
        return this;
    }

    /**
     * Whether the shunt compensators should be taken into account in the computation.
     * If true, the shunt admittance will be used in the admittance matrix. If false,
     * the shunts will be ignored.
     */
    public boolean isWithShuntCompensators() {
        return withShuntCompensators;
    }

    public ShortCircuitParameters setWithShuntCompensators(boolean withShuntCompensators) {
        this.withShuntCompensators = withShuntCompensators;
        return this;
    }

    /**
     * Whether the VSC converter stations should be taken into account in the computation.
     * If true, the VSC converter stations are modeled as equivalent reactances. If false,
     * they are ignored.
     */
    public boolean isWithVSCConverterStations() {
        return withVSCConverterStations;
    }

    public ShortCircuitParameters setWithVSCConverterStations(boolean withVSCConverterStations) {
        this.withVSCConverterStations = withVSCConverterStations;
        return this;
    }

    /**
     * Defines which step of the tap changer should be used for the computation.
     * If false, the step defined in the network model will be used. If true,
     * the neutral step (rho = 1, alpha = 0) will be used.
     */
    public boolean isWithNeutralPosition() {
        return withNeutralPosition;
    }

    public ShortCircuitParameters setWithNeutralPosition(boolean withNeutralPosition) {
        this.withNeutralPosition = withNeutralPosition;
        return this;
    }

    /**
     * The initial voltage profile mode, it can be either:
     * - nominal: nominal voltages will be used
     * - configured: the voltage profile is given by the user, voltage ranges and associated coefficients should be given using {@link VoltageRange}
     * - previous value: the voltage profile computed from the load flow will be used
     */
    public InitialVoltageProfileMode getInitialVoltageProfileMode() {
        return initialVoltageProfileMode;
    }

    public ShortCircuitParameters setInitialVoltageProfileMode(InitialVoltageProfileMode initialVoltageProfileMode) {
        this.initialVoltageProfileMode = initialVoltageProfileMode;
        return this;
    }

    /**
     * In case of CONFIGURED initial voltage profile, the coefficients to apply to each nominal voltage.
     * @return a list with voltage ranges and associated coefficients
     */
    public List<VoltageRange> getVoltageRanges() {
        return voltageRanges;
    }

    public ShortCircuitParameters setVoltageRanges(List<VoltageRange> voltageRanges) {
        checkVoltageRange(voltageRanges);
        this.voltageRanges = voltageRanges;
        return this;
    }

    /**
     * A boolean indicating if the functional logs in reportNode should be detailed or aggregated.
     */
    public boolean isDetailedReport() {
        return detailedReport;
    }

    public ShortCircuitParameters setDetailedReport(boolean detailedReport) {
        this.detailedReport = detailedReport;
        return this;
    }

    /**
     * Validates the ShortCircuitParameters. If the initial voltage profile mode is set to CONFIGURED, then the voltage ranges should not be empty.
     */
    public void validate() {
        if (initialVoltageProfileMode == InitialVoltageProfileMode.CONFIGURED && (voltageRanges == null || voltageRanges.isEmpty())) {
            throw new PowsyblException("Configured initial voltage profile but nominal voltage ranges with associated coefficients are missing.");
        }
    }

    private double checkSubTransientCoefficient(double subTransientCoefficient) {
        if (subTransientCoefficient > 1) {
            throw new PowsyblException("subTransientCoefficient > 1");
        }
        return subTransientCoefficient;
    }
}
