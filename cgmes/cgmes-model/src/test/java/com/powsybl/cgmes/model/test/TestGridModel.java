package com.powsybl.cgmes.model.test;

import java.io.InputStream;
import java.nio.file.Files;

/*
 * #%L
 * CGMES data model
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.nio.file.Path;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ReadOnlyDataSource;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TestGridModel {

    public TestGridModel(
            String resourceName,
            CgmesModel expected) {
        this.name = resourceName;
        this.path = null;
        this.basename = null;
        this.compressionExtension = null;
        this.expected = expected;
        this.resourceName = resourceName;
    }

    public TestGridModel(
            Path path,
            String basename,
            CgmesModel expected) {
        this(path, basename, null, expected);
    }

    public TestGridModel(
            Path path,
            String basename,
            CompressionFormat compressionExtension,
            CgmesModel expected) {
        this.name = path.getName(path.getNameCount() - 1).toString();
        this.path = path;
        this.basename = basename;
        this.compressionExtension = compressionExtension;
        this.expected = expected;
        this.resourceName = null;
    }

    public String name() {
        return name;
    }

    public boolean exists() {
        if (path != null) {
            return Files.exists(path);
        } else {
            return resourceStream(resourceName) != null;
        }
    }

    public String basename() {
        return basename;
    }

    public CompressionFormat getCompressionExtension() {
        return compressionExtension;
    }

    public ReadOnlyDataSource dataSource() {
        if (isResource()) {
            return DataSourceUtil.createReadOnlyMemDataSource(
                    resourceName,
                    resourceStream(resourceName));
        } else {
            return DataSourceUtil.createDataSource(
                    path,
                    basename(),
                    getCompressionExtension(),
                    null);
        }
    }

    public CgmesModel expected() {
        return expected;
    }

    private boolean isResource() {
        return resourceName != null;
    }

    private InputStream resourceStream(String resource) {
        return ClassLoader.getSystemResourceAsStream(resource);
    }

    private final String name;
    private final Path path;
    private final String basename;
    private final CompressionFormat compressionExtension;
    private final CgmesModel expected;

    private final String resourceName;
}
