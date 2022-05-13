/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.json.JsonUtil;

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
public class StateMonitor {

    private final FaultContext faultContext;

    // Whether the result should indicate a limit violation
    private boolean withLimitViolations;

    // Whether the results should include the voltage map on the whole network
    private boolean withVoltageMap;

    public FaultContext getFaultContext() {
        return faultContext;
    }

    public boolean isWithLimitViolations() {
        return withLimitViolations;
    }

    public boolean isWithVoltageMap() {
        return withVoltageMap;
    }

    public StateMonitor(@JsonProperty("faultContext") FaultContext faultContext,
                        @JsonProperty("withLimitViolations") boolean withLimitViolations,
                        @JsonProperty("withVoltageMap") boolean withVoltageMap) {
        this.faultContext = Objects.requireNonNull(faultContext);
        this.withLimitViolations = withLimitViolations;
        this.withVoltageMap = withVoltageMap;
    }

    public StateMonitor merge(StateMonitor monitorTobeMerged) {
        Objects.requireNonNull(monitorTobeMerged);
        this.withLimitViolations = monitorTobeMerged.isWithLimitViolations();
        this.withVoltageMap = monitorTobeMerged.isWithVoltageMap();
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
        return Objects.equals(withLimitViolations, that.withLimitViolations) &&
                Objects.equals(withVoltageMap, that.withVoltageMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(withLimitViolations, withVoltageMap);
    }

    @Override
    public String toString() {
        return "StateMonitor{" +
            "withLimitViolations=" + withLimitViolations +
            ", withVoltageMap=" + withVoltageMap +
            '}';
    }

    public static void write(List<StateMonitor> monitors, Path jsonFile) {
        try (OutputStream out = Files.newOutputStream(jsonFile)) {
            JsonUtil.createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, monitors);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<StateMonitor> read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return JsonUtil.createObjectMapper().readerForListOf(StateMonitor.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
