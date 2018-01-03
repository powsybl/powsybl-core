/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.commons.datasource.DataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppStorageDataSource implements DataSource {

    private static final String SEPARATOR = "__";

    public interface Name {

        static Name parse(String text) {
            Objects.requireNonNull(text);
            if (text.startsWith(SuffixAndExtension.START_PATTERN)) {
                int pos = text.indexOf(SEPARATOR, SuffixAndExtension.START_PATTERN.length());
                if (pos == -1) {
                    throw new IllegalStateException("Second separator not found");
                }
                String suffix = text.substring(SuffixAndExtension.START_PATTERN.length(), pos);
                String ext = text.substring(pos + SEPARATOR.length());
                return new SuffixAndExtension(suffix, ext);
            } else if (text.startsWith(FileName.START_PATTERN)) {
                String fileName = text.substring(FileName.START_PATTERN.length());
                return new FileName(fileName);
            } else {
                return null;
            }
        }
    }

    public static class SuffixAndExtension implements Name {

        static final String START_PATTERN = "DATA_SOURCE_SUFFIX_EXT" + SEPARATOR;

        private final String suffix;

        private final String ext;

        SuffixAndExtension(String suffix, String ext) {
            this.suffix = suffix;
            this.ext = ext;
        }

        public String getSuffix() {
            return suffix;
        }

        public String getExt() {
            return ext;
        }

        @Override
        public String toString() {
            return START_PATTERN + Objects.toString(suffix, "") + SEPARATOR + Objects.toString(ext, "");
        }
    }

    public static class FileName implements Name {

        static final String START_PATTERN = "DATA_SOURCE_FILE_NAME" + SEPARATOR;

        private final String name;

        FileName(String name) {
            this.name = Objects.requireNonNull(name);
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return START_PATTERN + name;
        }
    }

    private final AppStorage storage;

    private final String nodeId;

    public AppStorageDataSource(AppStorage storage, String nodeId) {
        this.storage = Objects.requireNonNull(storage);
        this.nodeId = Objects.requireNonNull(nodeId);
    }

    @Override
    public String getBaseName() {
        return "";
    }

    @Override
    public OutputStream newOutputStream(final String suffix, final String ext, boolean append) throws IOException {
        if (append) {
            throw new UnsupportedOperationException("Append mode not supported");
        }
        return storage.writeBinaryData(nodeId, new SuffixAndExtension(suffix, ext).toString());
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
        Objects.requireNonNull(fileName);
        if (append) {
            throw new UnsupportedOperationException("Append mode not supported");
        }
        return storage.writeBinaryData(nodeId, new FileName(fileName).toString());
    }

    @Override
    public boolean exists(String suffix, String ext) {
        return storage.dataExists(nodeId, new SuffixAndExtension(suffix, ext).toString());
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        return storage.dataExists(nodeId, new FileName(fileName).toString());
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return storage.readBinaryData(nodeId, new SuffixAndExtension(suffix, ext).toString())
                .orElseThrow(() -> new IOException("*" + Objects.toString(suffix, "") + "." + Objects.toString(ext, "") + " does not exist"));
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        return storage.readBinaryData(nodeId, new FileName(fileName).toString())
                .orElseThrow(() -> new IOException(fileName + " does not exist"));
    }
}
