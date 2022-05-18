/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.option;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.converter.ShortCircuitAnalysisJsonModule;

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

    private final String id;

    private final boolean withLimitViolations;

    private final boolean withVoltageMap;

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

    public FaultOptions(String id,
                        boolean withLimitViolations,
                        boolean withVoltageMap) {
        this.id = Objects.requireNonNull(id);
        this.withLimitViolations = withLimitViolations;
        this.withVoltageMap = withVoltageMap;
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
        return Objects.equals(id, that.id) &&
                Objects.equals(withLimitViolations, that.withLimitViolations) &&
                Objects.equals(withVoltageMap, that.withVoltageMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, withLimitViolations, withVoltageMap);
    }

    @Override
    public String toString() {
        return "FaultOptions{" +
            "id=" + id +
            "withLimitViolations=" + withLimitViolations +
            ", withVoltageMap=" + withVoltageMap +
            '}';
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper().registerModule(new ShortCircuitAnalysisJsonModule());
    }

    public static void write(List<FaultOptions> options, Path jsonFile) {
        try (OutputStream out = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<FaultOptions> read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return createObjectMapper().readerForListOf(FaultOptions.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
