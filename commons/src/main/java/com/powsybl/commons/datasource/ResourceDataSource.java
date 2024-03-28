/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ResourceDataSource implements ReadOnlyDataSource {

    private final String baseName;

    private final List<ResourceSet> resourceSets;

    public ResourceDataSource(String baseName, ResourceSet... resourceSets) {
        this(baseName, Arrays.asList(resourceSets));
    }

    public ResourceDataSource(String baseName, List<ResourceSet> resourceSets) {
        this.baseName = Objects.requireNonNull(baseName);
        this.resourceSets = Objects.requireNonNull(resourceSets);
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    @Override
    public boolean exists(String suffix, String ext) {
        return exists(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public boolean exists(String fileName) {
        Objects.requireNonNull(fileName);
        return resourceSets.stream().anyMatch(resourceSet -> resourceSet.getFileNames().contains(fileName));
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) {
        return newInputStream(DataSourceUtil.getFileName(baseName, suffix, ext));
    }

    @Override
    public InputStream newInputStream(String fileName) {
        Objects.requireNonNull(fileName);
        return resourceSets.stream().filter(resourceSet -> resourceSet.exists(fileName))
                .map(resourceSet -> resourceSet.newInputStream(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("File '" + fileName + "' not found"));
    }

    @Override
    public Set<String> listNames(String regex) {
        Pattern p = Pattern.compile(regex);
        return resourceSets.stream()
                .flatMap(resourceSet -> resourceSet.getFileNames().stream())
                .filter(name -> p.matcher(name).matches())
                .collect(Collectors.toSet());
    }
}
