/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.commons.datasource.DataSource;

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

        static <T> T parse(String text, NameHandler<T> handler) {
            Objects.requireNonNull(handler);
            T result;
            AppStorageDataSource.Name dataSrcName = parse(text);
            try {
                if (dataSrcName instanceof AppStorageDataSource.SuffixAndExtension) {
                    result = handler.onSuffixAndExtension((AppStorageDataSource.SuffixAndExtension) dataSrcName);
                } else if (dataSrcName instanceof AppStorageDataSource.FileName) {
                    result = handler.onFileName((AppStorageDataSource.FileName) dataSrcName);
                } else {
                    result = handler.onOther(dataSrcName);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return result;
        }
    }

    public interface NameHandler<T> {

        T onSuffixAndExtension(AppStorageDataSource.SuffixAndExtension suffixAndExtension) throws IOException;

        T onFileName(AppStorageDataSource.FileName fileName) throws IOException;

        T onOther(AppStorageDataSource.Name name);
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
    public String getBaseName() {
        return nodeName;
    }

    @Override
    public OutputStream newOutputStream(final String suffix, final String ext, boolean append) {
        if (append) {
            throw new UnsupportedOperationException("Append mode not supported");
        }
        return storage.writeBinaryData(nodeId, new SuffixAndExtension(suffix, ext).toString());
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) {
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
    public boolean exists(String fileName) {
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

    @Override
    public Set<String> listNames(String regex) throws IOException {
        Pattern p = Pattern.compile(regex);
        Set<String> names = storage.getDataNames(nodeId).stream()
                .filter(name -> p.matcher(name).matches())
                .map(name -> Name.parse(name, new NameHandler<String>() {

                    @Override
                    public String onSuffixAndExtension(SuffixAndExtension suffixAndExtension) throws IOException {
                        throw new AssertionError("Don't know how to unmap suffix-and-extension to a data source name " + name);
                    }

                    @Override
                    public String onFileName(FileName fileName) throws IOException {
                        return fileName.getName();
                    }

                    @Override
                    public String onOther(Name otherName) {
                        // Return the original name
                        return name;
                    }
                }))
                .collect(Collectors.toSet());
        LOG.info("AppStorageDataSource::listNames()");
        names.forEach(n -> LOG.info("    {}", n));
        return names;
    }

    private static final Logger LOG = LoggerFactory.getLogger(AppStorageDataSource.class);
}
