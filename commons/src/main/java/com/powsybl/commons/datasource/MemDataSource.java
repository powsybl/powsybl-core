/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MemDataSource implements DataSource, ReadOnlyDataSource {

    private final Map<String, byte[]> data = new HashMap<>();

    private String mainFileName;

    public MemDataSource(String mainFileName) {
        this.mainFileName = mainFileName;
    }

    public byte[] getData(String fileName) {
        return data.get(fileName);
    }

    public void putData(String fileName, InputStream data) {
        try {
            putData(fileName, ByteStreams.toByteArray(data));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void putData(String fileName, byte[] data) {
        this.data.put(fileName, data);
    }

    @Override
    public String getMainFileName() {
        return mainFileName;
    }

    @Override
    public void setMainFileName(String mainFileName) {
        this.mainFileName = mainFileName;
    }

    @Override
    public boolean fileExists(String fileName) {
        Objects.requireNonNull(fileName);
        return data.containsKey(fileName);
    }

    @Override
    public InputStream newInputStream(String fileName) {
        Objects.requireNonNull(fileName);
        byte[] ba = data.get(fileName);
        if (ba == null) {
            throw new UncheckedIOException(new IOException(fileName + " does not exist"));
        }
        return new ByteArrayInputStream(ba);
    }

    @Override
    public Set<String> getFileNames(String regex) {
        Pattern p = Pattern.compile(regex);
        return data.keySet().stream()
                .filter(name -> p.matcher(name).matches())
                .collect(Collectors.toSet());
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) {
        Objects.requireNonNull(fileName);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (append) {
            byte[] ba = getData(fileName);
            if (ba != null) {
                os.write(ba, 0, ba.length);
            }
        }
        return new ObservableOutputStream(os, fileName, new DefaultDataSourceObserver() {
            @Override
            public void closed(String streamName) {
                putData(fileName, os.toByteArray());
            }
        });
    }
}
