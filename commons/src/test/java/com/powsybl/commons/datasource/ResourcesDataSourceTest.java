/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ResourcesDataSourceTest extends AbstractReadOnlyDataSourceTest {

    @Test
    void testExists() {
        ResourceDataSource dataSourceWithNoDataExtension = new ResourceDataSource("foo", List.of(new ResourceSet("/test/", "foo.txt", "foo.bar")));
        assertTrue(dataSourceWithNoDataExtension.exists("foo.txt"));
        assertTrue(dataSourceWithNoDataExtension.exists(null, "bar"));
        assertTrue(dataSourceWithNoDataExtension.exists(null, "txt"));
        assertTrue(dataSourceWithNoDataExtension.isDataExtension("bar"));
        assertTrue(dataSourceWithNoDataExtension.isDataExtension("txt"));

        ResourceDataSource dataSourceWithEmptyDataExtension = new ResourceDataSource("foo", "", List.of(new ResourceSet("/test/", "foo.txt", "foo.bar")));
        assertTrue(dataSourceWithEmptyDataExtension.exists("foo.txt"));
        assertTrue(dataSourceWithEmptyDataExtension.exists(null, "bar"));
        assertTrue(dataSourceWithEmptyDataExtension.exists(null, "txt"));
        assertTrue(dataSourceWithEmptyDataExtension.isDataExtension("bar"));
        assertTrue(dataSourceWithEmptyDataExtension.isDataExtension("txt"));

        ResourceDataSource dataSourceWithDataExtension = new ResourceDataSource("foo", "txt", List.of(new ResourceSet("/test/", "foo.txt", "foo.bar")));
        assertTrue(dataSourceWithDataExtension.exists("foo.txt"));
        assertTrue(dataSourceWithDataExtension.exists(null, "bar"));
        assertTrue(dataSourceWithDataExtension.exists(null, "txt"));
        assertFalse(dataSourceWithDataExtension.isDataExtension("bar"));
        assertTrue(dataSourceWithDataExtension.isDataExtension("txt"));
    }

    @Test
    void test() {
        ResourceDataSource dataSource = new ResourceDataSource("foo", new ResourceSet("/test/", "foo.txt"));
        assertEquals("foo", dataSource.getBaseName());
        assertNull(dataSource.getDataExtension());
        assertTrue(dataSource.exists("foo.txt"));
        assertTrue(dataSource.exists(null, "txt"));
        assertFalse(dataSource.exists("foo.doc"));
        assertFalse(dataSource.exists(null, "doc"));
        assertNotNull(dataSource.newInputStream("foo.txt"));
        assertNotNull(dataSource.newInputStream(null, "txt"));
        assertEquals(Collections.singleton("foo.txt"), dataSource.listNames(".*"));
    }

    @Override
    protected ResourceDataSource createDataSourceForPolynomialRegexTest() {
        String filename = "a".repeat(100) + "b";
        ResourceSet resourceSet = new ResourceSet("/test/", filename);
        return new ResourceDataSource("test", List.of(resourceSet));
    }
}
