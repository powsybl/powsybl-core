/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TestGridModelResources extends AbstractTestGridModel {

    public TestGridModelResources(
            String name,
            CgmesModel expected,
            String... resourceNames) {
        super(name, expected);
        this.resourceNames = resourceNames;
    }

    @Override
    public boolean exists() {
        for (String r : resourceNames) {
            if (resourceStream(r) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ReadOnlyDataSource dataSource() {
        if (resourceNames.length == 1) {
            return DataSourceUtil.createReadOnlyMemDataSource(resourceNames[0], resourceStream(resourceNames[0]));
        } else {
            throw new UnsupportedOperationException(
                    "dataSource() from multiple resources not supported, use dataSourceBasedOn(FileSystem)");
        }
    }

    @Override
    public DataSource dataSourceBasedOn(FileSystem fs) throws IOException {
        Path folder = Files.createDirectory(fs.getPath(name()));
        DataSource ds = new FileDataSource(folder, baseNameFromResourceNames());
        for (String r : resourceNames) {
            // Take last component of the resource name
            String r1 = r.replaceAll("^.*\\/", "");
            try (InputStream is = resourceStream(r);
                    OutputStream os = ds.newOutputStream(r1, false)) {
                ByteStreams.copy(is, os);
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("List of names in data source for {} = {}", name(), Arrays.toString(ds.listNames(".*").toArray()));
        }
        return ds;
    }

    private InputStream resourceStream(String resource) {
        return getClass().getClassLoader().getResourceAsStream(resource);
    }

    private String baseNameFromResourceNames() {
        for (String r : resourceNames) {
            // Ignore resources related to boundaries
            if (r.contains("Boundary") || r.contains("_BD_")) {
                continue;
            }
            // From last component of resource name,
            // remove all subset suffixes and XML extension
            return r.replaceAll("^.*\\/", "").replaceAll("(?i)_(EQ|TP|SV|SSH|DL|GL|DY).*XML", "");
        }
        throw new CgmesModelException("Data source does not contain valid data");
    }

    private final String[] resourceNames;

    private static final Logger LOG = LoggerFactory.getLogger(TestGridModelResources.class);
}
