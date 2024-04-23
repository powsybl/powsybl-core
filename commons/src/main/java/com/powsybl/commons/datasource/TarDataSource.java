/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.ForwardingOutputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class TarDataSource extends AbstractArchiveDataSource {

    public TarDataSource(Path directory, String tarFileName, String baseName, CompressionFormat compressionFormat, String sourceFormat, DataSourceObserver observer) {
        super(directory, tarFileName, baseName, compressionFormat, ArchiveFormat.TAR, sourceFormat, observer);
    }

    public TarDataSource(Path directory, String baseName, CompressionFormat compressionFormat, String sourceFormat, DataSourceObserver observer) {
        super(directory,
            baseName + sourceFormat + ".tar" + (compressionFormat == null ? "" : "." + compressionFormat.getExtension()),
            baseName, compressionFormat, ArchiveFormat.TAR, sourceFormat, observer);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Files are here located in the archive.</p>
     */
    @Override
    public Set<String> listNames(String regex) throws IOException {
        // Initialize variables
        Pattern p = Pattern.compile(regex);
        Set<String> names = new HashSet<>();
        Path zipFilePath = getArchiveFilePath();

        // Explore the archive
        try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(zipFilePath));
             InputStream cis = getCompressedInputStream(inputStream, compressionFormat);
             TarArchiveInputStream tar = new TarArchiveInputStream(cis)) {
            ArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                // Consider only files located at the root of the archive
                if (!entry.isDirectory()
                    && entry.getName().indexOf('/') == entry.getName().lastIndexOf('/')
                    && (baseName.isEmpty() || entry.getName().startsWith(baseName))) {
                    FileInformation fileInformation = new FileInformation(entry.getName(), false);
                    // Check that files have the same source format and respect the regex
                    if ((sourceFormat.isEmpty() || fileInformation.getSourceFormat().equals(sourceFormat))
                        && p.matcher(entry.getName()).matches()) {
                        names.add(entry.getName());
                    }
                }
            }
        }
        return names;
    }

    private boolean entryExists(Path tarFilePath, String fileName) {
        if (Files.exists(tarFilePath)) {
            try (InputStream fis = Files.newInputStream(tarFilePath);
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 InputStream is = getCompressedInputStream(bis, compressionFormat);
                 TarArchiveInputStream tais = new TarArchiveInputStream(is)) {

                TarArchiveEntry entry;
                while ((entry = tais.getNextEntry()) != null) {
                    if (entry.getName().equals(fileName)) {
                        return true;
                    }
                }
                return false;
            } catch (IOException | UnsupportedOperationException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Check if a file exists in the archive. The file name will be constructed as:
     * <p>{@code <basename><suffix>.<ext>}</p>
     * @param suffix Suffix to add to the basename of the datasource
     * @param ext Extension of the file (for example: .iidm, .xml, .txt, etc.)
     * @param checkConsistencyWithDataSource Should the filename be checked for consistency with the DataSource
     * @return true if the file exists, else false
     */
    @Override
    public boolean exists(String suffix, String ext, boolean checkConsistencyWithDataSource) throws IOException {
        return exists(DataSourceUtil.getFileName(baseName, suffix, ext), checkConsistencyWithDataSource);
    }

    /**
     * Check if a file exists in the archive.
     * @param fileName Name of the file
     * @param checkConsistencyWithDataSource Should the filename be checked for consistency with the DataSource
     * @return true if the file exists, else false
     */
    @Override
    public boolean exists(String fileName, boolean checkConsistencyWithDataSource) {
        Objects.requireNonNull(fileName);
        Path tarFilePath = getArchiveFilePath();
        return (!checkConsistencyWithDataSource || isConsistentWithDataSource(fileName)) && entryExists(tarFilePath, fileName);
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        return newOutputStream(DataSourceUtil.getFileName(baseName, suffix, ext), append);
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
        Objects.requireNonNull(fileName);
        if (append) {
            throw new UnsupportedOperationException("append not supported in tar file data source");
        }
        Path tarFilePath = getArchiveFilePath();
        OutputStream os = new TarEntryOutputStream(tarFilePath, fileName, compressionFormat);
        return observer != null ? new ObservableOutputStream(os, tarFilePath + ":" + fileName, observer) : os;
    }

    @Override
    public InputStream newInputStream(String suffix, String ext, boolean checkConsistencyWithDataSource) throws IOException {
        return newInputStream(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public InputStream newInputStream(String fileName, boolean checkConsistencyWithDataSource) throws IOException {
        Objects.requireNonNull(fileName);
        Path tarFilePath = getArchiveFilePath();
        if (checkConsistencyWithDataSource && !isConsistentWithDataSource(fileName)) {
            throw new PowsyblException(String.format("File %s is inconsistent with the ArchiveDataSource", fileName));
        }
        try {
            InputStream fis = Files.newInputStream(tarFilePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            InputStream is = getCompressedInputStream(bis, this.compressionFormat);
            TarArchiveInputStream tais = new TarArchiveInputStream(is);
            TarArchiveEntry entry;
            while ((entry = tais.getNextEntry()) != null) {
                if (entry.getName().equals(fileName)) {
                    return observer != null ? new ObservableInputStream(tais, tarFilePath + ":" + fileName, observer) : tais;
                }
            }
            throw new PowsyblException(String.format("File %s does not seem to exist in archive %s", fileName, tarFilePath.getFileName()));
        } catch (IOException e) {
            throw new PowsyblException(String.format("Tar file %s does not seem to exist", tarFilePath.getFileName()));
        }
    }

    private static final class TarEntryOutputStream extends ForwardingOutputStream<OutputStream> {

        private final Path tarFilePath;
        private final String fileName;
        private final CompressionFormat compressionFormat;
        private boolean closed;

        private TarEntryOutputStream(Path tarFilePath, String fileName, CompressionFormat compressionFormat) throws IOException {
            super(getTmpStream(getTmpStreamFilePath(tarFilePath)));
            this.tarFilePath = tarFilePath;
            this.fileName = fileName;
            this.compressionFormat = compressionFormat;
            this.closed = false;
        }

        private static OutputStream getTmpStream(Path tarFilePath) throws IOException {
            return new BufferedOutputStream(Files.newOutputStream(tarFilePath));
        }

        private static Path getTmpStreamFilePath(Path tarFilePath) {
            return tarFilePath.getParent().resolve("tmp_stream_" + tarFilePath.getFileName() + ".stream");
        }

        private static TarArchiveOutputStream getTarStream(Path tmpTarFilePath) throws IOException {
            return new TarArchiveOutputStream(new BufferedOutputStream(Files.newOutputStream(tmpTarFilePath)));
        }

        private static Path getTmpTarFilePath(Path tarFilePath) {
            return tarFilePath.getParent().resolve("tmp_" + tarFilePath.getFileName());
        }

        private static Path getTmpCompressedTarFilePath(Path tarFilePath) {
            return tarFilePath.getParent().resolve("tmp_comp_" + tarFilePath.getFileName());
        }

        private void compressTarFile() throws IOException {
            try (InputStream fis = Files.newInputStream(getTmpTarFilePath(tarFilePath));
                 OutputStream fos = Files.newOutputStream(getTmpCompressedTarFilePath(tarFilePath), StandardOpenOption.CREATE);
                 OutputStream compressedOS = getCompressedOutputStream(fos, this.compressionFormat)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    compressedOS.write(buffer, 0, len);
                }
            }
        }

        private static OutputStream getCompressedOutputStream(OutputStream is, CompressionFormat compressionFormat) throws IOException {
            return switch (compressionFormat) {
                case GZIP -> new GzipCompressorOutputStream(is);
                case BZIP2 -> new BZip2CompressorOutputStream(is);
                case XZ -> new XZCompressorOutputStream(is);
                case ZSTD -> new ZstdCompressorOutputStream(is);
                default -> is;
            };
        }

        @Override
        public void close() throws IOException {
            if (!closed) {

                // Close temporary stream file
                super.close();

                // Open a new temporary archive
                try (TarArchiveOutputStream taos = getTarStream(getTmpTarFilePath(tarFilePath))) {

                    // Useful parameter
                    taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
                    taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

                    // Copy content of temporary stream file into an entry of the temporary archive
                    try (InputStream is = Files.newInputStream(getTmpStreamFilePath(tarFilePath))) {

                        // Content of the stream
                        byte[] streamContent = is.readAllBytes();

                        // New tar entry
                        TarArchiveEntry entry = new TarArchiveEntry(fileName);
                        entry.setSize(streamContent.length);

                        // New file to add
                        taos.putArchiveEntry(entry);

                        // Write the data in the entry
                        taos.write(streamContent);

                        // close new entry
                        taos.closeArchiveEntry();
                    }

                    // Copy existing entries into the temporary archive
                    if (Files.exists(tarFilePath)) {
                        try (InputStream fis = Files.newInputStream(tarFilePath);
                             BufferedInputStream bis = new BufferedInputStream(fis);
                             InputStream cis = getCompressedInputStream(bis, compressionFormat);
                             TarArchiveInputStream tarInput = new TarArchiveInputStream(cis)) {
                            TarArchiveEntry oldEntry;
                            while ((oldEntry = tarInput.getNextEntry()) != null) {
                                if (!oldEntry.getName().equals(fileName)) {
                                    taos.putArchiveEntry(oldEntry);
                                    byte[] buffer = new byte[8192];
                                    int len;
                                    while ((len = tarInput.read(buffer)) != -1) {
                                        taos.write(buffer, 0, len);
                                    }
                                    taos.closeArchiveEntry();
                                }
                            }
                        }
                    }

                    // Finishes the TAR archive without closing the underlying OutputStream
                    taos.finish();
                }

                // Compress the archive if needed
                compressTarFile();

                // swap with tmp tar
                Path tmpTarFilePath = getTmpCompressedTarFilePath(tarFilePath);
                Files.move(tmpTarFilePath, tarFilePath, StandardCopyOption.REPLACE_EXISTING);

                closed = true;
            }
        }
    }

    private static InputStream getCompressedInputStream(InputStream is, CompressionFormat compressionFormat) throws IOException {
        if (compressionFormat == null) {
            return is;
        }
        return switch (compressionFormat) {
            case GZIP -> new GzipCompressorInputStream(is);
            case BZIP2 -> new BZip2CompressorInputStream(is);
            case XZ -> new XZCompressorInputStream(is);
            case ZSTD -> new ZstdCompressorInputStream(is);
            default -> is;
        };
    }
}
