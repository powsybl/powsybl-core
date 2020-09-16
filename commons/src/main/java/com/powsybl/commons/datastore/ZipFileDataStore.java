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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Iterators;
import com.powsybl.commons.util.ZipEntryInputStream;
import com.powsybl.commons.util.ZipEntryOutputStream;

import net.java.truevfs.comp.zip.ZipFile;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class ZipFileDataStore implements DataStore {

    private final Path path;

    public ZipFileDataStore(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    @Override
    public List<String> getEntryNames() throws IOException {

        if (Files.exists(path)) {
            try (ZipFile zipFile = new ZipFile(path)) {
                List<String> list = new ArrayList<>();
                Iterators.forEnumeration(zipFile.entries()).forEachRemaining(e -> list.add(e.getName()));
                return Collections.unmodifiableList(list);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean exists(String entryName) {
        if (entryName != null && Files.exists(path)) {
            try (ZipFile zipFile = new ZipFile(path)) {
                return zipFile.entry(entryName) != null;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public InputStream newInputStream(String entryName) throws IOException {
        if (exists(entryName)) {
            return new ZipEntryInputStream(new ZipFile(path), entryName);
        }
        return null;
    }

    @Override
    public OutputStream newOutputStream(String entryName, boolean append) throws IOException {
        Objects.requireNonNull(entryName);
        if (append) {
            throw new UnsupportedOperationException("append not supported in zip file data source");
        }

        return new ZipEntryOutputStream(path, entryName, exists(entryName));
    }

}
