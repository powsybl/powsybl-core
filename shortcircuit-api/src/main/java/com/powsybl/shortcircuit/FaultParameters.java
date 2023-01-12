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

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultParameters {

    // VERSION = 1.0 withLimitViolations, withVoltageMap, withFeederResult, studyType and minVoltageDropProportionalThreshold
    // VERSION = 1.1 withVoltageMap -> withSimpleResult and withVoltageAndVoltageDropProfileResult
    public static final String VERSION = "1.1";

    private final String id;

    private final boolean withLimitViolations;

    private final boolean withSimpleResult;

    private final StudyType studyType;

    private final boolean withFeederResult;

    private final boolean withVoltageAndVoltageDropProfileResult;

    private final double minVoltageDropProportionalThreshold;

    /** Fault id */
    public String getId() {
        return id;
    }

    /** Override general parameter withLimitViolations from {@link ShortCircuitParameters} */
    public boolean isWithLimitViolations() {
        return withLimitViolations;
    }

    /** Override general parameter withSimpleResult from {@link ShortCircuitParameters} */
    public boolean isWithSimpleResult() {
        return withSimpleResult;
    }

    /** Override general parameter studyType from {@link ShortCircuitParameters} */
    public StudyType getStudyType() {
        return studyType;
    }

    /** Override general parameter withFeederResult from {@link ShortCircuitParameters} */
    public boolean isWithFeederResult() {
        return withFeederResult;
    }

    /** Override general parameter withVoltageAndVoltageDropProfileResult from {@link ShortCircuitParameters} */
    public boolean isWithVoltageAndVoltageDropProfileResult() {
        return withVoltageAndVoltageDropProfileResult;
    }

    /** Override general parameter minVoltageDropProportionalThreshold from {@link ShortCircuitParameters} */
    public double getMinVoltageDropProportionalThreshold() {
        return minVoltageDropProportionalThreshold;
    }

    public FaultParameters(String id,
                           boolean withLimitViolations,
                           boolean withVoltageAndVoltageDropProfileResult,
                           boolean withFeederResult,
                           StudyType studyType,
                           double minVoltageDropProportionalThreshold,
                           boolean withSimpleResult) {
        this.id = Objects.requireNonNull(id);
        this.withLimitViolations = withLimitViolations;
        this.withVoltageAndVoltageDropProfileResult = withVoltageAndVoltageDropProfileResult;
        this.withFeederResult = withFeederResult;
        this.studyType = studyType;
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        this.withSimpleResult = withSimpleResult;
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
                Objects.equals(withVoltageAndVoltageDropProfileResult, that.withVoltageAndVoltageDropProfileResult) &&
                Objects.equals(withFeederResult, that.withFeederResult) &&
                Objects.equals(studyType, that.studyType) &&
                Objects.equals(minVoltageDropProportionalThreshold, that.minVoltageDropProportionalThreshold) &&
                Objects.equals(withSimpleResult, that.withSimpleResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, withLimitViolations, withVoltageAndVoltageDropProfileResult, withFeederResult, studyType, minVoltageDropProportionalThreshold, withSimpleResult);
    }

    @Override
    public String toString() {
        return "FaultParameters{" +
                "id=" + id +
                ", withLimitViolations=" + withLimitViolations +
                ", withVoltageAndVoltageDropProfileResult=" + withVoltageAndVoltageDropProfileResult +
                ", withFeederResult=" + withFeederResult +
                ", studyType=" + studyType +
                ", minVoltageDropProportionalThreshold=" + minVoltageDropProportionalThreshold +
                ", withSimpleResult=" + withSimpleResult +
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
