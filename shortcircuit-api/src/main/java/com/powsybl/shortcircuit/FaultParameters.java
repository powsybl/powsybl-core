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

    private final String id;

    private final boolean withLimitViolations;

    private final boolean withVoltageMap;

    private final boolean withFeederResult;

    private final StudyType studyType;

    private final double minVoltageDropProportionalThreshold;

    private final boolean useResistances;

    private final boolean useLoads;

    private final VoltageMapType voltageMapType;

    private final NominalVoltageMapType nominalVoltageMapType;

    /** Fault id */
    public String getId() {
        return id;
    }

    /** Whether the result should indicate a limit violation */
    public boolean isWithLimitViolations() {
        return withLimitViolations;
    }

    /** Whether the results should include the voltage map on the whole network */
    public boolean isWithVoltageMap() {
        return withVoltageMap;
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

    /** Override general parameter useResistances from {@link ShortCircuitParameters} */
    public boolean isUseResistances() {
        return useResistances;
    }

    /** Override general parameter useLoads from {@link ShortCircuitParameters} */
    public boolean isUseLoads() {
        return useLoads;
    }

    /** Override general parameter voltageMapType from {@link ShortCircuitParameters} */
    public VoltageMapType getVoltageMapType() {
        return voltageMapType;
    }

    /** Override general parameter nominalVoltageMapType from {@link ShortCircuitParameters} */
    public NominalVoltageMapType getNominalVoltageMapType() {
        return nominalVoltageMapType;
    }

    public FaultParameters(String id,
                           boolean withLimitViolations,
                           boolean withVoltageMap,
                           boolean withFeederResult,
                           StudyType studyType,
                           double minVoltageDropProportionalThreshold,
                           boolean useResistances,
                           boolean useLoads,
                           VoltageMapType voltageMapType,
                           NominalVoltageMapType nominalVoltageMapType) {
        this.id = Objects.requireNonNull(id);
        this.withLimitViolations = withLimitViolations;
        this.withVoltageMap = withVoltageMap;
        this.withFeederResult = withFeederResult;
        this.studyType = studyType;
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        this.useResistances = useResistances;
        this.useLoads = useLoads;
        this.voltageMapType = voltageMapType;
        this.nominalVoltageMapType = nominalVoltageMapType;
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
                Objects.equals(withVoltageMap, that.withVoltageMap) &&
                Objects.equals(withFeederResult, that.withFeederResult) &&
                Objects.equals(studyType, that.studyType) &&
                Objects.equals(minVoltageDropProportionalThreshold, that.minVoltageDropProportionalThreshold) &&
                Objects.equals(useResistances, that.useResistances) &&
                Objects.equals(useLoads, that.useLoads) &&
                Objects.equals(voltageMapType, that.voltageMapType) &&
                Objects.equals(nominalVoltageMapType, that.nominalVoltageMapType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, withLimitViolations, withVoltageMap, withFeederResult, studyType, minVoltageDropProportionalThreshold, useResistances,
                useLoads, voltageMapType, nominalVoltageMapType);
    }

    @Override
    public String toString() {
        return "FaultParameters{" +
                "id=" + id +
                ", withLimitViolations=" + withLimitViolations +
                ", withVoltageMap=" + withVoltageMap +
                ", withFeederResult=" + withFeederResult +
                ", studyType=" + studyType +
                ", minVoltageDropProportionalThreshold=" + minVoltageDropProportionalThreshold +
                ", useResistances=" + useResistances +
                ", useLoads=" + useLoads +
                ", voltageMapType=" + voltageMapType +
                ", nominalVoltageMapType=" + nominalVoltageMapType +
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
