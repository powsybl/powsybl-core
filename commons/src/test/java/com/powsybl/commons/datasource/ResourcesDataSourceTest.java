/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ResourcesDataSourceTest {

    @Test
    public void test() {
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
