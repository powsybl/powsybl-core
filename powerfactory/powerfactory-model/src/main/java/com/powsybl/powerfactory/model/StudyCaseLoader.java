/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface StudyCaseLoader {

    static Optional<StudyCase> load(Path file) {
        Objects.requireNonNull(file);
        return load(file.getFileName().toString(), () -> {
            try {
                return Files.newInputStream(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    static Optional<StudyCase> load(String fileName, Supplier<InputStream> inputStreamSupplier) {
        return load(fileName, inputStreamSupplier, Lists.newArrayList(ServiceLoader.load(StudyCaseLoader.class)));
    }

    static Optional<StudyCase> load(String fileName, Supplier<InputStream> inputStreamSupplier,
                                    Collection<StudyCaseLoader> studyCaseLoaders) {
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(inputStreamSupplier);
        Objects.requireNonNull(studyCaseLoaders);
        for (StudyCaseLoader studyCaseLoader : studyCaseLoaders) {
            if (fileName.endsWith(studyCaseLoader.getExtension()) &&
                    studyCaseLoader.test(inputStreamSupplier.get())) {
                return Optional.of(studyCaseLoader.doLoad(fileName, inputStreamSupplier.get()));
            }
        }
        return Optional.empty();
    }

    String getExtension();

    boolean test(InputStream is);

    StudyCase doLoad(String fileName, InputStream is);
}
