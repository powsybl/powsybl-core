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

import static com.powsybl.shortcircuit.ShortCircuitConstants.NominalVoltageMapType;
import static com.powsybl.shortcircuit.ShortCircuitConstants.VoltageMapType;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultParameters {

    // VERSION = 1.0 withLimitViolations, withVoltageMap, withFeederResult, studyType and minVoltageDropProportionalThreshold
    // VERSION = 1.1 voltageMapType, nominalVoltageMapType, useResistances, useLoads, useCapacities, useShunts
    public static final String VERSION = "1.1";

    private final String id;

    private final boolean withLimitViolations;

    private final boolean withVoltageMap;

    private final boolean withFeederResult;

    private final StudyType studyType;

    private final double minVoltageDropProportionalThreshold;

    private final ShortCircuitConstants.VoltageMapType voltageMapType;

    private final ShortCircuitConstants.NominalVoltageMapType nominalVoltageMapType;

    private final boolean useResistances;

    private final boolean useLoads;

    private final boolean useCapacities;

    private final boolean useShunts;

    /** Fault id */
    public String getId() {
        return id;
    }

    /** Override general parameter withLimitViolations from {@link ShortCircuitParameters} */
    public boolean isWithLimitViolations() {
        return withLimitViolations;
    }

    /** Override general parameter withVoltageMap from {@link ShortCircuitParameters} */
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

    /** Override general parameter voltageMapType from {@link ShortCircuitParameters} */
    public VoltageMapType getVoltageMapType() {
        return voltageMapType;
    }

    /** Override general parameter nominalVoltageMapType from {@link ShortCircuitParameters} */
    public NominalVoltageMapType getNominalVoltageMapType() {
        return nominalVoltageMapType;
    }

    /** Override general parameter useResistances from {@link ShortCircuitParameters} */
    public boolean isUseResistances() {
        return useResistances;
    }

    /** Override general parameter useLoads from {@link ShortCircuitParameters} */
    public boolean isUseLoads() {
        return useLoads;
    }

    /** Override general parameter useCapacities from {@link ShortCircuitParameters} */
    public boolean isUseCapacities() {
        return useCapacities;
    }

    /** Override general parameter useShunts from {@link ShortCircuitParameters} */
    public boolean isUseShunts() {
        return useShunts;
    }

    public FaultParameters(String id,
                           boolean withLimitViolations,
                           boolean withVoltageMap,
                           boolean withFeederResult,
                           StudyType studyType,
                           double minVoltageDropProportionalThreshold,
                           VoltageMapType voltageMapType,
                           NominalVoltageMapType nominalVoltageMapType,
                           boolean useResistances,
                           boolean useLoads,
                           boolean useCapacities,
                           boolean useShunts) {
        this.id = Objects.requireNonNull(id);
        this.withLimitViolations = withLimitViolations;
        this.withVoltageMap = withVoltageMap;
        this.withFeederResult = withFeederResult;
        this.studyType = studyType;
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        this.voltageMapType = voltageMapType;
        this.nominalVoltageMapType = nominalVoltageMapType;
        this.useResistances = useResistances;
        this.useLoads = useLoads;
        this.useCapacities = useCapacities;
        this.useShunts = useShunts;
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
                Objects.equals(voltageMapType, that.voltageMapType) &&
                Objects.equals(nominalVoltageMapType, that.nominalVoltageMapType) &&
                Objects.equals(useResistances, that.useResistances) &&
                Objects.equals(useLoads, that.useLoads) &&
                Objects.equals(useCapacities, that.useCapacities) &&
                Objects.equals(useShunts, that.useShunts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, withLimitViolations, withVoltageMap, withFeederResult, studyType, minVoltageDropProportionalThreshold,
                voltageMapType, nominalVoltageMapType, useResistances, useLoads, useCapacities, useShunts);
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
                ", voltageMapType=" + voltageMapType +
                ", nominalVoltageMapType=" + nominalVoltageMapType +
                ", useResistances=" + useResistances +
                ", useLoads=" + useLoads +
                ", useCapacities=" + useCapacities +
                ", useShunts=" + useShunts +
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
