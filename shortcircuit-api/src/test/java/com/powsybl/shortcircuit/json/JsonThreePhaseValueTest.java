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
import com.powsybl.shortcircuit.ThreePhaseValue;
import com.powsybl.shortcircuit.converter.ShortCircuitAnalysisJsonModule;
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
public class JsonThreePhaseValueTest extends AbstractConverterTest {

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper().registerModule(new ShortCircuitAnalysisJsonModule());
    }

    private static void write(List<ThreePhaseValue> values, Path jsonFile) {
        try (OutputStream out = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, values);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<ThreePhaseValue> read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return createObjectMapper().readerForListOf(ThreePhaseValue.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void roundTrip() throws IOException {
        List<ThreePhaseValue> values = new ArrayList<>();
        values.add(new ThreePhaseValue(1.0, 2.0, 3.0, 1.1, 2.1, 3.1, 1.2, 2.2, 3.2, 1.3, 2.3, 3.3));
        roundTripTest(values, JsonThreePhaseValueTest::write, JsonThreePhaseValueTest::read, "/ThreePhaseValue.json");
    }

    @Test
    public void readUnexpectedField() throws IOException {
        Files.copy(getClass().getResourceAsStream("/ThreePhaseValueInvalid.json"), fileSystem.getPath("/ThreePhaseValueInvalid.json"));

        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unexpected");
        read(fileSystem.getPath("/ThreePhaseValueInvalid.json"));
    }
}
