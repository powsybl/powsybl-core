/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Set;

import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.loadflow.validation.ValidationType;
import com.powsybl.loadflow.validation.ValidationUtils;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ValidationWriters implements AutoCloseable {

    final EnumMap<ValidationType, Writer> writersMap = new EnumMap<>(ValidationType.class);
    final EnumMap<ValidationType, ValidationWriter> validationWritersMap = new EnumMap<>(ValidationType.class);

    public ValidationWriters(String networkId, Set<ValidationType> validationTypes, Path folder, ValidationConfig config) {
        validationTypes.forEach(validationType -> {
            try {
                Writer writer = Files.newBufferedWriter(validationType.getOutputFile(folder), StandardCharsets.UTF_8);
                writersMap.put(validationType, writer);
                validationWritersMap.put(validationType, ValidationUtils.createValidationWriter(networkId, config, writer, validationType));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public ValidationWriter getWriter(ValidationType validationType) {
        return validationWritersMap.get(validationType);
    }

    @Override
    public void close() throws Exception {
        validationWritersMap.values().forEach(validationWriter -> {
            try {
                validationWriter.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        writersMap.values().forEach(writer -> {
            try {
                writer.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

}
