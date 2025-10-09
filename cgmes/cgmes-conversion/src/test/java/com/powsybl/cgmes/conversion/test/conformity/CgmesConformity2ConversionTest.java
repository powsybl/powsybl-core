/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.conformity;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity2Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesConformity2ConversionTest {

    @BeforeAll
    static void setUpBeforeClass() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        tester = new ConversionTester(
                importParams,
                TripleStoreFactory.onlyDefaultImplementation(),
                new ComparisonConfig());
    }

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void microGridBaseCaseType2Assembled() throws IOException {
        tester.testConversion(null, CgmesConformity2Catalog.microGridType2Assembled());
        // FIXME(Luma) One of the DC Links (the one that lies in BE) is not imported because it contains a DCSeriesDevice
        //   This kind of DC devices is not yet supported
        assertEquals(2, tester.lastConvertedNetwork().getVscConverterStationCount());
        assertEquals(1, tester.lastConvertedNetwork().getHvdcLineCount());
    }

    private static ConversionTester tester;

    private FileSystem fileSystem;
}
