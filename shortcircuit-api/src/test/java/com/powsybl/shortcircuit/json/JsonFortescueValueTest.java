/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.shortcircuit.FortescueValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class JsonFortescueValueTest extends AbstractSerDeTest {

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper().registerModule(new ShortCircuitAnalysisJsonModule());
    }

    private static void write(List<FortescueValue> values, Path jsonFile) {
        try (OutputStream out = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, values);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<FortescueValue> read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return createObjectMapper().readerForListOf(FortescueValue.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void roundTrip() throws IOException {
        List<FortescueValue> values = new ArrayList<>();
        values.add(new FortescueValue(1.2, 2.2, 3.2, 1.3, 2.3, 3.3));
        roundTripTest(values, JsonFortescueValueTest::write, JsonFortescueValueTest::read, "/FortescueValue.json");
    }

    @Test
    void readUnexpectedField() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FortescueValueInvalid.json"), fileSystem.getPath("/FortescueValueInvalid.json"));

        Path path = fileSystem.getPath("/FortescueValueInvalid.json");
        UncheckedIOException e = assertThrows(UncheckedIOException.class, () -> read(path));
        assertEquals("com.fasterxml.jackson.databind.JsonMappingException: Unexpected field: unexpected (through reference chain: java.util.ArrayList[0])", e.getMessage());
    }
}
