/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitscaling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.limitscaling.LimitScalingList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class LimitScalingListSerDeUtil {

    private LimitScalingListSerDeUtil() {
    }

    /**
     * Read a limit reduction list from a JSON file.
     * @param jsonFile the JSON file containing the serialized limit reduction list.
     */
    public static LimitScalingList read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Read a limit reduction list from an output stream on a JSON.
     * @param is an input stream on the JSON serialized limit reduction list.
     */
    public static LimitScalingList read(InputStream is) {
        Objects.requireNonNull(is);
        ObjectMapper objectMapper = createObjectMapper();
        try {
            return objectMapper.readValue(is, LimitScalingList.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Write a limit reduction list as JSON to a file.
     * @param limitScalingList the reduction list to serialize
     * @param jsonFile a {@link Path} where to serialize the reduction list
     */
    public static void write(LimitScalingList limitScalingList, Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (OutputStream outputStream = Files.newOutputStream(jsonFile)) {
            write(limitScalingList, outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Write a limit reduction list as JSON to an output stream.
     * @param limitScalingList the reduction list to serialize
     * @param outputStream the output stream to use for the serialization
     */
    public static void write(LimitScalingList limitScalingList, OutputStream outputStream) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(outputStream, limitScalingList);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new LimitScalingModule());
    }
}
