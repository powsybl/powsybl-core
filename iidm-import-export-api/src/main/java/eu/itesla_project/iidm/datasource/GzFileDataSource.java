/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GzFileDataSource extends AbstractDataSource {

    private final Path directory;

    private final String baseName;

    private final DataSourceObserver observer;

    public GzFileDataSource(Path directory, String baseName, DataSourceObserver observer) {
        this.directory = Objects.requireNonNull(directory);
        this.baseName = Objects.requireNonNull(baseName);
        this.observer = observer;
    }

    public GzFileDataSource(Path directory, String baseName) {
        this(directory, baseName, null);
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    private Path getPath(String suffix, String ext) {
        return directory.resolve(getFileName(baseName, suffix, ext) + ".gz");
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        Path path = getPath(suffix, ext);
        OutputStream os = new GZIPOutputStream(Files.newOutputStream(path, append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE));
        return observer != null ? new ObservableOutputStream(os, path.toString(), observer) : os;
    }

    @Override
    public boolean exists(String suffix, String ext) {
        Path path = getPath(suffix, ext);
        return Files.exists(path) && Files.isRegularFile(path);
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        Path path = directory.resolve(fileName + ".gz");
        return Files.exists(path) && Files.isRegularFile(path);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return new GZIPInputStream(Files.newInputStream(getPath(suffix, ext)));
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        return new GZIPInputStream(Files.newInputStream(directory.resolve(fileName + ".gz")));
    }
}
