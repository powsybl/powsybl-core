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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.powsybl.commons.io.ForwardingInputStream;
import com.powsybl.commons.util.ZipEntryOutputStream;

import net.java.truevfs.comp.zip.ZipEntry;
import net.java.truevfs.comp.zip.ZipFile;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class ZipFileDataStore implements DataStore {

    private final Path path;

    public ZipFileDataStore(Path path) {
        Objects.requireNonNull(path);
        this.path = path;
    }

    @Override
    public List<String> getEntryNames() throws IOException {

        if (Files.exists(path)) {
            try (ZipFile zipFile = new ZipFile(path)) {
                return Collections.list(zipFile.entries()).stream().map(ZipEntry::getName).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean exists(String entryName) {
        if (Files.exists(path)) {
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
        Objects.requireNonNull(entryName);

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

        return new ZipEntryOutputStream(path, entryName);
    }

    private static final class ZipEntryInputStream extends ForwardingInputStream<InputStream> {

        private final ZipFile zipFile;

        public ZipEntryInputStream(ZipFile zipFile, String fileName) throws IOException {
            super(zipFile.getInputStream(fileName));
            this.zipFile = zipFile;
        }

        @Override
        public void close() throws IOException {
            super.close();

            zipFile.close();
        }
    }

}
