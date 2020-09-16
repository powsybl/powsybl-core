/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import java.io.UncheckedIOException;
import java.nio.file.NotDirectoryException;

import com.powsybl.commons.datastore.DataStore;
import com.powsybl.commons.datastore.DirectoryDataStore;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class StoreDataSourceTest extends AbstractDataSourceTest {

    @Override
    protected DataSource createDataSource() {
        DataStore store;
        try {
            store = new DirectoryDataStore(testDir);
        } catch (NotDirectoryException e) {
            throw new UncheckedIOException(e);
        }
        return new StoreDataSource(store, getBaseName());
    }
}
