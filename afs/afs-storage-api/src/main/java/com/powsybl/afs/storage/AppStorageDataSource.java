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
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A datasource corresponding to a data blob stored in the file system.
 * A data blob is associated to a node and a name identifying it among data blobs of this node.
 *
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

    private final String nodeName;

    public AppStorageDataSource(AppStorage storage, String nodeId, String nodeName) {
        this.storage = Objects.requireNonNull(storage);
        this.nodeId = Objects.requireNonNull(nodeId);
        this.nodeName = Objects.requireNonNull(nodeName);
    }

    @Override
    public String getMainFileName() {
        return null;
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) {
        Objects.requireNonNull(fileName);
        if (append) {
            throw new UnsupportedOperationException("Append mode not supported");
        }
        return storage.writeBinaryData(nodeId, fileName);
    }

    @Override
    public boolean fileExists(String fileName) {
        Objects.requireNonNull(fileName);
        if (storage.dataExists(nodeId, fileName)) {
            return true;
        }
        // backward compatibility
        return getFileNameStream().anyMatch(fileName2 -> fileName2.equals(fileName));
    }

    @Override
    public InputStream newInputStream(String fileName) {
        Objects.requireNonNull(fileName);
        return storage.readBinaryData(nodeId, fileName)
                .orElseGet(() -> {
                    // for backward compatibility
                    for (String dataName : storage.getDataNames(nodeId)) {
                        String fileName2 = migrateDataName(dataName);
                        if (fileName.equals(fileName2)) {
                            return storage.readBinaryData(nodeId, dataName).orElseThrow(AssertionError::new);
                        }
                    }
                    throw new UncheckedIOException(new IOException(fileName + " does not exist"));
                });
    }

    private String migrateDataName(String dataName) {
        Name name = Name.parse(dataName);
        if (name == null) {
            return dataName;
        }
        // for backward compatibility
        if (name instanceof FileName) {
            return ((FileName) name).getName();
        } else if (name instanceof SuffixAndExtension) {
            SuffixAndExtension suffixAndExtension = (SuffixAndExtension) name;
            return nodeName + Objects.toString(suffixAndExtension.getSuffix(), "") + "." + Objects.toString(suffixAndExtension.getExt(), "");
        } else {
            throw new AssertionError("Unknown name impl");
        }
    }

    private Stream<String> getFileNameStream() {
        return storage.getDataNames(nodeId).stream()
                .map(this::migrateDataName);
    }

    @Override
    public Set<String> getFileNames(String regex) {
        Objects.requireNonNull(regex);
        Pattern p = Pattern.compile(regex);
        return getFileNameStream()
                .filter(name -> p.matcher(name).matches())
                .collect(Collectors.toSet());
    }
}
