/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface PowerFactoryDataLoader<T extends PowerFactoryData> {

    @SuppressWarnings("unchecked")
    static <T extends PowerFactoryData> List<PowerFactoryDataLoader<T>> find(Class<T> dataClass) {
        Objects.requireNonNull(dataClass);
        return ServiceLoader.load(PowerFactoryDataLoader.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(loader -> loader.getDataClass() == dataClass)
                .map(loader -> (PowerFactoryDataLoader<T>) loader)
                .collect(Collectors.toList());
    }

    static <T extends PowerFactoryData> Optional<T> load(Path file, Class<T> dataClass) {
        Objects.requireNonNull(file);
        return load(file.getFileName().toString(), () -> {
            try {
                return Files.newInputStream(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, dataClass);
    }

    static <T extends PowerFactoryData> Optional<T> load(String fileName, Supplier<InputStream> inputStreamSupplier,
                                                        Class<T> dataClass) {
        return load(fileName, inputStreamSupplier, dataClass, find(dataClass));
    }

    static <T extends PowerFactoryData> Optional<T> load(String fileName, Supplier<InputStream> inputStreamSupplier,
                                                         Class<T> dataClass, List<PowerFactoryDataLoader<T>> dataLoaders) {
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(inputStreamSupplier);
        Objects.requireNonNull(dataLoaders);
        for (PowerFactoryDataLoader<T> dataLoader : dataLoaders) {
            if (dataLoader.getDataClass() == dataClass && fileName.endsWith(dataLoader.getExtension())) {
                try (var testIs = inputStreamSupplier.get()) {
                    if (dataLoader.test(testIs)) {
                        try (var loadIs = inputStreamSupplier.get()) {
                            return Optional.of(dataLoader.doLoad(fileName, loadIs));
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return Optional.empty();
    }

    Class<T> getDataClass();

    String getExtension();

    boolean test(InputStream is);

    T doLoad(String fileName, InputStream is);
}
