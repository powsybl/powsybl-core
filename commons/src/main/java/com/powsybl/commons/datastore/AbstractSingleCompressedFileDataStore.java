/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.powsybl.commons.util.Filenames;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public abstract class AbstractSingleCompressedFileDataStore implements DataStore {

    private final Path path;
    private final String entryFilename;

    protected AbstractSingleCompressedFileDataStore(Path path) {
        this.path = Objects.requireNonNull(path);
        entryFilename = Filenames.getBasename(path.getFileName().toString());
    }

    public Path getPath() {
        return path;
    }

    public String getEntryFilename() {
        return entryFilename;
    }

    @Override
    public List<String> getEntryNames() throws IOException {
        return Collections.singletonList(entryFilename);
    }

    @Override
    public boolean exists(String entryName) {
        return entryFilename.equals(entryName);
    }

}
