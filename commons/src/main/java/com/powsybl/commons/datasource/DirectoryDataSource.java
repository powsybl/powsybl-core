/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class DirectoryDataSource extends AbstractFileSystemDataSource {

    public DirectoryDataSource(Path directory, String baseName) {
        this(directory, baseName, null, null, null);
    }

    public DirectoryDataSource(Path directory, String baseName,
                               DataSourceObserver observer) {
        this(directory, baseName, null, null, observer);
    }

    public DirectoryDataSource(Path directory, String baseName,
                               String mainExtension,
                               DataSourceObserver observer) {
        this(directory, baseName, mainExtension, null, observer);
    }

    DirectoryDataSource(Path directory, String baseName,
                        String mainExtension,
                        CompressionFormat compressionFormat,
                        DataSourceObserver observer) {
        super(directory, baseName, mainExtension, compressionFormat, observer);
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

    private Path getPath(String fileName) {
        Objects.requireNonNull(fileName);
        FileInformation fileInformation = new FileInformation(fileName, false);
        return directory.resolve(fileInformation.getCompressionFormat() == null ? fileName + getCompressionExtension() : fileName);
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
    public boolean exists(String suffix, String ext) throws IOException {
        return exists(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        Path path = getPath(fileName);
        return Files.isRegularFile(path);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return newInputStream(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        Path path = getPath(fileName);
        InputStream is = getCompressedInputStream(Files.newInputStream(path));
        return observer != null ? new ObservableInputStream(is, path.toString(), observer) : is;
    }

    @Override
    public Set<String> listNames(String regex) throws IOException {
        // Consider only files in the given folder, do not go into folders
        Pattern p = Pattern.compile(regex);
        int maxDepth = 1;
        try (Stream<Path> paths = Files.walk(directory, maxDepth)) {
            return paths
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.startsWith(baseName))
                // Return names after removing the compression extension
                .map(name -> name.replace(getCompressionExtension(), ""))
                .filter(s -> p.matcher(s).matches())
                .collect(Collectors.toSet());
        }
    }
}
