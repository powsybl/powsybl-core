/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.comparator;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkStateComparatorTest {

    private FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "other");
        Path xlsFile = fileSystem.getPath("/work/test.xls");
        new NetworkStateComparator(network, "other")
                .generateXls(xlsFile);

        try (InputStream is = Files.newInputStream(xlsFile)) {
            Workbook wb = new XSSFWorkbook(is);
            assertEquals(6, wb.getNumberOfSheets());
            Sheet busesSheet = wb.getSheet("Buses");
            Sheet linesSheet = wb.getSheet("Lines");
            Sheet transformersSheet = wb.getSheet("Transformers");
            Sheet busesGenerators = wb.getSheet("Generators");
            Sheet busesLoads = wb.getSheet("Loads");
            Sheet busesShunts = wb.getSheet("Shunts");
            assertNotNull(busesSheet);
            assertNotNull(linesSheet);
            assertNotNull(transformersSheet);
            assertNotNull(busesGenerators);
            assertNotNull(busesLoads);
            assertNotNull(busesShunts);
        }
    }
}
