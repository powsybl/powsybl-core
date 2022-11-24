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
import java.util.List;
import java.util.Objects;

import static com.powsybl.shortcircuit.ShortCircuitConstants.InitialNominalVoltageProfileType;
import static com.powsybl.shortcircuit.ShortCircuitConstants.InitialVoltageProfileType;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultParameters {

    // VERSION = 1.0 withLimitViolations, withVoltageMap, withFeederResult, studyType and minVoltageDropProportionalThreshold
    // VERSION = 1.1 initialVoltageProfileType, initialNominalVoltageProfileType, withVoltageMap -> withVoltageProfileResult
    public static final String VERSION = "1.1";

    private final String id;

    private final boolean withLimitViolations;

    private final boolean withVoltageProfileResult;

    private final boolean withFeederResult;

    private final StudyType studyType;

    private final double minVoltageDropProportionalThreshold;

    private final InitialVoltageProfileType initialVoltageProfileType;

    private final InitialNominalVoltageProfileType initialNominalVoltageProfileType;

    /** Fault id */
    public String getId() {
        return id;
    }

    /** Override general parameter withLimitViolations from {@link ShortCircuitParameters} */
    public boolean isWithLimitViolations() {
        return withLimitViolations;
    }

    /** Override general parameter withVoltageProfileResult from {@link ShortCircuitParameters} */
    public boolean isWithVoltageProfileResult() {
        return withVoltageProfileResult;
    }

    /** Override general parameter withFeederResult from {@link ShortCircuitParameters} */
    public boolean isWithFeederResult() {
        return withFeederResult;
    }

    /** Override general parameter studyType from {@link ShortCircuitParameters} */
    public StudyType getStudyType() {
        return studyType;
    }

    /** Override general parameter minVoltageDropProportionalThreshold from {@link ShortCircuitParameters} */
    public double getMinVoltageDropProportionalThreshold() {
        return minVoltageDropProportionalThreshold;
    }

    /** Override general parameter initialVoltageProfileType from {@link ShortCircuitParameters} */
    public InitialVoltageProfileType getInitialVoltageProfileType() {
        return initialVoltageProfileType;
    }

    /** Override general parameter initialNominalVoltageProfileType from {@link ShortCircuitParameters} */
    public ShortCircuitConstants.InitialNominalVoltageProfileType getInitialNominalVoltageProfileType() {
        return initialNominalVoltageProfileType;
    }

    public FaultParameters(String id,
                           boolean withLimitViolations,
                           boolean withVoltageProfileResult,
                           boolean withFeederResult,
                           StudyType studyType,
                           double minVoltageDropProportionalThreshold,
                           InitialVoltageProfileType initialVoltageProfileType,
                           InitialNominalVoltageProfileType initialNominalVoltageProfileType) {
        this.id = Objects.requireNonNull(id);
        this.withLimitViolations = withLimitViolations;
        this.withVoltageProfileResult = withVoltageProfileResult;
        this.withFeederResult = withFeederResult;
        this.studyType = studyType;
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        this.initialVoltageProfileType = initialVoltageProfileType;
        this.initialNominalVoltageProfileType = initialNominalVoltageProfileType;
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
                Objects.equals(withVoltageProfileResult, that.withVoltageProfileResult) &&
                Objects.equals(withFeederResult, that.withFeederResult) &&
                Objects.equals(studyType, that.studyType) &&
                Objects.equals(minVoltageDropProportionalThreshold, that.minVoltageDropProportionalThreshold) &&
                Objects.equals(initialVoltageProfileType, that.initialVoltageProfileType) &&
                Objects.equals(initialNominalVoltageProfileType, that.initialNominalVoltageProfileType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, withLimitViolations, withVoltageProfileResult, withFeederResult, studyType, minVoltageDropProportionalThreshold,
                initialVoltageProfileType, initialNominalVoltageProfileType);
    }

    @Override
    public String toString() {
        return "FaultParameters{" +
                "id=" + id +
                ", withLimitViolations=" + withLimitViolations +
                ", withVoltageProfileResult=" + withVoltageProfileResult +
                ", withFeederResult=" + withFeederResult +
                ", studyType=" + studyType +
                ", minVoltageDropProportionalThreshold=" + minVoltageDropProportionalThreshold +
                ", initialVoltageProfileType=" + initialVoltageProfileType +
                ", initialNominalVoltageProfileType=" + initialNominalVoltageProfileType +
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
