/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ResourcesDataSource implements ReadOnlyDataSource {

    private final String resourceDir;

    private final String mainFileName;

    private final Set<String> fileNames;

    public ResourcesDataSource(String resourceDir, String mainFileName, String... fileNames) {
        this.resourceDir = Objects.requireNonNull(resourceDir);
        this.mainFileName = Objects.requireNonNull(mainFileName);
        this.fileNames = Arrays.stream(Objects.requireNonNull(fileNames))
                               .map(fileName -> checkFile(resourceDir, fileName))
                               .collect(Collectors.toSet());
    }

    private static String checkFile(String resourceDir, String fileName) {
        Objects.requireNonNull(fileName);
        String resourceName = resourceDir + fileName;
        if (ResourcesDataSource.class.getResourceAsStream(resourceName) == null) {
            throw new IllegalArgumentException("Resource '" + resourceName + "' not found");
        }
        return fileName;
    }

    @Override
    public String getMainFileName() {
        return mainFileName;
    }

    @Override
    public boolean fileExists(String fileName) {
        Objects.requireNonNull(fileName);
        return fileName.contains(fileName);
    }

    @Override
    public InputStream newInputStream(String fileName) {
        Objects.requireNonNull(fileName);
        return getClass().getResourceAsStream(resourceDir + fileName);
    }

    @Override
    public Set<String> getFileNames(String regex) {
        Pattern p = Pattern.compile(regex);
        return fileNames.stream()
                        .filter(name -> p.matcher(name).matches())
                        .collect(Collectors.toSet());
    }
}
