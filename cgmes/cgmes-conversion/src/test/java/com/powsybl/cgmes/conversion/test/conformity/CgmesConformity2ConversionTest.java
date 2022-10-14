/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.conformity;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity2Catalog;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.junit.Assert.assertEquals;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity2ConversionTest {

    @BeforeClass
    public static void setUpBeforeClass() {
        tester = new ConversionTester(
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
    }

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void microGridBaseCaseType2Assembled() throws IOException {
        tester.testConversion(null, CgmesConformity2Catalog.microGridType2Assembled());
        // FIXME(Luma) One of the DC Links (the one that lies in BE) is not imported because it contains a DCSeriesDevice
        //   This kind of DC devices is not yet supported
        assertEquals(2, tester.lastConvertedNetwork().getVscConverterStationCount());
        assertEquals(1, tester.lastConvertedNetwork().getHvdcLineCount());
    }

    private static ConversionTester tester;

    private FileSystem fileSystem;
}
