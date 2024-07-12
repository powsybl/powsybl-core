/**
 * Copyright (c) 2017, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Giovanni Ferrari {@literal <giovanni.ferrari@techrain.it>}
 */
public class ReadOnlyMemDataSource implements ReadOnlyDataSource {

    private final Map<String, byte[]> data = new HashMap<>();

    private final String baseName;

    private final String mainExtension;

    public ReadOnlyMemDataSource() {
        this("", null);
    }

    public ReadOnlyMemDataSource(String baseName) {
        this(baseName, null);
    }

    public ReadOnlyMemDataSource(String baseName, String mainExtension) {
        this.baseName = Objects.requireNonNull(baseName);
        this.mainExtension = mainExtension;
    }

    public byte[] getData(String suffix, String ext) {
        return getData(DataSourceUtil.getFileName(baseName, suffix, ext));
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
    public String getBaseName() {
        return baseName;
    }

    @Override
    public String getMainExtension() {
        return mainExtension;
    }

    @Override
    public boolean exists(String suffix, String ext) throws IOException {
        return exists(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public boolean existsStrict(String suffix, String ext) throws IOException {
        return (mainExtension == null || mainExtension.isEmpty() || mainExtension.equals(ext))
            && exists(suffix, ext);
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        Objects.requireNonNull(fileName);
        return data.containsKey(fileName);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        return newInputStream(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        Objects.requireNonNull(fileName);
        byte[] ba = data.get(fileName);
        if (ba == null) {
            throw new IOException(fileName + " does not exist");
        }
        return new ByteArrayInputStream(ba);
    }

    @Override
    public Set<String> listNames(String regex) throws IOException {
        Pattern p = Pattern.compile(regex);
        return data.keySet().stream()
                .filter(name -> p.matcher(name).matches())
                .collect(Collectors.toSet());
    }
}
