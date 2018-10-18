/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.datasource.compressor.Bzip2DataSourceCompressor;
import com.powsybl.commons.datasource.compressor.GzipDataSourceCompressor;
import com.powsybl.commons.datasource.compressor.NoOpDataSourceCompressor;

import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@rte-france.com>
 */
public interface DataSourceUtil {

    static OpenOption[] getOpenOptions(boolean append) {
        OpenOption[] defaultOpenOptions = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
        OpenOption[] appendOpenOptions = {StandardOpenOption.APPEND};

        return append ? appendOpenOptions : defaultOpenOptions;
    }

    static DataSource createDataSource(Path directory, String fileName, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(fileName);

        if (fileName.endsWith(".zip")) {
            return new ZipFileDataSource(directory, fileName, observer);
        } else if (fileName.endsWith(".gz")) {
            return new FileDataSource(directory, fileName, GzipDataSourceCompressor.INSTANCE, observer);
        } else if (fileName.endsWith(".bz2")) {
            return new FileDataSource(directory, fileName, Bzip2DataSourceCompressor.INSTANCE, observer);
        } else {
            return new FileDataSource(directory, fileName, NoOpDataSourceCompressor.INSTANCE, observer);
        }
    }
}
