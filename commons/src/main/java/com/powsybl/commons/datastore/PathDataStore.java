/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class PathDataStore implements DataStore {

    private final Path path;

    public PathDataStore(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    @Override
    public List<String> getEntryNames() throws IOException {
        try (Stream<Path> files = Files.list(path)) {
            return files.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public InputStream newInputStream(String entryName) throws IOException {
        return Files.newInputStream(path.resolve(entryName));
    }

    @Override
    public OutputStream newOutputStream(String entryName) throws IOException {
        return Files.newOutputStream(path.resolve(entryName));
    }

    @Override
    public boolean exists(String entryName) {
        return Files.exists(path.resolve(entryName));
    }

}
