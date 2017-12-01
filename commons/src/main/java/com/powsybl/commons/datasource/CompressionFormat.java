/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public enum CompressionFormat {
    GZIP("gz"),
    BZIP2("bz2"),
    ZIP("zip");

    CompressionFormat(String extension) {
        this.extension = Objects.requireNonNull(extension);
    }

    public String getExtension() {
        return extension;
    }

    public static Collection<String> getFormats() {
        return Arrays.stream(CompressionFormat.values())
            .map(CompressionFormat::name)
            .collect(Collectors.toList());
    }

    private final String extension;
}
