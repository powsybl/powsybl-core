/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.model;

import org.junit.Test;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;

import static org.junit.Assert.*;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class CgmesOnDataSourceTest {

    private void doTestExists(String profile, String cimVersion, boolean expectedExists) {
        ReadOnlyDataSource dataSource = new ResourceDataSource("incomplete",
                new ResourceSet("/", "empty_cim" + cimVersion + "_" + profile + ".xml"));
        CgmesOnDataSource cgmesOnDataSource = new CgmesOnDataSource(dataSource);
        boolean exists = "14".equals(cimVersion) ? cgmesOnDataSource.existsCim14() : cgmesOnDataSource.exists();
        assertEquals(expectedExists, exists);
    }

    @Test
    public void testEQcim14() {
        doTestExists("EQ", "14", true);
    }

    @Test
    public void testEQcim16() {
        doTestExists("EQ", "16", true);
    }

    @Test
    public void testSVcim14() {
        doTestExists("SV", "14", false);
    }

    @Test
    public void testSVcim16() {
        doTestExists("SV", "16", false);
    }
}
