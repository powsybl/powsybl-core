/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.Fault;
import com.powsybl.shortcircuit.converter.ShortCircuitAnalysisJsonModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *  @author Thomas Adam <tadam at silicom.fr>
 */
public final class JsonShortCircuitInput {

    private JsonShortCircuitInput() {
    }

    public static List<Fault> update(List<Fault> input, Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            return update(input, is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<Fault> update(List<Fault> input, InputStream is) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            input.addAll(objectMapper.readerForListOf(Fault.class).readValue(is));
            return input;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper().registerModule(new ShortCircuitAnalysisJsonModule());
    }

    public static void write(List<Fault> input, Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (OutputStream outputStream = Files.newOutputStream(jsonFile)) {
            write(input, outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void write(List<Fault> input, OutputStream outputStream) {
        try {
            ObjectMapper objectMapper = createObjectMapper();
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter().forType(new TypeReference<List<Fault>>() { });
            writer.writeValue(outputStream, input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<Fault> read(Path jsonFile) {
        return update(new ArrayList<>(), jsonFile);
    }

    public static List<Fault> read(InputStream jsonStream) {
        return update(new ArrayList<>(), jsonStream);
    }
}
