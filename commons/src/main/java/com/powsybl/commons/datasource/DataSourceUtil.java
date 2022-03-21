/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface DataSourceUtil {

    static String getFileName(String baseName, String suffix, String ext) {
        Objects.requireNonNull(baseName);
        return baseName + (suffix != null ? suffix : "") + (ext == null || ext.isEmpty() ? "" : "." + ext);
    }

    static OpenOption[] getOpenOptions(boolean append) {
        OpenOption[] defaultOpenOptions = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};
        OpenOption[] appendOpenOptions = {StandardOpenOption.APPEND, StandardOpenOption.WRITE};

        return append ? appendOpenOptions : defaultOpenOptions;
    }

    static String getBaseName(Path file) {
        return getBaseName(file.getFileName().toString());
    }

    static String getBaseName(String fileName) {
        Objects.requireNonNull(fileName);
        int pos = fileName.lastIndexOf('.'); // find last dot
        return pos == -1 ? fileName : fileName.substring(0, pos);
    }

    static DataSource createDataSource(Path directory, String basename, CompressionFormat compressionExtension, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(basename);

        if (compressionExtension == null) {
            return new FileDataSource(directory, basename, observer);
        } else {
            switch (compressionExtension) {
                case GZIP:
                    return new GzFileDataSource(directory, basename + ".gz", observer);
                case BZIP2:
                    return new Bzip2FileDataSource(directory, basename + ".bz2", observer);
                case ZIP:
                    return new ZipFileDataSource(directory, basename + ".zip", observer);
                default:
                    throw new AssertionError("Unexpected CompressionFormat value: " + compressionExtension);
            }
        }
    }

    static DataSource createDataSource(Path directory, String fileName, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(fileName);

        if (fileName.endsWith(".zip")) {
            return new ZipFileDataSource(directory, fileName, observer);
        } else if (fileName.endsWith(".gz")) {
            return new GzFileDataSource(directory, fileName, observer);
        } else if (fileName.endsWith(".bz2")) {
            return new Bzip2FileDataSource(directory, fileName, observer);
        } else {
            return new FileDataSource(directory, fileName, observer);
        }
    }
}
