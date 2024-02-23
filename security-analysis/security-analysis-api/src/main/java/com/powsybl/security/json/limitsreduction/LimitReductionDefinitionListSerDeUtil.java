/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitsreduction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.limitsreduction.LimitReductionDefinitionList;

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
public final class LimitReductionDefinitionListSerDeUtil {

    private LimitReductionDefinitionListSerDeUtil() {
    }

    /**
     * Read a limit reduction definition list from a JSON file.
     * @param jsonFile the JSON file containing the serialized limit reduction definition list.
     */
    public static LimitReductionDefinitionList read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Read a limit reduction definition list from an output stream on a JSON.
     * @param is an input stream on the JSON serialized limit reduction definition list.
     */
    public static LimitReductionDefinitionList read(InputStream is) {
        Objects.requireNonNull(is);
        ObjectMapper objectMapper = createObjectMapper();
        try {
            return objectMapper.readValue(is, LimitReductionDefinitionList.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Write a limit reduction definition list as JSON to a file.
     * @param limitReductionDefinitionList the definition list to serialize
     * @param jsonFile a {@link Path} where to serialize the definition list
     */
    public static void write(LimitReductionDefinitionList limitReductionDefinitionList, Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (OutputStream outputStream = Files.newOutputStream(jsonFile)) {
            write(limitReductionDefinitionList, outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Write a limit reduction definition list as JSON to an output stream.
     * @param limitReductionDefinitionList the definition list to serialize
     * @param outputStream the output stream to use for the serialization
     */
    public static void write(LimitReductionDefinitionList limitReductionDefinitionList, OutputStream outputStream) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(outputStream, limitReductionDefinitionList);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new LimitReductionModule());
    }
}
