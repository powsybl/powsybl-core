/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.sensitivity.SensitivityFactor;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class JsonSensitivityFactorsTest extends AbstractConverterTest {

    private static List<SensitivityFactor> create() throws IOException {
        return SensitivityFactorsJsonSerializer.read(new InputStreamReader(JsonSensitivityFactorsTest.class.getResourceAsStream("/sensitivityFactorsExample.json")));
    }

    private static List<SensitivityFactor> read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            return SensitivityFactorsJsonSerializer.read(new InputStreamReader(is));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(List<SensitivityFactor> cracFile, Path jsonFile) {
        Objects.requireNonNull(cracFile);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            SensitivityFactorsJsonSerializer.write(cracFile, new OutputStreamWriter(os));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), JsonSensitivityFactorsTest::write, JsonSensitivityFactorsTest::read, "/sensitivityFactorsExample.json");
    }
}
