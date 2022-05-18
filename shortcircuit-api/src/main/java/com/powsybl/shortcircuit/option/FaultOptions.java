/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.option;

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
public class FaultOptions {

    private final FaultContext faultContext;

    private boolean withLimitViolations;

    private boolean withVoltageMap;

    public FaultContext getFaultContext() {
        return faultContext;
    }

    /** Whether the result should indicate a limit violation */
    public boolean isWithLimitViolations() {
        return withLimitViolations;
    }

    /** Whether the results should include the voltage map on the whole network */
    public boolean isWithVoltageMap() {
        return withVoltageMap;
    }

    public FaultOptions(@JsonProperty("faultContext") FaultContext faultContext,
                        @JsonProperty("withLimitViolations") boolean withLimitViolations,
                        @JsonProperty("withVoltageMap") boolean withVoltageMap) {
        this.faultContext = Objects.requireNonNull(faultContext);
        this.withLimitViolations = withLimitViolations;
        this.withVoltageMap = withVoltageMap;
    }

    public FaultOptions merge(FaultOptions optionTobeMerged) {
        Objects.requireNonNull(optionTobeMerged);
        this.withLimitViolations = optionTobeMerged.isWithLimitViolations();
        this.withVoltageMap = optionTobeMerged.isWithVoltageMap();
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
        FaultOptions that = (FaultOptions) o;
        return Objects.equals(withLimitViolations, that.withLimitViolations) &&
                Objects.equals(withVoltageMap, that.withVoltageMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(withLimitViolations, withVoltageMap);
    }

    @Override
    public String toString() {
        return "FaultOptions{" +
            "withLimitViolations=" + withLimitViolations +
            ", withVoltageMap=" + withVoltageMap +
            '}';
    }

    public static void write(List<FaultOptions> options, Path jsonFile) {
        try (OutputStream out = Files.newOutputStream(jsonFile)) {
            JsonUtil.createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<FaultOptions> read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return JsonUtil.createObjectMapper().readerForListOf(FaultOptions.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
