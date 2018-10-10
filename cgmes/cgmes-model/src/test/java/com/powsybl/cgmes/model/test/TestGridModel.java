package com.powsybl.cgmes.model.test;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.FileDataSource;
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
        this.boundaryResourceName = null;
    }

    public TestGridModel(
            String resourceName,
            String boundaryResourceName,
            CgmesModel expected) {
        this.name = resourceName;
        this.path = null;
        this.basename = null;
        this.compressionExtension = null;
        this.expected = expected;
        this.resourceName = resourceName;
        this.boundaryResourceName = boundaryResourceName;
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
        this.boundaryResourceName = null;
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
            if (boundaryResourceName == null) {
                return DataSourceUtil.createReadOnlyMemDataSource(resourceName, resourceStream(resourceName));
            } else {
                try {
                    return dataSourceFromResourceAndBoundaries();
                } catch (IOException x) {
                    String message = String.format("creating data source for resource [%s] and boundary resource [%s]",
                            resourceName,
                            boundaryResourceName);
                    throw new CgmesModelException(message, x);
                }
            }
        } else {
            return DataSourceUtil.createDataSource(path, basename(), getCompressionExtension(), null);
        }
    }

    public CgmesModel expected() {
        return expected;
    }

    private boolean isResource() {
        return resourceName != null;
    }

    private InputStream resourceStream(String resource) {
        return getClass().getClassLoader().getResourceAsStream(resource);
    }

    private ReadOnlyDataSource dataSourceFromResourceAndBoundaries() throws IOException {
        // When data and boundary resources are given,
        // we combine them in a single data source
        // An alternative would be to allow configure the location of boundaries
        // TODO the temporal file system created for the merge is not closed explicitly
        FileSystem fileSystem = Jimfs.newFileSystem();
        Path folder = Files.createDirectory(fileSystem.getPath("data"));

        ReadOnlyDataSource gmds = DataSourceUtil.createReadOnlyMemDataSource(
                resourceName,
                resourceStream(resourceName));
        Set<String> names = gmds.listNames("(?i)^.*\\.XML$");
        DataSource mergedds = new FileDataSource(folder, baseNameFromNames(names));
        for (Iterator<String> k = names.iterator(); k.hasNext();) {
            String name = k.next();
            try (InputStream is = gmds.newInputStream(name);
                    OutputStream os = mergedds.newOutputStream(name, false)) {
                ByteStreams.copy(is, os);
            }
        }
        ReadOnlyDataSource bds = DataSourceUtil.createReadOnlyMemDataSource(
                boundaryResourceName,
                resourceStream(boundaryResourceName));
        Set<String> bnames = bds.listNames("(?i)^.*\\.XML$");
        for (Iterator<String> k = bnames.iterator(); k.hasNext();) {
            String name = k.next();
            try (InputStream is = bds.newInputStream(name);
                    OutputStream os = mergedds.newOutputStream(name, false)) {
                ByteStreams.copy(is, os);
            }
        }

        Set<String> names1 = mergedds.listNames(".*");
        LOG.info("List of names in data source for ... = {}", Arrays.toString(names1.toArray()));
        return mergedds;
    }

    private String baseNameFromNames(Set<String> names) {
        return names.iterator().next()
                .replaceAll("(?i)_EQ.*XML", "")
                .replaceAll("(?i)_TP.*XML", "")
                .replaceAll("(?i)_SV.*XML", "");
    }

    private final String name;
    private final Path path;
    private final String basename;
    private final CompressionFormat compressionExtension;
    private final CgmesModel expected;

    private final String resourceName;
    private final String boundaryResourceName;

    private static final Logger LOG = LoggerFactory.getLogger(TestGridModel.class);
}
