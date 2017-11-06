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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ZipFileDataSource implements DataSource {

    private final Path directory;

    private final String zipFileName;

    private final String baseName;

    private final DataSourceObserver observer;

    public ZipFileDataSource(Path directory, String zipFileName, String baseName, DataSourceObserver observer) {
        this.directory = Objects.requireNonNull(directory);
        this.zipFileName = Objects.requireNonNull(zipFileName);
        this.baseName = Objects.requireNonNull(baseName);
        this.observer = observer;
    }

    public ZipFileDataSource(Path directory, String zipFileName, String baseName) {
        this(directory, zipFileName, baseName, null);
    }

    public ZipFileDataSource(Path directory, String baseName) {
        this(directory, baseName + ".zip", baseName, null);
    }

    public ZipFileDataSource(Path directory, String baseName, DataSourceObserver observer) {
        this(directory, baseName + ".zip", baseName, observer);
    }

    public ZipFileDataSource(Path zipFile) {
        this(zipFile.getParent(), com.google.common.io.Files.getNameWithoutExtension(zipFile.getFileName().toString()));
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    private Path getZipFilePath() {
        return directory.resolve(zipFileName);
    }

    @Override
    public boolean exists(String suffix, String ext) throws IOException {
        return exists(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        Objects.requireNonNull(fileName);
        Path zipFilePath = getZipFilePath();
        if (Files.exists(zipFilePath)) {
            try (ZipFile zipFile = new ZipFile(zipFilePath)) {
                return zipFile.entry(fileName) != null;
            }
        }
        return false;
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return newInputStream(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        Objects.requireNonNull(fileName);
        Path zipFilePath = getZipFilePath();
        if (Files.exists(zipFilePath)) {
            ZipFile zipFile = new ZipFile(zipFilePath);
            InputStream is = zipFile.getInputStream(fileName);
            if (is != null) {
                InputStream fis = new ForwardingInputStream<InputStream>(is) {
                    @Override
                    public void close() throws IOException {
                        zipFile.close();
                    }
                };
                return observer != null ? new ObservableInputStream(fis, zipFilePath + ":" + fileName, observer) : fis;
            } else {
                zipFile.close();
            }
        }
        return null;
    }

    private static class AddEntryToZipOutputStream extends ForwardingOutputStream<ZipOutputStream> {

        private final Path zipFilePath;

        private final String fileName;

        private AddEntryToZipOutputStream(Path zipFilePath, String fileName) throws IOException {
            super(new ZipOutputStream(Files.newOutputStream(getTmpZipFilePath(zipFilePath))));
            this.zipFilePath = zipFilePath;
            this.fileName = fileName;

            // create new entry
            os.putNextEntry(new ZipEntry(fileName));
        }

        private static Path getTmpZipFilePath(Path zipFilePath) {
            return zipFilePath.getParent().resolve(zipFilePath.getFileName() + ".tmp");
        }

        @Override
        public void close() throws IOException {
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
            Files.copy(tmpZipFilePath, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(tmpZipFilePath);
        }
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
        Objects.requireNonNull(fileName);
        if (append) {
            throw new UnsupportedOperationException("append not supported in zip file data source");
        }
        Path zipFilePath = getZipFilePath();
        OutputStream fos = new AddEntryToZipOutputStream(zipFilePath, fileName);
        return observer != null ? new ObservableOutputStream(fos, zipFilePath + ":" + fileName, observer) : fos;
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        return newOutputStream(DataSourceUtil.getFileName(baseName, suffix, ext), append);
    }
}
