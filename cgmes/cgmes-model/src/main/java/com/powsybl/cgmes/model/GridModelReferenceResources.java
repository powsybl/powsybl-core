/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class GridModelReferenceResources extends AbstractGridModelReference {

    public GridModelReferenceResources(
            String name,
            CgmesModel expected,
            ResourceSet... resourceSets) {
        super(name, expected);
        this.resourceSets = resourceSets;
    }

    @Override
    public ReadOnlyDataSource dataSource() {
        ReadOnlyDataSource ds = new ResourceDataSource("", resourceSets);
        if (LOG.isInfoEnabled()) {
            try {
                LOG.info("List of names in data source for {} = {}", name(), Arrays.toString(ds.listNames(".*").toArray()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return ds;
    }

    private String baseNameFromResourceNames() {
        for (ResourceSet resourceSet : resourceSets) {
            for (String fileName : resourceSet.getFileNames()) {
                // Ignore resources related to boundaries
                if (fileName.contains("Boundary") || fileName.contains("_BD_")) {
                    continue;
                }
                // From last component of resource name,
                // remove all subset suffixes and XML extension
                return fileName.replaceAll("^.*\\/", "").replaceAll("(?i)_(EQ|TP|SV|SSH|DL|GL|DY).*XML", "");
            }
        }
        throw new CgmesModelException("Data source does not contain valid data");
    }

    private final ResourceSet[] resourceSets;

    private static final Logger LOG = LoggerFactory.getLogger(GridModelReferenceResources.class);
}
