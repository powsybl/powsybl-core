/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.test;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourcesDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;

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

    private InputStream resourceStream(String resource) {
        return getClass().getClassLoader().getResourceAsStream(resource);
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
        ResourcesDataSource ds = new ResourcesDataSource("/", null, resourceNames);
        if (LOG.isInfoEnabled()) {
            LOG.info("List of names in data source for {} = {}", name(), Arrays.toString(ds.getFileNames(".*").toArray()));
        }
        return ds;
    }

    private final String[] resourceNames;

    private static final Logger LOG = LoggerFactory.getLogger(TestGridModelResources.class);
}
