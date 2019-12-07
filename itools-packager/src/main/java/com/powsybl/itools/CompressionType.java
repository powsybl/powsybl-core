/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author François Nicot <francois.nicot@rte-france.com>
 */
public enum CompressionType {
    ZIP(".zip"),
    GZIP(".tar.gz"),
    BZIP2(".tar.bz2"),
    ZIPERROR("Unexpected Compression Format value");

    private static CompressionType compressionType = null;

    CompressionType(String extension) {
        this.extension = Objects.requireNonNull(extension);
    }

    public String getExtension() {
        return extension;
    }

    public static Collection<String> getFormats() {
        return Arrays.stream(CompressionType.values())
                .map(CompressionType::name)
                .collect(Collectors.toList());
    }

    public static CompressionType compareFileExtension(String name) {
        if (name.endsWith(CompressionType.ZIP.getExtension())) {
            compressionType = ZIP;
        } else if (name.endsWith(CompressionType.GZIP.getExtension())) {
            compressionType = GZIP;
        } else if (name.endsWith(CompressionType.BZIP2.getExtension())) {
            compressionType = BZIP2;
        } else {
            compressionType = ZIPERROR;
        }
        return compressionType;
    }

    private final String extension;
}
