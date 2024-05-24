/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 *  A state monitor allows to output in security analysis results some state variables related to branches, buses and three
 *  windings transformers. The supported state variables are active power, reactive power and current at both side for branches
 *  (see {@link com.powsybl.security.results.BranchResult}), active power, reactive power and current at network side for
 *  three windings transformers (see {@link com.powsybl.security.results.ThreeWindingsTransformerResult}) and voltage angle and voltage
 *  magnitude for buses (see {@link com.powsybl.security.results.BusResult}). A state monitor is defined in a contingency context.
 *  See {@link ContingencyContext} for more details.
 *  </p>
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class StateMonitor {

    /**
     * define on which situation information are needed
     */
    private final ContingencyContext contingencyContext;

    /**
     * branchs ids on which information will be collected
     */
    private final Set<String> branchIds = new LinkedHashSet<>();

    /**
     * voltageLevels ids on which information will be collected
     */
    private final Set<String> voltageLevelIds = new LinkedHashSet<>();

    /**
     * voltageLevels ids on which information will be collected
     */
    private final Set<String> threeWindingsTransformerIds = new LinkedHashSet<>();

    public ContingencyContext getContingencyContext() {
        return contingencyContext;
    }

    public Set<String> getBranchIds() {
        return branchIds;
    }

    public Set<String> getVoltageLevelIds() {
        return voltageLevelIds;
    }

    public Set<String> getThreeWindingsTransformerIds() {
        return threeWindingsTransformerIds;
    }

    public StateMonitor(@JsonProperty("contingencyContext") ContingencyContext contingencyContext,
                        @JsonProperty("branchIds") Set<String> branchIds, @JsonProperty("voltageLevelIds") Set<String> voltageLevelIds,
                        @JsonProperty("threeWindingsTransformerIds") Set<String> threeWindingsTransformerIds) {
        this.contingencyContext = Objects.requireNonNull(contingencyContext);
        this.branchIds.addAll(Objects.requireNonNull(branchIds));
        this.voltageLevelIds.addAll(Objects.requireNonNull(voltageLevelIds));
        this.threeWindingsTransformerIds.addAll(Objects.requireNonNull(threeWindingsTransformerIds));
    }

    public StateMonitor merge(StateMonitor monitorTobeMerged) {
        this.branchIds.addAll(monitorTobeMerged.getBranchIds());
        this.voltageLevelIds.addAll(monitorTobeMerged.getVoltageLevelIds());
        this.threeWindingsTransformerIds.addAll(monitorTobeMerged.getThreeWindingsTransformerIds());
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StateMonitor that = (StateMonitor) o;
        return Objects.equals(contingencyContext, that.contingencyContext) &&
            Objects.equals(branchIds, that.branchIds) &&
            Objects.equals(voltageLevelIds, that.voltageLevelIds) &&
            Objects.equals(threeWindingsTransformerIds, that.threeWindingsTransformerIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contingencyContext, branchIds, voltageLevelIds, threeWindingsTransformerIds);
    }

    @Override
    public String toString() {
        return "StateMonitor{" +
            "contingencyContext=" + contingencyContext +
            ", branchIds=" + branchIds +
            ", voltageLevelIds=" + voltageLevelIds +
            ", threeWindingsTransformerIds=" + threeWindingsTransformerIds +
            '}';
    }

    public static void write(List<StateMonitor> monitors, Path jsonFile) {
        try {
            OutputStream out = Files.newOutputStream(jsonFile);
            JsonUtil.createObjectMapper().writer().writeValue(out, monitors);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<StateMonitor> read(Path jsonFile) {
        try {
            return JsonUtil.createObjectMapper().readerFor(new TypeReference<List<StateMonitor>>() {
            }).readValue(Files.newInputStream(jsonFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
