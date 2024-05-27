/**
 * Copyright (c) 2017, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;

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
public class ReadOnlyMemDataSource extends AbstractReadOnlyDataSource {

    private final Map<String, byte[]> data = new HashMap<>();

    private final String baseName;

    public ReadOnlyMemDataSource() {
        this("");
    }

    public ReadOnlyMemDataSource(String baseName) {
        this.baseName = Objects.requireNonNull(baseName);
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
    public boolean exists(String suffix, String ext, boolean checkConsistencyWithDataSource) throws IOException {
        return exists(DataSourceUtil.getFileName(baseName, suffix, ext), checkConsistencyWithDataSource);
    }

    @Override
    public boolean exists(String fileName, boolean checkConsistencyWithDataSource) throws IOException {
        Objects.requireNonNull(fileName);
        return (!checkConsistencyWithDataSource || isConsistentWithDataSource(fileName)) && data.containsKey(fileName);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext, boolean checkConsistencyWithDataSource) throws IOException {
        return newInputStream(DataSourceUtil.getFileName(baseName, suffix, ext), checkConsistencyWithDataSource);
    }

    @Override
    public InputStream newInputStream(String fileName, boolean checkConsistencyWithDataSource) throws IOException {
        Objects.requireNonNull(fileName);
        if (checkConsistencyWithDataSource && !isConsistentWithDataSource(fileName)) {
            throw new PowsyblException(String.format("File %s is inconsistent with the ReadOnlyMemDataSource", fileName));
        }
        byte[] ba = data.get(fileName);
        if (ba == null) {
            throw new IOException(fileName + " does not exist");
        }
        return new ByteArrayInputStream(ba);
    }

    @Override
    public Set<String> listNames(String regex) throws IOException {
        Pattern p = Pattern.compile(regex);
        // TODO: Should the list implement a filter based on basename ?
        return data.keySet().stream()
                .filter(name -> p.matcher(name).matches())
                .collect(Collectors.toSet());
    }
}
