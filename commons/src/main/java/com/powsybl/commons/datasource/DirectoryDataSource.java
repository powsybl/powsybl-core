/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class DirectoryDataSource extends AbstractDataSource {

    public DirectoryDataSource(Path directory, String baseName,
                               String sourceFormat,
                               DataSourceObserver observer) {
        super(directory, baseName, null, null, sourceFormat, observer);
    }

    DirectoryDataSource(Path directory, String baseName,
                               CompressionFormat compressionFormat,
                               String sourceFormat,
                               DataSourceObserver observer) {
        super(directory, baseName, compressionFormat, null, sourceFormat, observer);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Files are here located in the datasource directory.</p>
     * <p>The files returned are those that start with the basename of the datasource and end with the source and
     * compression extensions.</p>
     */
    @Override
    public Set<String> listNames(String regex) throws IOException {
        // Consider only files in the given directory, do not go into folders
        Pattern p = Pattern.compile(regex);
        int maxDepth = 1;
        try (Stream<Path> paths = Files.walk(directory, maxDepth)) {
            return paths
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                // Name starts with the base name
                .filter(name -> name.startsWith(baseName))
                // Source format and compression format are equals
                .filter(name -> {
                    FileInformation fileInformation = new FileInformation(name, false);
                    return (sourceFormat.isEmpty() || fileInformation.getSourceFormat().equals(sourceFormat))
                        && (compressionFormat == null && fileInformation.getCompressionFormat() == null
                        || fileInformation.getCompressionFormat() != null && fileInformation.getCompressionFormat().equals(compressionFormat));
                })
                // Name matches the regex
                .filter(s -> p.matcher(s).matches())
                // Delete the compression extension
                .map(name -> name.replace(getCompressionExtension(), ""))
                .collect(Collectors.toSet());
        }
    }

    private Path getPath(String fileName) {
        Objects.requireNonNull(fileName);
        FileInformation fileInformation = new FileInformation(fileName, false);
        return directory.resolve(fileInformation.getCompressionFormat() == null ? fileName + getCompressionExtension() : fileName);
    }

    /**
     * Check if a file exists in the datasource. The file name will be constructed as:
     * <p>{@code <directory>/<basename><suffix>.<ext>.<compression_ext>}</p>
     * with the compression extension being optional and depending on how the datasource is configured.
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
     * Check if a file exists in the datasource. The file name will be constructed as:
     * <p>{@code <directory>/<fileName>.<compression_ext>}</p>
     * with the compression extension being optional and depending on how the datasource is configured.
     * @param fileName Name of the file (with or without the compression extension)
     * @param checkConsistencyWithDataSource Should the filename be checked for consistency with the DataSource
     * @return true if the file exists, else false
     */
    @Override
    public boolean exists(String fileName, boolean checkConsistencyWithDataSource) {
        Path path = getPath(fileName);
        return (!checkConsistencyWithDataSource || isConsistentWithDataSource(path.getFileName().toString()))
            && Files.isRegularFile(path);
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        return newOutputStream(DataSourceUtil.getFileName(baseName, suffix, ext), append);
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
        Path path = getPath(fileName);
        OutputStream os = getCompressedOutputStream(Files.newOutputStream(path, DataSourceUtil.getOpenOptions(append)));
        return observer != null ? new ObservableOutputStream(os, path.toString(), observer) : os;
    }

    @Override
    public InputStream newInputStream(String suffix, String ext, boolean checkConsistencyWithDataSource) throws IOException {
        return newInputStream(DataSourceUtil.getFileName(baseName, suffix, ext), checkConsistencyWithDataSource);
    }

    @Override
    public InputStream newInputStream(String fileName, boolean checkConsistencyWithDataSource) throws IOException {
        Path path = getPath(fileName);
        if (checkConsistencyWithDataSource && !isConsistentWithDataSource(path.getFileName().toString())) {
            return null;
        }
        InputStream is = getCompressedInputStream(Files.newInputStream(path));
        return observer != null ? new ObservableInputStream(is, path.toString(), observer) : is;
    }

    /**
     * @throws IOException Overriding classes may throw this exception
     */
    protected InputStream getCompressedInputStream(InputStream is) throws IOException {
        return is;
    }

    /**
     * @throws IOException Overriding classes may throw this exception
     */
    protected OutputStream getCompressedOutputStream(OutputStream os) throws IOException {
        return os;
    }

    @Override
    public boolean isConsistentWithDataSource(String fileName) {
        FileInformation fileInformation = new FileInformation(fileName, false);
        return fileName.startsWith(baseName) &&
            (sourceFormat.isEmpty() || fileInformation.getSourceFormat().equals(sourceFormat)) &&
            (compressionFormat == null || fileInformation.getCompressionFormat().equals(compressionFormat));
    }
}
