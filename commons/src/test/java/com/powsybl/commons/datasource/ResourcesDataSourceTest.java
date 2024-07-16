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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ResourcesDataSourceTest {

    @Test
    void testExists() {
        ResourceDataSource dataSourceWithNoMainExtension = new ResourceDataSource("foo", List.of(new ResourceSet("/test/", "foo.txt", "foo.bar")));
        assertTrue(dataSourceWithNoMainExtension.exists("foo.txt"));
        assertTrue(dataSourceWithNoMainExtension.exists(null, "bar"));
        assertTrue(dataSourceWithNoMainExtension.exists(null, "txt"));
        assertTrue(dataSourceWithNoMainExtension.isMainExtension("bar"));
        assertTrue(dataSourceWithNoMainExtension.isMainExtension("txt"));

        ResourceDataSource dataSourceWithEmptyMainExtension = new ResourceDataSource("foo", "", List.of(new ResourceSet("/test/", "foo.txt", "foo.bar")));
        assertTrue(dataSourceWithEmptyMainExtension.exists("foo.txt"));
        assertTrue(dataSourceWithEmptyMainExtension.exists(null, "bar"));
        assertTrue(dataSourceWithEmptyMainExtension.exists(null, "txt"));
        assertTrue(dataSourceWithEmptyMainExtension.isMainExtension("bar"));
        assertTrue(dataSourceWithEmptyMainExtension.isMainExtension("txt"));

        ResourceDataSource dataSourceWithMainExtension = new ResourceDataSource("foo", "txt", List.of(new ResourceSet("/test/", "foo.txt", "foo.bar")));
        assertTrue(dataSourceWithMainExtension.exists("foo.txt"));
        assertTrue(dataSourceWithMainExtension.exists(null, "bar"));
        assertTrue(dataSourceWithMainExtension.exists(null, "txt"));
        assertFalse(dataSourceWithMainExtension.isMainExtension("bar"));
        assertTrue(dataSourceWithMainExtension.isMainExtension("txt"));
    }

    @Test
    void test() {
        ResourceDataSource dataSource = new ResourceDataSource("foo", new ResourceSet("/test/", "foo.txt"));
        assertEquals("foo", dataSource.getBaseName());
        assertNull(dataSource.getMainExtension());
        assertTrue(dataSource.exists("foo.txt"));
        assertTrue(dataSource.exists(null, "txt"));
        assertFalse(dataSource.exists("foo.doc"));
        assertFalse(dataSource.exists(null, "doc"));
        assertNotNull(dataSource.newInputStream("foo.txt"));
        assertNotNull(dataSource.newInputStream(null, "txt"));
        assertEquals(Collections.singleton("foo.txt"), dataSource.listNames(".*"));
    }
}
