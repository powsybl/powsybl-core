/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FileDataSource implements DataSource {

    private static final String COMPRESSION_EXT = "";

    private final Path directory;

    private final String fileName;

    private final DataSourceObserver observer;

    public FileDataSource(Path directory, String fileName) {
        this(directory, fileName, null);
    }

    public FileDataSource(Path directory, String fileName, DataSourceObserver observer) {
        this.directory = Objects.requireNonNull(directory);
        this.fileName = Objects.requireNonNull(fileName);
        this.observer = observer;
    }

    @Override
    public String getBaseName() {
        return DataSourceUtil.getBaseName(fileName);
    }

    protected String getCompressionExt() {
        return COMPRESSION_EXT;
    }

    protected InputStream getCompressedInputStream(InputStream is) throws IOException {
        return is;
    }

    protected OutputStream getCompressedOutputStream(OutputStream os) throws IOException {
        return os;
    }

    private Path getPath(String fileName) {
        Objects.requireNonNull(fileName);
        return directory.resolve(fileName);
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        return newOutputStream(DataSourceUtil.getFileName(DataSourceUtil.getBaseName(fileName), suffix, ext), append);
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
        Path path = getPath(fileName);
        OutputStream os = getCompressedOutputStream(Files.newOutputStream(path, DataSourceUtil.getOpenOptions(append)));
        return observer != null ? new ObservableOutputStream(os, path.toString(), observer) : os;
    }

    @Override
    public boolean exists(String suffix, String ext) throws IOException {
        return fileName.endsWith((suffix == null ? "" : suffix) + "." + ext + getCompressionExt()) && Files.isRegularFile(directory.resolve(fileName));
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        Path path = getPath(fileName);
        return Files.isRegularFile(path);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return newInputStream(fileName);
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
                    // Return names after removing the compression extension
                    .map(name -> name.replace(getCompressionExt(),  ""))
                    .filter(s -> p.matcher(s).matches())
                    .collect(Collectors.toSet());
        }
    }
}
