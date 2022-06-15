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

import static com.powsybl.shortcircuit.ShortCircuitConstants.StartedGroups;
import static com.powsybl.shortcircuit.ShortCircuitConstants.VoltageMapType;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultParameters {

    private final String id;

    private final boolean withLimitViolations;

    private final boolean withVoltageMap;

    private final boolean withFeederResult;

    private final StudyType studyType;

    private double subTransStudyReactanceCoefficient;

    private final double minVoltageDropProportionalThreshold;

    private ShortCircuitConstants.VoltageMapType voltageMapType;

    private boolean useResistances;

    private boolean useLoads;

    private boolean useCapacities;

    private boolean useShunts;

    private boolean useTapChangers;

    private boolean useMutuals;

    private boolean modelVSC;

    private StartedGroups startedGroupsInsideZone;

    private double startedGroupsInsideZoneThreshold;

    private StartedGroups startedGroupsOutOfZone;

    private double startedGroupsOutOfZoneThreshold;

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

    public double getSubTransStudyReactanceCoefficient() {
        return subTransStudyReactanceCoefficient;
    }

    public VoltageMapType getVoltageMapType() {
        return voltageMapType;
    }

    public boolean isUseResistances() {
        return useResistances;
    }

    public boolean isUseLoads() {
        return useLoads;
    }

    public boolean isUseCapacities() {
        return useCapacities;
    }

    public boolean isUseShunts() {
        return useShunts;
    }

    public boolean isUseTapChangers() {
        return useTapChangers;
    }

    public boolean isUseMutuals() {
        return useMutuals;
    }

    public boolean isModelVSC() {
        return modelVSC;
    }

    public StartedGroups getStartedGroupsInsideZone() {
        return startedGroupsInsideZone;
    }

    public double getStartedGroupsInsideZoneThreshold() {
        return startedGroupsInsideZoneThreshold;
    }

    public StartedGroups getStartedGroupsOutOfZone() {
        return startedGroupsOutOfZone;
    }

    public double getStartedGroupsOutOfZoneThreshold() {
        return startedGroupsOutOfZoneThreshold;
    }

    public FaultParameters(String id, boolean withLimitViolations, boolean withVoltageMap, boolean withFeederResult, StudyType studyType,
                           double subTransStudyReactanceCoefficient, double minVoltageDropProportionalThreshold, VoltageMapType voltageMapType,
                           boolean useResistances, boolean useLoads, boolean useCapacities, boolean useShunts, boolean useTapChangers,
                           boolean useMutuals, boolean modelVSC, StartedGroups startedGroupsInsideZone, double startedGroupsInsideZoneThreshold,
                           StartedGroups startedGroupsOutOfZone, double startedGroupsOutOfZoneThreshold) {
        this.id = id;
        this.withLimitViolations = withLimitViolations;
        this.withVoltageMap = withVoltageMap;
        this.withFeederResult = withFeederResult;
        this.studyType = studyType;
        this.subTransStudyReactanceCoefficient = subTransStudyReactanceCoefficient;
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        this.voltageMapType = voltageMapType;
        this.useResistances = useResistances;
        this.useLoads = useLoads;
        this.useCapacities = useCapacities;
        this.useShunts = useShunts;
        this.useTapChangers = useTapChangers;
        this.useMutuals = useMutuals;
        this.modelVSC = modelVSC;
        this.startedGroupsInsideZone = startedGroupsInsideZone;
        this.startedGroupsInsideZoneThreshold = startedGroupsInsideZoneThreshold;
        this.startedGroupsOutOfZone = startedGroupsOutOfZone;
        this.startedGroupsOutOfZoneThreshold = startedGroupsOutOfZoneThreshold;
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
        return withLimitViolations == that.withLimitViolations && withVoltageMap == that.withVoltageMap && withFeederResult == that.withFeederResult &&
                Double.compare(that.subTransStudyReactanceCoefficient, subTransStudyReactanceCoefficient) == 0 &&
                Double.compare(that.minVoltageDropProportionalThreshold, minVoltageDropProportionalThreshold) == 0 &&
                useResistances == that.useResistances && useLoads == that.useLoads && useCapacities == that.useCapacities &&
                useShunts == that.useShunts && useTapChangers == that.useTapChangers && useMutuals == that.useMutuals && modelVSC == that.modelVSC &&
                Double.compare(that.startedGroupsInsideZoneThreshold, startedGroupsInsideZoneThreshold) == 0 &&
                Double.compare(that.startedGroupsOutOfZoneThreshold, startedGroupsOutOfZoneThreshold) == 0 && id.equals(that.id) &&
                studyType == that.studyType && voltageMapType == that.voltageMapType && startedGroupsInsideZone == that.startedGroupsInsideZone &&
                startedGroupsOutOfZone == that.startedGroupsOutOfZone;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, withLimitViolations, withVoltageMap, withFeederResult, studyType, subTransStudyReactanceCoefficient,
                minVoltageDropProportionalThreshold, voltageMapType, useResistances, useLoads, useCapacities, useShunts, useTapChangers,
                useMutuals, modelVSC, startedGroupsInsideZone, startedGroupsInsideZoneThreshold, startedGroupsOutOfZone, startedGroupsOutOfZoneThreshold);
    }

    @Override
    public String toString() {
        return "FaultParameters{" +
                "id='" + id + '\'' +
                ", withLimitViolations=" + withLimitViolations +
                ", withVoltageMap=" + withVoltageMap +
                ", withFeederResult=" + withFeederResult +
                ", studyType=" + studyType +
                ", subTransStudyReactanceCoefficient=" + subTransStudyReactanceCoefficient +
                ", minVoltageDropProportionalThreshold=" + minVoltageDropProportionalThreshold +
                ", voltageMapType=" + voltageMapType +
                ", useResistances=" + useResistances +
                ", useLoads=" + useLoads +
                ", useCapacities=" + useCapacities +
                ", useShunts=" + useShunts +
                ", useTapChangers=" + useTapChangers +
                ", useMutuals=" + useMutuals +
                ", modelVSC=" + modelVSC +
                ", startedGroupsInsideZone=" + startedGroupsInsideZone +
                ", startedGroupsInsideZoneThreshold=" + startedGroupsInsideZoneThreshold +
                ", startedGroupsOutOfZone=" + startedGroupsOutOfZone +
                ", startedGroupsOutOfZoneThreshold=" + startedGroupsOutOfZoneThreshold +
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
