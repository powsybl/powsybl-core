/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.iidm.network.Network;
import com.powsybl.sensitivity.json.SensitivityFactorsJsonSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class JsonSensitivityFactorsProvider implements SensitivityFactorsProvider {
    List<SensitivityFactor> sensitivityFactors;
    /**
     * Creates a provider by reading the sensitivity factors from a JSON UTF-8 encoded file.
     */
    public JsonSensitivityFactorsProvider(final Path path) {
        Objects.requireNonNull(path);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            sensitivityFactors = SensitivityFactorsJsonSerializer.read(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Creates a provider by reading the sensitivity factors from a JSON UTF-8 encoded input stream.
     */
    public JsonSensitivityFactorsProvider(final InputStream input) {
        Objects.requireNonNull(input);
        try {
            sensitivityFactors = SensitivityFactorsJsonSerializer.read(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    @Override
    public List<SensitivityFactor> getFactors(Network network) {
        return sensitivityFactors;
    }
}
