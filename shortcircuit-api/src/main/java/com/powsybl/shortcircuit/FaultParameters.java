/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.json.ShortCircuitAnalysisJsonModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultParameters {

    // VERSION = 1.0 withLimitViolations, withVoltageMap, withFeederResult, studyType and minVoltageDropProportionalThreshold
    // VERSION = 1.1 withVoltageMap -> withFortescueResult and withVoltageResult
    // VERSION = 1.2 subTransientCoefficient, withLoads, withShuntCompensators, withVSCConverterStations, withNeutralPosition, initialVoltageProfile
    public static final String VERSION = "1.2";

    private final String id;

    private final boolean withLimitViolations;

    private final boolean withFortescueResult;

    private final StudyType studyType;

    private final boolean withFeederResult;

    private final boolean withVoltageResult;

    private final double minVoltageDropProportionalThreshold;

    private final double subTransientCoefficient;

    private final boolean withLoads;

    private final boolean withShuntCompensators;

    private final boolean withVSCConverterStations;

    private final boolean withNeutralPosition;

    private final InitialVoltageProfile initialVoltageProfile;

    private final List<ConfiguredInitialVoltageProfileCoefficient> configuredInitialVoltageProfileCoefficients;

    /** Fault id */
    public String getId() {
        return id;
    }

    /** Override general parameter withLimitViolations from {@link ShortCircuitParameters} */
    public boolean isWithLimitViolations() {
        return withLimitViolations;
    }

    /** Override general parameter withFortescueResult from {@link ShortCircuitParameters} */
    public boolean isWithFortescueResult() {
        return withFortescueResult;
    }

    /** Override general parameter studyType from {@link ShortCircuitParameters} */
    public StudyType getStudyType() {
        return studyType;
    }

    /** Override general parameter withFeederResult from {@link ShortCircuitParameters} */
    public boolean isWithFeederResult() {
        return withFeederResult;
    }

    /** Override general parameter withVoltageResult from {@link ShortCircuitParameters} */
    public boolean isWithVoltageResult() {
        return withVoltageResult;
    }

    /** Override general parameter minVoltageDropProportionalThreshold from {@link ShortCircuitParameters} */
    public double getMinVoltageDropProportionalThreshold() {
        return minVoltageDropProportionalThreshold;
    }

    /** Override general parameter subTransientCoefficient from {@link ShortCircuitParameters} */
    public double getSubTransientCoefficient() {
        return subTransientCoefficient;
    }

    /** Override general parameter withLoads from {@link com.powsybl.shortcircuit.ShortCircuitParameters} */
    public boolean isWithLoads() {
        return withLoads;
    }

    /** Override general parameter withShuntCompensators from {@link com.powsybl.shortcircuit.ShortCircuitParameters} */
    public boolean isWithShuntCompensators() {
        return withShuntCompensators;
    }

    /** Override general parameter withVSCConverterStations from {@link com.powsybl.shortcircuit.ShortCircuitParameters} */
    public boolean isWithVSCConverterStations() {
        return withVSCConverterStations;
    }

    /** Override general parameter withNeutralPosition from {@link com.powsybl.shortcircuit.ShortCircuitParameters} */
    public boolean isWithNeutralPosition() {
        return withNeutralPosition;
    }

    /** Override general parameter initialVoltageProfile from {@link com.powsybl.shortcircuit.ShortCircuitParameters} */
    public InitialVoltageProfile getInitialVoltageProfile() {
        return initialVoltageProfile;
    }

    /** Override general parameter configuredInitialVoltageProfileCoefficients from {@link ShortCircuitParameters}*/
    public List<ConfiguredInitialVoltageProfileCoefficient> getConfiguredInitialVoltageProfileCoefficients() {
        return configuredInitialVoltageProfileCoefficients;
    }

    public FaultParameters(String id,
                           boolean withLimitViolations,
                           boolean withVoltageResult,
                           boolean withFeederResult,
                           StudyType studyType,
                           double minVoltageDropProportionalThreshold,
                           boolean withFortescueResult,
                           double subTransientCoefficient,
                           boolean withLoads,
                           boolean withShuntCompensators,
                           boolean withVSCConverterStations,
                           boolean withNeutralPosition,
                           InitialVoltageProfile initialVoltageProfile,
                           List<ConfiguredInitialVoltageProfileCoefficient> coefficients) {
        this.id = Objects.requireNonNull(id);
        this.withLimitViolations = withLimitViolations;
        this.withVoltageResult = withVoltageResult;
        this.withFeederResult = withFeederResult;
        this.studyType = studyType;
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        this.withFortescueResult = withFortescueResult;
        this.subTransientCoefficient = subTransientCoefficient;
        this.withLoads = withLoads;
        this.withShuntCompensators = withShuntCompensators;
        this.withVSCConverterStations = withVSCConverterStations;
        this.withNeutralPosition = withNeutralPosition;
        this.initialVoltageProfile = initialVoltageProfile;
        this.configuredInitialVoltageProfileCoefficients = new ArrayList<>();
        if (coefficients != null) {
            this.configuredInitialVoltageProfileCoefficients.addAll(coefficients);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FaultParameters that = (FaultParameters) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(withLimitViolations, that.withLimitViolations) &&
                Objects.equals(withVoltageResult, that.withVoltageResult) &&
                Objects.equals(withFeederResult, that.withFeederResult) &&
                Objects.equals(studyType, that.studyType) &&
                Objects.equals(minVoltageDropProportionalThreshold, that.minVoltageDropProportionalThreshold) &&
                Objects.equals(withFortescueResult, that.withFortescueResult) &&
                Objects.equals(subTransientCoefficient, that.subTransientCoefficient) &&
                Objects.equals(withLoads, that.withLoads) &&
                Objects.equals(withShuntCompensators, that.withShuntCompensators) &&
                Objects.equals(withVSCConverterStations, that.withVSCConverterStations) &&
                Objects.equals(withNeutralPosition, that.withNeutralPosition) &&
                Objects.equals(initialVoltageProfile, that.initialVoltageProfile) &&
                Objects.equals(configuredInitialVoltageProfileCoefficients, that.configuredInitialVoltageProfileCoefficients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, withLimitViolations, withVoltageResult, withFeederResult, studyType,
                minVoltageDropProportionalThreshold, withFortescueResult, subTransientCoefficient,
                withLoads, withShuntCompensators, withVSCConverterStations, withNeutralPosition,
                initialVoltageProfile, configuredInitialVoltageProfileCoefficients);
    }

    @Override
    public String toString() {
        return "FaultParameters{" +
                "id=" + id +
                ", withLimitViolations=" + withLimitViolations +
                ", withVoltageResult=" + withVoltageResult +
                ", withFeederResult=" + withFeederResult +
                ", studyType=" + studyType +
                ", minVoltageDropProportionalThreshold=" + minVoltageDropProportionalThreshold +
                ", withFortescueResult=" + withFortescueResult +
                ", subTransientCoefficient=" + subTransientCoefficient +
                ", withLoads=" + withLoads +
                ", withShuntCompensators=" + withShuntCompensators +
                ", withVSCConverterStations=" + withVSCConverterStations +
                ", withNeutralPosition=" + withNeutralPosition +
                ", initialVoltageProfile=" + initialVoltageProfile +
                ", configuredInitialVoltageProfileCoefficients=" + configuredInitialVoltageProfileCoefficients +
                '}';
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper().registerModule(new ShortCircuitAnalysisJsonModule());
    }

    public static void write(List<FaultParameters> parameters, Path jsonFile) {
        try (OutputStream out = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, parameters);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<FaultParameters> read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return createObjectMapper().readerForListOf(FaultParameters.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
