/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.contingency.ContingencyContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class ContingencyContextJsonTest extends AbstractSerDeTest {
    private static final ObjectMapper MAPPER = JsonUtil.createObjectMapper();
    private static final ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();

    @Test
    void roundTripTest() throws IOException {
        List<ContingencyContext> contexts = List.of(ContingencyContext.none(),
                ContingencyContext.all(),
                ContingencyContext.onlyContingencies(),
                ContingencyContext.specificContingency("lineId"));
        roundTripTest(contexts, ContingencyContextJsonTest::writeContexts,
                ContingencyContextJsonTest::readContingencyContexts,
                "/contingencyContexts.json");
    }

    private static List<ContingencyContext> readContingencyContexts(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return MAPPER.readerForListOf(ContingencyContext.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeContexts(List<ContingencyContext> contexts, Path path) {
        write(contexts, path);
    }

    private static <T> void write(T object, Path jsonFile) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(jsonFile);
        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            WRITER.writeValue(os, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
