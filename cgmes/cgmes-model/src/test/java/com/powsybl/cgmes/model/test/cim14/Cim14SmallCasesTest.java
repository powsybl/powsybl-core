/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.test.cim14;

import com.powsybl.cgmes.model.CgmesOnDataSource;
import com.powsybl.cgmes.model.test.CgmesModelTester;
import com.powsybl.cgmes.model.test.TestGridModelResources;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Cim14SmallCasesTest {

    @Test
    public void small1() {
        new CgmesModelTester(Cim14SmallCasesCatalog.small1()).test();
    }

    @Test
    public void small1PlusInvalidFileContent() throws IOException {
        TestGridModelResources t = Cim14SmallCasesCatalog.small1PlusInvalidFileContent();
        // The data source contains invalid files
        CgmesOnDataSource c = new CgmesOnDataSource(t.dataSource());
        assertTrue(t.dataSource().listNames(".*").containsAll(INVALID_CONTENT_FILES));
        // And they are correctly ignored as valid CIM content
        assertFalse(c.names().containsAll(INVALID_CONTENT_FILES));
        // The test case ignoring the invalid content is handled correctly
        new CgmesModelTester(t).test();
    }

    @Test
    public void m7Buses() {
        new CgmesModelTester(Cim14SmallCasesCatalog.m7buses()).test();
    }

    @Test
    public void ieee14() {
        new CgmesModelTester(Cim14SmallCasesCatalog.ieee14()).test();
    }

    @Test
    public void nordic32() {
        new CgmesModelTester(Cim14SmallCasesCatalog.nordic32()).test();
    }

    private static final String[] INVALID_CONTENT_FILES_VALUES = new String[] {
        "invalidContent_EQ.notxml",
        "invalidContent_EQ.xml",
        "validRdfInvalidContent_EQ.xml",
        "validCim14InvalidContent_EQ.xml"
    };
    private static final Set<String> INVALID_CONTENT_FILES = new HashSet<>(Arrays.asList(INVALID_CONTENT_FILES_VALUES));

}
