/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ResourcesDataSourceTest {

    @Test
    void test() throws IOException {
        ResourceDataSource dataSource = new ResourceDataSource("foo", new ResourceSet("/test/", "foo.txt"));
        assertEquals("foo", dataSource.getBaseName());
        assertTrue(dataSource.exists("foo.txt"));
        assertTrue(dataSource.exists(null, "txt"));
        assertFalse(dataSource.exists("foo.doc"));
        assertFalse(dataSource.exists(null, "doc"));
        assertNotNull(dataSource.newInputStream("foo.txt"));
        assertNotNull(dataSource.newInputStream(null, "txt"));
        assertEquals(Collections.singleton("foo.txt"), dataSource.listNames(".*"));
    }
}
