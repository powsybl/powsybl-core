/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.IOUtils;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DefaultDataSourceObserver;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ObservableOutputStream;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class MemDataStore implements DataStore {

    private final Map<String, byte[]> entries = new HashMap<>();

    @Override
    public List<String> getEntryNames() throws IOException {
        return entries.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public boolean exists(String entryName) {
        return entries.containsKey(entryName);
    }

    @Override
    public InputStream newInputStream(String entryName) throws IOException {
        return new ByteArrayInputStream(entries.get(entryName));
    }

    @Override
    public OutputStream newOutputStream(String entryName, boolean append) throws IOException {
        ByteArrayOutputStream os =  new ByteArrayOutputStream();
        if (append) {
            byte[] ba = entries.get(entryName);
            if (ba != null) {
                os.write(ba, 0, ba.length);
            }
        }
        return new ObservableOutputStream(os, entryName, new DefaultDataSourceObserver() {
            @Override
            public void closed(String streamName) {
                entries.put(entryName, os.toByteArray());
            }
        });
    }

    @Override
    public DataSource toDataSource(String filename) {
        MemDataSource dataSource = new MemDataSource();
        entries.forEach((k, v) -> {
            try (OutputStream out = dataSource.newOutputStream(k, false)) {
                IOUtils.copy(new ByteArrayInputStream(v), out);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        return dataSource;
    }

}
