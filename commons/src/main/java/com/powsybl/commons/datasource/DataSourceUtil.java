/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import java.io.InputStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@rte-france.com>
 */
public interface DataSourceUtil {

    static String getFileName(String baseName, String suffix, String ext) {
        Objects.requireNonNull(baseName);
        return baseName + (suffix != null ? suffix : "") + (ext != null ? "." + ext : "");
    }

    static OpenOption[] getOpenOptions(boolean append) {
        OpenOption[] defaultOpenOptions = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
        OpenOption[] appendOpenOptions = {StandardOpenOption.APPEND};

        return append ? appendOpenOptions : defaultOpenOptions;
    }

    static String getBaseName(Path file) {
        return getBaseName(file.getFileName().toString());
    }

    static String getBaseName(String fileName) {
        Objects.requireNonNull(fileName);
        int pos = fileName.indexOf('.'); // find first dot in case of double extension (.xml.gz)
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
                    return new GzFileDataSource(directory, basename, observer);
                case BZIP2:
                    return new Bzip2FileDataSource(directory, basename, observer);
                case ZIP:
                    return new ZipFileDataSource(directory, basename, observer);
                default:
                    throw new AssertionError("Unexpected CompressionFormat value: " + compressionExtension);
            }
        }
    }

    static DataSource createDataSource(Path directory, String fileNameOrBaseName, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(fileNameOrBaseName);

        if (fileNameOrBaseName.endsWith(".zip")) {
            return new ZipFileDataSource(directory, getBaseName(fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 4)), observer);
        } else if (fileNameOrBaseName.endsWith(".gz")) {
            return new GzFileDataSource(directory, getBaseName(fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 3)), observer);
        } else if (fileNameOrBaseName.endsWith(".bz2")) {
            return new Bzip2FileDataSource(directory, getBaseName(fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 4)), observer);
        } else {
            return new FileDataSource(directory, getBaseName(fileNameOrBaseName), observer);
        }
    }

    static ReadOnlyMemDataSource createReadOnlyMemDataSource(String fileName, InputStream content) {
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(content);

        ReadOnlyMemDataSource dataSource;
        if (fileName.endsWith(".zip")) {
            dataSource = new ZipMemDataSource(fileName, content);
        } else if (fileName.endsWith(".gz")) {
            dataSource =  new GzMemDataSource(fileName, content);
        } else if (fileName.endsWith(".bz2")) {
            dataSource =  new Bzip2MemDataSource(fileName, content);
        } else {
            dataSource = new ReadOnlyMemDataSource(getBaseName(fileName));
            dataSource.putData(fileName, content);
        }

        return dataSource;
    }
}
