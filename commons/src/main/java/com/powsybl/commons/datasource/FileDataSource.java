/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.datasource.compressor.DataSourceCompressor;
import com.powsybl.commons.datasource.compressor.NoOpDataSourceCompressor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
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

    private static final int MAX_DEPTH = 1;

    private final Path dir;

    private final String fileName;

    private final DataSourceCompressor compressor;

    private final DataSourceObserver observer;

    public FileDataSource(Path dir, String fileName, DataSourceCompressor compressor, DataSourceObserver observer) {
        this.dir = Objects.requireNonNull(dir);
        this.fileName = Objects.requireNonNull(fileName);
        this.compressor = Objects.requireNonNull(compressor);
        this.observer = observer;
    }

    public FileDataSource(Path dir, String fileName, DataSourceCompressor compressor) {
        this(dir, fileName, compressor, null);
    }

    public FileDataSource(Path dir, String fileName) {
        this(dir, fileName, NoOpDataSourceCompressor.INSTANCE);
    }

    public FileDataSource(Path file, DataSourceCompressor compressor) {
        this(file.getParent(), file.getFileName().toString(), compressor);
    }

    public FileDataSource(Path file) {
        this(file, NoOpDataSourceCompressor.INSTANCE);
    }

    private Path getFile(String fileName) {
        return dir.resolve(fileName.endsWith(compressor.getExtension()) ? fileName : fileName + compressor.getExtension());
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) {
        try {
            Path file = getFile(fileName);
            OutputStream os = compressor.compress(Files.newOutputStream(file, DataSourceUtil.getOpenOptions(append)));
            return observer != null ? new ObservableOutputStream(os, file.toString(), observer) : os;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getMainFileName() {
        return fileName;
    }

    @Override
    public boolean fileExists(String fileName) {
        return Files.exists(getFile(fileName));
    }

    @Override
    public InputStream newInputStream(String fileName) {
        try {
            Path file = getFile(fileName);
            InputStream is = compressor.uncompress(Files.newInputStream(file));
            return observer != null ? new ObservableInputStream(is, file.toString(), observer) : is;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Set<String> getFileNames(String regex) {
        // Consider only files in the given folder, do not go into folders
        Pattern p = Pattern.compile(regex);
        try (Stream<Path> paths = Files.walk(dir, MAX_DEPTH)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(compressor.getExtension()))
                    // Return names after removing the compression extension
                    .map(name -> name.replace(compressor.getExtension(), ""))
                    .filter(s -> p.matcher(s).matches())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
