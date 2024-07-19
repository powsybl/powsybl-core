/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ArchiveFormatTest {
    @Test
    void test() {
        assertEquals(1, ArchiveFormat.values().length);
        assertEquals("zip", ArchiveFormat.ZIP.getExtension());

        List<String> formats = List.of(
            ArchiveFormat.ZIP.name());
        assertEquals(formats, ArchiveFormat.getFormats());
    }
}
