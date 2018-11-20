/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.test;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ReadOnlyDataSource;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TestGridModelPath extends AbstractTestGridModel {

    public TestGridModelPath(
            Path path,
            CgmesModel expected) {
        this(path, null, expected);
    }

    public TestGridModelPath(
            Path path,
            CompressionFormat compressionExtension,
            CgmesModel expected) {
        super(path.getFileName().toString(), expected);
        this.path = path;
        this.compressionExtension = compressionExtension;
    }

    public CompressionFormat getCompressionExtension() {
        return compressionExtension;
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public ReadOnlyDataSource dataSource() {
        return DataSourceUtil.createDataSource(path.getParent(), path.getFileName().toString(), getCompressionExtension(), null);
    }

    private final Path path;
    private final CompressionFormat compressionExtension;
}
