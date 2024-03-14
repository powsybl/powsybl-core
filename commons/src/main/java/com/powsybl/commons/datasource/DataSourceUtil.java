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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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
        int pos = fileName.indexOf('.'); // find first dot in case of double extension (.xml.gz)
        return pos == -1 ? fileName : fileName.substring(0, pos);
    }

    static DataSource createDataSource(Path directory, String basename, CompressionFormat compressionExtension, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(basename);

        if (compressionExtension == null) {
            return new FileDataSource(directory, basename, observer);
        } else {
            return switch (compressionExtension) {
                case BZIP2 -> new Bzip2FileDataSource(directory, basename, observer);
                case GZIP -> new GzFileDataSource(directory, basename, observer);
                case XZ -> new XZFileDataSource(directory, basename, observer);
                case ZIP -> new ZipFileDataSource(directory, basename, observer);
                case ZSTD -> new ZstdFileDataSource(directory, basename, observer);
            };
        }
    }

    static DataSource createDataSource(Path directory, String fileNameOrBaseName, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(fileNameOrBaseName);

        int dotIndex = fileNameOrBaseName.charAt(fileNameOrBaseName.length() - 4) == '.' ? fileNameOrBaseName.length() - 4 : fileNameOrBaseName.length() - 3;
        String fileNameNoExtension = fileNameOrBaseName.substring(0, dotIndex);
        String extension = fileNameOrBaseName.substring(dotIndex + 1);

        return switch (extension) {
            case "zst" -> new ZstdFileDataSource(directory, getBaseName(fileNameNoExtension), observer);
            case "zip" -> new ZipFileDataSource(directory, fileNameOrBaseName, getBaseName(fileNameNoExtension), observer);
            case "xz" -> new XZFileDataSource(directory, getBaseName(fileNameNoExtension), observer);
            case "gz" -> new GzFileDataSource(directory, getBaseName(fileNameNoExtension), observer);
            case "bz2" -> new Bzip2FileDataSource(directory, getBaseName(fileNameNoExtension), observer);
            default -> new FileDataSource(directory, getBaseName(fileNameOrBaseName), observer);
        };
    }
}
