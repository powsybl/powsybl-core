/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.datasource;

import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@rte-france.com>
 */
public interface DataSourceUtil {

    OpenOption[] DEFAULT_OPEN_OPTIONS = { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
    OpenOption[] APPEND_OPEN_OPTIONS = { StandardOpenOption.APPEND };

    static String getFileName(String baseName, String suffix, String ext) {
        return baseName + (suffix != null ? suffix : "") + (ext != null ? "." + ext : "");
    }

    static OpenOption[] getOpenOptions(boolean append) {
        return append ? APPEND_OPEN_OPTIONS : DEFAULT_OPEN_OPTIONS;
    }

    static String getBaseName(Path file) {
        return getBaseName(file.getFileName().toString());
    }

    static String getBaseName(String fileName) {
        int pos = fileName.indexOf('.'); // find first dot in case of double extension (.xml.gz)
        return pos == -1 ? fileName : fileName.substring(0, pos);
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
}
