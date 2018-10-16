/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.datasource.compressor.DataSourceCompressor;

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

    private static final String COMPRESSION_EXT = "";

    private static final int MAX_DEPTH = 1;

    private final Path dir;

    private final String fileName;

    private final DataSourceCompressor compressor;

    private final DataSourceObserver observer;

    public FileDataSource(Path file, DataSourceCompressor compressor) {
        this(file.getParent(), file.getFileName().toString(), compressor, null);
    }

    public FileDataSource(Path dir, String fileName, DataSourceCompressor compressor, DataSourceObserver observer) {
        this.dir = Objects.requireNonNull(dir);
        this.fileName = Objects.requireNonNull(fileName);
        this.compressor = Objects.requireNonNull(compressor);
        this.observer = observer;
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) {
        try {
            OutputStream os = compressor.compress(Files.newOutputStream(dir.resolve(fileName), DataSourceUtil.getOpenOptions(append)));
            return observer != null ? new ObservableOutputStream(os, fileName, observer) : os;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getMainFileName() {
        return fileName.replace(compressor.getExtension(), "");
    }

    @Override
    public boolean fileExists(String fileName) {
        return Files.exists(dir.resolve(fileName));
    }

    @Override
    public InputStream newInputStream(String fileName) {
        try {
            InputStream is = compressor.uncompress(Files.newInputStream(dir.resolve(fileName)));
            return observer != null ? new ObservableInputStream(is, fileName, observer) : is;
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
                    // Return names after removing the compression extension
                    .map(name -> name.replace(compressor.getExtension(),  ""))
                    .filter(s -> p.matcher(s).matches())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
