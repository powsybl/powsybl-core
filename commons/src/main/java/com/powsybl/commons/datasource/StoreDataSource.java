/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.powsybl.commons.datastore.DataStore;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class StoreDataSource implements DataSource {

    private final DataStore store;
    private final String baseName;

    public StoreDataSource(DataStore store, String baseName) {
        this.store = Objects.requireNonNull(store);
        this.baseName = Objects.requireNonNull(baseName);
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    @Override
    public boolean exists(String suffix, String ext) throws IOException {
        return store.exists(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        return store.exists(fileName);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return store.newInputStream(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        return store.newInputStream(fileName);
    }

    @Override
    public Set<String> listNames(String regex) throws IOException {
        return new HashSet<>(store.getEntryNames());
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
        return store.newOutputStream(fileName, append);
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        return store.newOutputStream(DataSourceUtil.getFileName(baseName, suffix, ext), append);
    }

}
