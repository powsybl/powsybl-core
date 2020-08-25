/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import java.io.IOException;
import java.io.OutputStream;

import com.powsybl.commons.datastore.DataStore;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class StoreDataSource extends ReadOnlyStoreDataSource implements DataSource {

    private final DataStore store;

    public StoreDataSource(DataStore store, String baseName) {
        super(store, baseName);
        this.store = store;
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
        return store.newOutputStream(fileName, append);
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        return store.newOutputStream(DataSourceUtil.getFileName(getBaseName(), suffix, ext), append);
    }

}
