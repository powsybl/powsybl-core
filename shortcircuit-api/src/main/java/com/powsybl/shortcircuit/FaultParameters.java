/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.json.ShortCircuitAnalysisJsonModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.powsybl.shortcircuit.VoltageRange.checkVoltageRange;

/**
 * Class to override general short-circuit analysis parameters and make them specific to a particular fault.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class FaultParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(FaultParameters.class);

    // VERSION = 1.0 withLimitViolations, withVoltageMap, withFeederResult, studyType and minVoltageDropProportionalThreshold
    // VERSION = 1.1 withVoltageMap -> withFortescueResult and withVoltageResult
    // VERSION = 1.2 subTransientCoefficient, withLoads, withShuntCompensators, withVSCConverterStations, withNeutralPosition,
    //                initialVoltageProfileMode, voltageRange
    // VERSION = 1.3 voltage in voltageRange
    public static final String VERSION = "1.3";

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

    private final InitialVoltageProfileMode initialVoltageProfileMode;

    private final List<VoltageRange> voltageRanges;

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

    /** Override general parameter initialVoltageProfileMode from {@link com.powsybl.shortcircuit.ShortCircuitParameters} */
    public InitialVoltageProfileMode getInitialVoltageProfileMode() {
        return initialVoltageProfileMode;
    }

    /** Override general parameter voltageRanges from {@link ShortCircuitParameters}*/
    public List<VoltageRange> getVoltageRanges() {
        return voltageRanges;
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
                           InitialVoltageProfileMode initialVoltageProfileMode,
                           List<VoltageRange> voltageRanges) {
        this.id = Objects.requireNonNull(id);
        this.withLimitViolations = withLimitViolations;
        this.withVoltageResult = withVoltageResult;
        this.withFeederResult = withFeederResult;
        this.studyType = studyType;
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        this.withFortescueResult = withFortescueResult;
        this.subTransientCoefficient = checkSubTransientCoefficient(subTransientCoefficient);
        this.withLoads = withLoads;
        this.withShuntCompensators = withShuntCompensators;
        this.withVSCConverterStations = withVSCConverterStations;
        this.withNeutralPosition = withNeutralPosition;
        this.initialVoltageProfileMode = initialVoltageProfileMode;
        this.voltageRanges = new ArrayList<>();
        if (voltageRanges != null) {
            if (initialVoltageProfileMode == InitialVoltageProfileMode.CONFIGURED) {
                checkVoltageRange(voltageRanges);
                this.voltageRanges.addAll(voltageRanges);
            } else {
                LOGGER.warn("Nominal voltage ranges with associated coefficient are defined but InitialVoltageProfileMode is not CONFIGURED: they are ignored");
            }
        }
        this.validate();
    }

    private double checkSubTransientCoefficient(double subTransientCoefficient) {
        if (subTransientCoefficient > 1) {
            throw new PowsyblException("subTransientCoefficient > 1");
        }
        return subTransientCoefficient;
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
                Objects.equals(initialVoltageProfileMode, that.initialVoltageProfileMode) &&
                Objects.equals(voltageRanges, that.voltageRanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, withLimitViolations, withVoltageResult, withFeederResult, studyType,
                minVoltageDropProportionalThreshold, withFortescueResult, subTransientCoefficient,
                withLoads, withShuntCompensators, withVSCConverterStations, withNeutralPosition,
                initialVoltageProfileMode, voltageRanges);
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
                ", initialVoltageProfileMode=" + initialVoltageProfileMode +
                ", voltageRanges=" + voltageRanges +
                '}';
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper().registerModule(new ShortCircuitAnalysisJsonModule());
    }

    /**
     * Writes a list of FaultParameters to a JSON file
     * @param parameters the list of FaultParameters
     * @param jsonFile the path to the JSON file
     */
    public static void write(List<FaultParameters> parameters, Path jsonFile) {
        try (OutputStream out = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, parameters);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads a JSON file and creates the associated list of FaultParameters
     * @param jsonFile the path to the JSON file
     * @return a list of FaultParameters
     */
    public static List<FaultParameters> read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return createObjectMapper().readerForListOf(FaultParameters.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Method used to validate FaultParameters. The voltage ranges should be defined if the parameter initialVoltageProfileMode is set to CONFIGURED.
     */
    public void validate() {
        if (initialVoltageProfileMode == InitialVoltageProfileMode.CONFIGURED && (voltageRanges == null || voltageRanges.isEmpty())) {
            throw new PowsyblException("Configured initial voltage profile but nominal voltage ranges with associated coefficients are missing.");
        }
    }
}
