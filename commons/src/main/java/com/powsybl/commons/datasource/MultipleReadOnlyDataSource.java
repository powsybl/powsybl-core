/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.PowsyblException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MultipleReadOnlyDataSource implements ReadOnlyDataSource {

    private final String baseName;

    private final List<ReadOnlyDataSource> dataSources;

    public MultipleReadOnlyDataSource(ReadOnlyDataSource... dataSource) {
        this(List.of(dataSource));
    }

    public MultipleReadOnlyDataSource(List<ReadOnlyDataSource> dataSources) {
        this(null, dataSources);
    }

    public MultipleReadOnlyDataSource(String baseName, List<ReadOnlyDataSource> dataSources) {
        this.dataSources = Objects.requireNonNull(dataSources);
        if (baseName == null && !dataSources.isEmpty()) {
            this.baseName = dataSources.get(0).getBaseName();
        } else {
            if (baseName != null) {
                this.baseName = baseName;
            } else {
                throw new PowsyblException("A base name should be provided");
            }
        }
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    @Override
    public boolean exists(String suffix, String ext) throws IOException {
        return dataSources.stream().anyMatch(dataSource -> {
            try {
                return dataSource.exists(suffix, ext);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        return dataSources.stream().anyMatch(dataSource -> {
            try {
                return dataSource.exists(fileName);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        for (var dataSource : dataSources) {
            if (dataSource.exists(suffix, ext)) {
                return dataSource.newInputStream(suffix, ext);
            }
        }
        return null;
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        for (var dataSource : dataSources) {
            if (dataSource.exists(fileName)) {
                return dataSource.newInputStream(fileName);
            }
        }
        return null;
    }

    @Override
    public Set<String> listNames(String regex) throws IOException {
        Set<String> names = new LinkedHashSet<>();
        for (var dataSource : dataSources) {
            names.addAll(dataSource.listNames(regex));
        }
        return names;
    }
}
