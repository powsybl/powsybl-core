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

    private static void doTestExists(String filename, String cimVersion, boolean expectedExists) {
        ReadOnlyDataSource dataSource = new ResourceDataSource("incomplete",
                new ResourceSet("/", filename));
        CgmesOnDataSource cgmesOnDataSource = new CgmesOnDataSource(dataSource);
        boolean exists = "14".equals(cimVersion) ? cgmesOnDataSource.existsCim14() : cgmesOnDataSource.exists();
        assertEquals(expectedExists, exists);
    }

    private static void doTestExistsEmpty(String profile, String cimVersion, boolean expectedExists) {
        String filename = "empty_cim" + cimVersion + "_" + profile + ".xml";
        doTestExists(filename, cimVersion, expectedExists);
    }

    @Test
    public void testEQcim14() {
        doTestExistsEmpty("EQ", "14", true);
    }

    @Test
    public void testEQcim16() {
        doTestExistsEmpty("EQ", "16", true);
    }

    @Test
    public void testSVcim14() {
        doTestExistsEmpty("SV", "14", false);
    }

    @Test
    public void testCimNoRdfcim16() {
        doTestExists("validCim16InvalidContent_EQ.xml", "16", false);
    }

    @Test
    public void testCimNoRdfcim14() {
        doTestExists("validCim14InvalidContent_EQ.xml", "14", false);
    }

    @Test
    public void testRdfNoCim16() {
        doTestExists("validRdfInvalidContent_EQ.xml", "16", false);
    }

    @Test
    public void testRdfNoCim14() {
        doTestExists("validRdfInvalidContent_EQ.xml", "14", false);
    }

    @Test
    public void testRdfCim16NotExistsCim14() {
        doTestExists("empty_cim16_EQ.xml", "14", false);
    }

    @Test
    public void testRdfCim14NotExistsCim16() {
        doTestExists("empty_cim14_EQ.xml", "16", false);
    }
}
