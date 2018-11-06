/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.io.ForwardingInputStream;
import com.powsybl.commons.io.ForwardingOutputStream;
import net.java.truevfs.comp.zip.ZipEntry;
import net.java.truevfs.comp.zip.ZipFile;
import net.java.truevfs.comp.zip.ZipOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ZipFileDataSource implements DataSource {

    private final Path directory;

    private final String zipFileName;

    private final DataSourceObserver observer;

    public ZipFileDataSource(Path directory, String zipFileName, DataSourceObserver observer) {
        this.directory = Objects.requireNonNull(directory);
        this.zipFileName = Objects.requireNonNull(zipFileName);
        this.observer = observer;
    }

    public ZipFileDataSource(Path directory, String zipFileName) {
        this(directory, zipFileName, null);
    }

    public ZipFileDataSource(Path zipFile) {
        this(zipFile.getParent(), zipFile.getFileName().toString());
    }

    @Override
    public String getMainFileName() {
        return null;
    }

    @Override
    public void setMainFileName(String mainFileName) {
        // nothing to do
    }

    private Path getZipFilePath() {
        return directory.resolve(zipFileName);
    }

    private static boolean entryExists(Path zipFilePath, String fileName) {
        if (Files.exists(zipFilePath)) {
            try (ZipFile zipFile = new ZipFile(zipFilePath)) {
                return zipFile.entry(fileName) != null;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean fileExists(String fileName) {
        Objects.requireNonNull(fileName);
        Path zipFilePath = getZipFilePath();
        return entryExists(zipFilePath, fileName);
    }

    private static final class ZipEntryInputStream extends ForwardingInputStream<InputStream> {

        private final ZipFile zipFile;

        private ZipEntryInputStream(ZipFile zipFile, String fileName) throws IOException {
            super(zipFile.getInputStream(fileName));
            this.zipFile = zipFile;
        }

        private static ZipEntryInputStream create(Path zipFilePath, String fileName) {
            try {
                return new ZipEntryInputStream(new ZipFile(zipFilePath), fileName);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void close() throws IOException {
            super.close();

            zipFile.close();
        }
    }

    @Override
    public InputStream newInputStream(String fileName) {
        Objects.requireNonNull(fileName);
        Path zipFilePath = getZipFilePath();
        InputStream is = ZipEntryInputStream.create(zipFilePath, fileName);
        return observer != null ? new ObservableInputStream(is, zipFilePath + ":" + fileName, observer) : is;
    }

    @Override
    public Set<String> getFileNames(String regex) {
        // Consider only files in the given folder, do not go into folders
        Pattern p = Pattern.compile(regex);
        Set<String> names = new HashSet<>();
        Path zipFilePath = getZipFilePath();
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry zipEntry = e.nextElement();
                if (!zipEntry.isDirectory() && p.matcher(zipEntry.getName()).matches()) {
                    names.add(zipEntry.getName());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return names;
    }


    private static final class ZipEntryOutputStream extends ForwardingOutputStream<ZipOutputStream> {

        private final Path zipFilePath;

        private final String fileName;

        private boolean closed;

        private ZipEntryOutputStream(Path zipFilePath, String fileName) throws IOException {
            super(new ZipOutputStream(Files.newOutputStream(getTmpZipFilePath(zipFilePath))));
            this.zipFilePath = zipFilePath;
            this.fileName = fileName;
            this.closed = false;

            // create new entry
            os.putNextEntry(new ZipEntry(fileName));
        }

        private static ZipEntryOutputStream create(Path zipFilePath, String fileName) {
            try {
                return new ZipEntryOutputStream(zipFilePath, fileName);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private static Path getTmpZipFilePath(Path zipFilePath) {
            return zipFilePath.getParent().resolve(zipFilePath.getFileName() + ".tmp");
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                // close new entry
                os.closeEntry();

                // copy existing entries
                if (Files.exists(zipFilePath)) {
                    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
                        Enumeration<? extends ZipEntry> e = zipFile.entries();
                        while (e.hasMoreElements()) {
                            ZipEntry zipEntry = e.nextElement();
                            if (!zipEntry.getName().equals(fileName)) {
                                os.putNextEntry(zipEntry);
                                try (InputStream zis = zipFile.getInputStream(zipEntry.getName())) {
                                    ByteStreams.copy(zis, os);
                                }
                                os.closeEntry();
                            }
                        }
                    }
                }

                // close zip
                super.close();

                // swap with tmp zip
                Path tmpZipFilePath = getTmpZipFilePath(zipFilePath);
                Files.move(tmpZipFilePath, zipFilePath, StandardCopyOption.REPLACE_EXISTING);

                closed = true;
            }
        }
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) {
        Objects.requireNonNull(fileName);
        if (append) {
            throw new UnsupportedOperationException("append not supported in zip file data source");
        }
        Path zipFilePath = getZipFilePath();
        OutputStream os = ZipEntryOutputStream.create(zipFilePath, fileName);
        return observer != null ? new ObservableOutputStream(os, zipFilePath + ":" + fileName, observer) : os;
    }
}
