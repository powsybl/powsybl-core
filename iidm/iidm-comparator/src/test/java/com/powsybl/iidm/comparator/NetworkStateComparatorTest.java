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
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkStateComparatorTest {

    private static final double EPS = Math.pow(10, -15);

    private FileSystem fileSystem;

    private Network network;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        network = EurostagTutorialExample1Factory.create();
        network.getBusView().getBus("VLGEN_0").setV(24.5).setAngle(2.33);
        network.getBusView().getBus("VLHV1_0").setV(402.14).setAngle(0);
        network.getBusView().getBus("VLHV2_0").setV(389.95).setAngle(-3.5);
        network.getBusView().getBus("VLLOAD_0").setV(147.58).setAngle(9.61);
        network.getLine("NHV1_NHV2_1").getTerminal1().setP(302.4).setQ(98.7);
        network.getLine("NHV1_NHV2_1").getTerminal2().setP(-300.4).setQ(-137.1);
        network.getLine("NHV1_NHV2_2").getTerminal1().setP(302.4).setQ(98.7);
        network.getLine("NHV1_NHV2_2").getTerminal2().setP(-300.4).setQ(-137.1);
        network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal1().setP(607).setQ(225.4);
        network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2().setP(-606.3).setQ(-197.4);
        network.getTwoWindingsTransformer("NHV2_NLOAD").getTerminal1().setP(600).setQ(274.3);
        network.getTwoWindingsTransformer("NHV2_NLOAD").getTerminal2().setP(-600).setQ(-200);
        network.getGenerator("GEN").getTerminal().setP(607).setQ(225.4);
        network.getLoad("LOAD").getTerminal().setP(600).setQ(200);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void test() throws IOException {
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "other");
        network.getVariantManager().setWorkingVariant("other");
        network.getBusView().getBus("VLGEN_0").setV(25.5);
        network.getBusView().getBus("VLHV2_0").setAngle(-3.4);
        network.getBusView().getBus("VLLOAD_0").setAngle(9);

        Path xlsFile = fileSystem.getPath("/work/test.xls");
        new NetworkStateComparator(network, VariantManagerConstants.INITIAL_VARIANT_ID)
                .generateXls(xlsFile);

        try (InputStream is = Files.newInputStream(xlsFile)) {
            Workbook wb = new XSSFWorkbook(is);
            assertEquals(8, wb.getNumberOfSheets());
            Sheet busesSheet = wb.getSheet("Buses");
            Sheet linesSheet = wb.getSheet("Lines");
            Sheet transformersSheet = wb.getSheet("Transformers");
            Sheet generatorsSheet = wb.getSheet("Generators");
            Sheet hvdcConverterStationsSheet = wb.getSheet("HVDC converter stations");
            Sheet loadsSheet = wb.getSheet("Loads");
            Sheet shuntsSheet = wb.getSheet("Shunts");
            Sheet svcsSheet = wb.getSheet("Static VAR Compensators");
            assertNotNull(busesSheet);
            assertNotNull(linesSheet);
            assertNotNull(transformersSheet);
            assertNotNull(hvdcConverterStationsSheet);
            assertNotNull(generatorsSheet);
            assertNotNull(loadsSheet);
            assertNotNull(shuntsSheet);
            assertNotNull(svcsSheet);

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();

            // check buses sheet content

            // other state, v
            CellReference c3 = new CellReference("C3");
            CellReference c4 = new CellReference("C4");
            CellReference c5 = new CellReference("C5");
            CellReference c6 = new CellReference("C6");
            assertEquals(25.5, busesSheet.getRow(c3.getRow()).getCell(c3.getCol()).getNumericCellValue(), EPS);
            assertEquals(402.14, busesSheet.getRow(c4.getRow()).getCell(c4.getCol()).getNumericCellValue(), EPS);
            assertEquals(389.95, busesSheet.getRow(c5.getRow()).getCell(c5.getCol()).getNumericCellValue(), EPS);
            assertEquals(147.58, busesSheet.getRow(c6.getRow()).getCell(c6.getCol()).getNumericCellValue(), EPS);

            // other state, angle
            CellReference d3 = new CellReference("D3");
            CellReference d4 = new CellReference("D4");
            CellReference d5 = new CellReference("D5");
            CellReference d6 = new CellReference("D6");
            assertEquals(2.33, busesSheet.getRow(d3.getRow()).getCell(d3.getCol()).getNumericCellValue(), EPS);
            assertEquals(0, busesSheet.getRow(d4.getRow()).getCell(d4.getCol()).getNumericCellValue(), EPS);
            assertEquals(-3.4, busesSheet.getRow(d5.getRow()).getCell(d5.getCol()).getNumericCellValue(), EPS);
            assertEquals(9, busesSheet.getRow(d6.getRow()).getCell(d6.getCol()).getNumericCellValue(), EPS);

            // initial state, v
            CellReference e3 = new CellReference("E3");
            CellReference e4 = new CellReference("E4");
            CellReference e5 = new CellReference("E5");
            CellReference e6 = new CellReference("E6");
            assertEquals(24.5, busesSheet.getRow(e3.getRow()).getCell(e3.getCol()).getNumericCellValue(), EPS);
            assertEquals(402.14, busesSheet.getRow(e4.getRow()).getCell(e4.getCol()).getNumericCellValue(), EPS);
            assertEquals(389.95, busesSheet.getRow(e5.getRow()).getCell(e5.getCol()).getNumericCellValue(), EPS);
            assertEquals(147.58, busesSheet.getRow(e6.getRow()).getCell(e6.getCol()).getNumericCellValue(), EPS);

            // initial state, angle
            CellReference f3 = new CellReference("F3");
            CellReference f4 = new CellReference("F4");
            CellReference f5 = new CellReference("F5");
            CellReference f6 = new CellReference("F6");
            assertEquals(2.33, busesSheet.getRow(f3.getRow()).getCell(f3.getCol()).getNumericCellValue(), EPS);
            assertEquals(0, busesSheet.getRow(f4.getRow()).getCell(f4.getCol()).getNumericCellValue(), EPS);
            assertEquals(-3.5, busesSheet.getRow(f5.getRow()).getCell(f5.getCol()).getNumericCellValue(), EPS);
            assertEquals(9.61, busesSheet.getRow(f6.getRow()).getCell(f6.getCol()).getNumericCellValue(), EPS);

            // diff, v
            CellReference g3 = new CellReference("G3");
            CellReference g4 = new CellReference("G4");
            CellReference g5 = new CellReference("G5");
            CellReference g6 = new CellReference("G6");
            assertEquals(1, busesSheet.getRow(g3.getRow()).getCell(g3.getCol()).getNumericCellValue(), EPS);
            assertEquals(0, busesSheet.getRow(g4.getRow()).getCell(g4.getCol()).getNumericCellValue(), EPS);
            assertEquals(0, busesSheet.getRow(g5.getRow()).getCell(g5.getCol()).getNumericCellValue(), EPS);
            assertEquals(0, busesSheet.getRow(g6.getRow()).getCell(g6.getCol()).getNumericCellValue(), EPS);

            // diff, angle
            CellReference h3 = new CellReference("H3");
            CellReference h4 = new CellReference("H4");
            CellReference h5 = new CellReference("H5");
            CellReference h6 = new CellReference("H6");
            assertEquals(0, busesSheet.getRow(h3.getRow()).getCell(h3.getCol()).getNumericCellValue(), EPS);
            assertEquals(0, busesSheet.getRow(h4.getRow()).getCell(h4.getCol()).getNumericCellValue(), EPS);
            assertEquals(0.1, busesSheet.getRow(h5.getRow()).getCell(h5.getCol()).getNumericCellValue(), EPS);
            assertEquals(0.6099999999999994, busesSheet.getRow(h6.getRow()).getCell(h6.getCol()).getNumericCellValue(), EPS);

            // statistics footer, v
            CellReference g7 = new CellReference("G7");
            CellReference g8 = new CellReference("G8");
            CellReference g9 = new CellReference("G9");
            assertEquals(1, busesSheet.getRow(g7.getRow()).getCell(g7.getCol()).getNumericCellValue(), EPS); // max
            assertEquals(0, busesSheet.getRow(g8.getRow()).getCell(g8.getCol()).getNumericCellValue(), EPS); // min
            assertEquals(0.25, busesSheet.getRow(g9.getRow()).getCell(g9.getCol()).getNumericCellValue(), EPS); // average

            // statistics footer, angle
            CellReference h7 = new CellReference("H7");
            CellReference h8 = new CellReference("H8");
            CellReference h9 = new CellReference("H9");
            assertEquals(0.6099999999999994, busesSheet.getRow(h7.getRow()).getCell(h7.getCol()).getNumericCellValue(), EPS); // max
            assertEquals(0, busesSheet.getRow(h8.getRow()).getCell(h8.getCol()).getNumericCellValue(), EPS); // min
            assertEquals(0.17749999999999988, busesSheet.getRow(h9.getRow()).getCell(h9.getCol()).getNumericCellValue(), EPS); // average
        }
    }
}
