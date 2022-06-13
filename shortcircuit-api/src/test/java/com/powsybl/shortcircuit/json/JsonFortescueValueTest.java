/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.FortescueValue;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class JsonFortescueValueTest extends AbstractConverterTest {

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
    public void roundTrip() throws IOException {
        List<FortescueValue> values = new ArrayList<>();
        values.add(new FortescueValue(1.2, 2.2, 3.2, 1.3, 2.3, 3.3));
        roundTripTest(values, JsonFortescueValueTest::write, JsonFortescueValueTest::read, "/FortescueValue.json");
    }

    @Test
    public void readUnexpectedField() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FortescueValueInvalid.json"), fileSystem.getPath("/FortescueValueInvalid.json"));

        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unexpected");
        read(fileSystem.getPath("/FortescueValueInvalid.json"));
    }
}
