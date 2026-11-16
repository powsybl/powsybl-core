/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public enum ArchiveFormat {
    ZIP("zip"),
    TAR("tar");

    ArchiveFormat(String extension) {
        this.extension = Objects.requireNonNull(extension);
    }

    public String getExtension() {
        return extension;
    }

    public static Collection<String> getFormats() {
        return Arrays.stream(ArchiveFormat.values())
            .map(ArchiveFormat::name)
            .toList();
    }

    private final String extension;
}
