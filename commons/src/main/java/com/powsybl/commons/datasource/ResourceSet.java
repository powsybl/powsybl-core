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

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ResourceSet {

    private final String dir;

    private final List<String> fileNames;

    public ResourceSet(String dir, String... fileNames) {
        this(dir, Arrays.asList(fileNames));
    }

    public ResourceSet(String dir, List<String> fileNames) {
        this.dir = Objects.requireNonNull(dir);
        this.fileNames = Objects.requireNonNull(fileNames);
        // check resources exist
        for (String fileName : fileNames) {
            String resourceName = getResourceName(dir, fileName);
            if (getClass().getResourceAsStream(resourceName) == null) {
                throw new IllegalArgumentException("Resource '" + resourceName + "' not found");
            }
        }
    }

    private static String getResourceName(String dir, String fileName) {
        return dir.endsWith("/") ? dir + fileName : dir + "/" + fileName;
    }

    public String getDir() {
        return dir;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public boolean exists(String fileName) {
        Objects.requireNonNull(fileName);
        return fileNames.contains(fileName);
    }

    public InputStream newInputStream(String fileName) {
        Objects.requireNonNull(fileName);
        if (!fileNames.contains(fileName)) {
            throw new IllegalArgumentException("File '" + fileName + "' not found");
        }
        return getClass().getResourceAsStream(getResourceName(dir, fileName));
    }
}
