/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class DataSourceUtilTest {

    @Test
    public void testGetBaseName() {
        assertEquals("dummy", DataSourceUtil.getBaseName("dummy.xml.gz"));
        assertEquals("dummy", DataSourceUtil.getBaseName("dummy.gz"));
        assertEquals("dummy", DataSourceUtil.getBaseName("dummy"));
    }
}
