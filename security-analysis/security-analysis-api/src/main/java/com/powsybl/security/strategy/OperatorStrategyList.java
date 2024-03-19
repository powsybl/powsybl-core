/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableList;
import com.powsybl.action.json.ActionJsonModule;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.json.SecurityAnalysisJsonModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * A container for a list of strategies.
 * The JSON format of this container is versioned.
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategyList {
    private final List<OperatorStrategy> operatorStrategies;
    public static final String VERSION = "1.1";

    public OperatorStrategyList(List<OperatorStrategy> operatorStrategies) {
        this.operatorStrategies = ImmutableList.copyOf(Objects.requireNonNull(operatorStrategies));
    }

    public List<OperatorStrategy> getOperatorStrategies() {
        return operatorStrategies;
    }

    public static OperatorStrategyList read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static OperatorStrategyList read(InputStream is) {
        Objects.requireNonNull(is);

        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule())
                .registerModule(new ActionJsonModule());
        try {
            return objectMapper.readValue(is, OperatorStrategyList.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Writes action list as JSON to a file.
     */
    public void write(Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (OutputStream outputStream = Files.newOutputStream(jsonFile)) {
            write(outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Writes action list as JSON to an output stream.
     */
    public void write(OutputStream outputStream) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(outputStream, this);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule())
                .registerModule(new ActionJsonModule());
    }
}
