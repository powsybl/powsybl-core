/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.datasource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.jboss.shrinkwrap.impl.base.io.tar.bzip.BZip2CompressorInputStream;
import org.jboss.shrinkwrap.impl.base.io.tar.bzip.BZip2CompressorOutputStream;

/**
 * @author Quinary <itesla@quinary.com>
 */
public class Bzip2FileDataSource implements DataSource {

    private final Path directory;

    private final String baseName;

    private final DataSourceObserver observer;

    public Bzip2FileDataSource(Path directory, String baseName, DataSourceObserver observer) {
        this.directory = Objects.requireNonNull(directory);
        this.baseName = Objects.requireNonNull(baseName);
        this.observer = observer;
    }

    public Bzip2FileDataSource(Path directory, String baseName) {
        this(directory, baseName, null);
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    private Path getPath(String suffix, String ext) {
        return directory.resolve(DataSourceUtil.getFileName(baseName, suffix, ext + ".bz2"));
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        Path path = getPath(suffix, ext);
        OutputStream os = new BZip2CompressorOutputStream(new BufferedOutputStream(Files.newOutputStream(path, append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE)));
        return observer != null ? new ObservableOutputStream(os, path.toString(), observer) : os;
    }

    @Override
    public boolean exists(String suffix, String ext) {
        Path path = getPath(suffix, ext);
        return Files.isRegularFile(path);
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        Path path = directory.resolve(fileName + ".bz2");
        return Files.isRegularFile(path);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return new BZip2CompressorInputStream(new BufferedInputStream(Files.newInputStream(getPath(suffix, ext))));
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        return new BZip2CompressorInputStream(new BufferedInputStream(Files.newInputStream(directory.resolve(fileName + ".bz2"))));
    }
}
