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

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
        Objects.requireNonNull(fileName);
        if (append) {
            throw new UnsupportedOperationException("append not supported in zip file data source");
        }
        Path zipFilePath = getZipFilePath();
        Path tmpZipFilePath = zipFilePath.getParent().resolve(zipFilePath.getFileName() + ".tmp");
        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmpZipFilePath));
        zos.putNextEntry(new ZipEntry(fileName));
        OutputStream fos = new ForwardingOutputStream<ZipOutputStream>(zos) {
            @Override
            public void close() throws IOException {
                os.closeEntry();
                // copy existing entries
                if (Files.exists(zipFilePath)) {
                    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
                        Enumeration<? extends ZipEntry> e = zipFile.entries();
                        while (e.hasMoreElements()) {
                            ZipEntry zipEntry = e.nextElement();
                            if (!zipEntry.getName().equals(fileName)) {
                                zos.putNextEntry(zipEntry);
                                try (InputStream zis = zipFile.getInputStream(zipEntry.getName())) {
                                    ByteStreams.copy(zis, zos);
                                }
                                zos.closeEntry();
                            }
                        }
                    }
                }
                zos.close();
                Files.copy(tmpZipFilePath, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
                Files.delete(tmpZipFilePath);
            }
        };
        return observer != null ? new ObservableOutputStream(fos, zipFilePath + ":" + fileName, observer) : fos;
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        return newOutputStream(DataSourceUtil.getFileName(baseName, suffix, ext), append);
    }
}
