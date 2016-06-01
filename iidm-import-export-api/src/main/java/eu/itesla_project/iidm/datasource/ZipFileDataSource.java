/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.datasource;

import com.google.common.io.ByteStreams;
import eu.itesla_project.commons.io.ForwardingInputStream;
import eu.itesla_project.commons.io.ForwardingOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

    public ZipFileDataSource(Path zipFile) {
        this(zipFile.getParent(), com.google.common.io.Files.getNameWithoutExtension(zipFile.getFileName().toString()));
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    @Override
    public boolean exists(String suffix, String ext) throws IOException {
        return exists(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        Path zipFilePath = getZipFilePath();
        if (Files.isRegularFile(zipFilePath)) {
            try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
                return zipFile.getEntry(fileName) != null;
            }
        }
        return false;
    }

    private Path getZipFilePath() {
        return directory.resolve(zipFileName);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return newInputStream(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        ZipFile zipFile = new ZipFile(getZipFilePath().toFile());
        ZipEntry entry = zipFile.getEntry(fileName);
        if (entry == null) {
            zipFile.close();
            throw new IllegalArgumentException("Entry " + fileName + " not found");
        }
        return new ForwardingInputStream<InputStream>(zipFile.getInputStream(entry)) {
            @Override
            public void close() throws IOException {
                super.close();
                zipFile.close();
            }
        };
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        if (append) {
            throw new UnsupportedOperationException("append not supported in zip file data source");
        }
        Path tmpZipFilePath = Files.createTempFile(directory, null, null);
        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmpZipFilePath));
        Path zipFilePath = getZipFilePath();
        String entryName = DataSourceUtil.getFileName(baseName, suffix, ext);
        zos.putNextEntry(new ZipEntry(entryName));
        return new ForwardingOutputStream<ZipOutputStream>(zos) {
            @Override
            public void close() throws IOException {
                os.closeEntry();
                if (Files.exists(zipFilePath)) {
                    try (ZipFile zipFile = new ZipFile(getZipFilePath().toFile())) {
                        for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
                            ZipEntry entry = e.nextElement();
                            if (!entry.getName().equals(entryName)) {
                                zos.putNextEntry(entry);
                                ByteStreams.copy(zipFile.getInputStream(entry), zos);
                                zos.closeEntry();
                            }
                        }
                    }
                }
                super.close();
                Files.copy(tmpZipFilePath, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
                Files.delete(tmpZipFilePath);
            }
        };
    }
}
