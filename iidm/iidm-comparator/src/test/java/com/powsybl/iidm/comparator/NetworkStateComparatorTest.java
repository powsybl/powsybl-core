/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.comparator;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NetworkStateComparatorTest {

    private static final double EPS = Math.pow(10, -15);

    private FileSystem fileSystem;

    private Network network;

    @BeforeEach
    void setUp() {
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

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() throws IOException {
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
            assertEquals(9, wb.getNumberOfSheets());
            Sheet busesSheet = wb.getSheet("Buses");
            Sheet linesSheet = wb.getSheet("Lines");
            Sheet twoWindingsTransformersSheet = wb.getSheet("2WindingsTransformers");
            Sheet threeWindingsTransformersSheet = wb.getSheet("3WindingsTransformers");
            Sheet generatorsSheet = wb.getSheet("Generators");
            Sheet hvdcConverterStationsSheet = wb.getSheet("HVDC converter stations");
            Sheet loadsSheet = wb.getSheet("Loads");
            Sheet shuntsSheet = wb.getSheet("Shunts");
            Sheet svcsSheet = wb.getSheet("Static VAR Compensators");
            assertNotNull(busesSheet);
            assertNotNull(linesSheet);
            assertNotNull(twoWindingsTransformersSheet);
            assertNotNull(threeWindingsTransformersSheet);
            assertNotNull(hvdcConverterStationsSheet);
            assertNotNull(generatorsSheet);
            assertNotNull(loadsSheet);
            assertNotNull(shuntsSheet);
            assertNotNull(svcsSheet);

            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();

            // check buses sheet content

            // other state, v
            assertNumericCellEquals(busesSheet, "C3", 25.5);
            assertNumericCellEquals(busesSheet, "C4", 402.14);
            assertNumericCellEquals(busesSheet, "C5", 389.95);
            assertNumericCellEquals(busesSheet, "C6", 147.58);

            // other state, angle
            assertNumericCellEquals(busesSheet, "D3", 2.33);
            assertNumericCellEquals(busesSheet, "D4", 0);
            assertNumericCellEquals(busesSheet, "D5", -3.4);
            assertNumericCellEquals(busesSheet, "D6", 9);

            // initial state, v
            assertNumericCellEquals(busesSheet, "E3", 24.5);
            assertNumericCellEquals(busesSheet, "E4", 402.14);
            assertNumericCellEquals(busesSheet, "E5", 389.95);
            assertNumericCellEquals(busesSheet, "E6", 147.58);

            // initial state, angle
            assertNumericCellEquals(busesSheet, "F3", 2.33);
            assertNumericCellEquals(busesSheet, "F4", 0);
            assertNumericCellEquals(busesSheet, "F5", -3.5);
            assertNumericCellEquals(busesSheet, "F6", 9.61);

            // diff, v
            assertNumericCellEquals(busesSheet, "G3", 1);
            assertNumericCellEquals(busesSheet, "G4", 0);
            assertNumericCellEquals(busesSheet, "G5", 0);
            assertNumericCellEquals(busesSheet, "G6", 0);

            // diff, angle
            assertNumericCellEquals(busesSheet, "H3", 0);
            assertNumericCellEquals(busesSheet, "H4", 0);
            assertNumericCellEquals(busesSheet, "H5", 0.1);
            assertNumericCellEquals(busesSheet, "H6", 0.6099999999999994);

            // statistics footer, v
            assertNumericCellEquals(busesSheet, "G7", 1); // max
            assertNumericCellEquals(busesSheet, "G8", 0); // min
            assertNumericCellEquals(busesSheet, "G9", 0.25); // average

            // statistics footer, angle
            assertNumericCellEquals(busesSheet, "H7", 0.6099999999999994); // max
            assertNumericCellEquals(busesSheet, "H8", 0); // min
            assertNumericCellEquals(busesSheet, "H9", 0.17749999999999988); // average
        }
    }

    @Test
    void testThreeWindings() throws IOException {
        Network threeWindingsTransformersNetwork = ThreeWindingsTransformerNetworkFactory.create();
        var twt = threeWindingsTransformersNetwork.getThreeWindingsTransformer("3WT");
        twt.getLeg1().newPhaseTapChanger().setTapPosition(0)
                .beginStep()
                .setR(0)
                .setX(0.1f)
                .setG(0)
                .setB(0)
                .setRho(1)
                .setAlpha(1)
                .endStep()
                .beginStep()
                .setR(0)
                .setX(0.1f)
                .setG(0)
                .setB(0)
                .setRho(1.01)
                .setAlpha(2)
                .endStep()
                .add();
        // values set below are only for test and do not reflect any physical reality
        twt.getTerminal(ThreeSides.ONE).setP(1).setQ(2);
        twt.getTerminal(ThreeSides.TWO).setP(3).setQ(4);
        twt.getTerminal(ThreeSides.THREE).setP(5).setQ(6);
        twt.getLeg1().getPhaseTapChanger().setTapPosition(1);
        twt.getLeg2().getRatioTapChanger().setTapPosition(0);
        twt.getLeg3().getRatioTapChanger().setTapPosition(0);

        threeWindingsTransformersNetwork.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "other");
        threeWindingsTransformersNetwork.getVariantManager().setWorkingVariant("other");
        twt.getTerminal(ThreeSides.ONE).setP(10).setQ(20);
        twt.getTerminal(ThreeSides.TWO).setP(30).setQ(40);
        twt.getTerminal(ThreeSides.THREE).setP(50).setQ(50);
        twt.getLeg1().getPhaseTapChanger().setTapPosition(0);
        twt.getLeg2().getRatioTapChanger().setTapPosition(1);
        twt.getLeg3().getRatioTapChanger().setTapPosition(2);
        Path xlsFile = fileSystem.getPath("/work/test3wt.xls");
        new NetworkStateComparator(threeWindingsTransformersNetwork, VariantManagerConstants.INITIAL_VARIANT_ID)
                .generateXls(xlsFile);

        try (InputStream is = Files.newInputStream(xlsFile)) {
            Workbook wb = new XSSFWorkbook(is);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();
            Sheet threeWindingsTransformersSheet = wb.getSheet("3WindingsTransformers");
            assertNotNull(threeWindingsTransformersSheet);
            assertNumericCellEquals(threeWindingsTransformersSheet, "C3", 10.0); // p1, other
            assertNumericCellEquals(threeWindingsTransformersSheet, "O3", 1.0); // p1, init
            assertNumericCellEquals(threeWindingsTransformersSheet, "AA3", 9.0); // p1, diff

            assertNumericCellEquals(threeWindingsTransformersSheet, "D3", 30.0); // q1, other
            assertNumericCellEquals(threeWindingsTransformersSheet, "P3", 3.0); // q1, init
            assertNumericCellEquals(threeWindingsTransformersSheet, "AB3", 27.0); // q1, diff

            assertNumericCellEquals(threeWindingsTransformersSheet, "L3", 0.0); // ptc1, other
            assertNumericCellEquals(threeWindingsTransformersSheet, "X3", 1.0); // ptc1, init
            assertNumericCellEquals(threeWindingsTransformersSheet, "AJ3", 1.0); // ptc1, diff

            assertNumericCellEquals(threeWindingsTransformersSheet, "K3", 2.0); // rtc3, other
            assertNumericCellEquals(threeWindingsTransformersSheet, "W3", 0.0); // rtc3, init
            assertNumericCellEquals(threeWindingsTransformersSheet, "AI3", 2.0); // rtc3, diff
        }
    }

    private static void assertNumericCellEquals(Sheet sheet, String cell, double expected) {
        CellReference c = new CellReference(cell);
        assertEquals(expected, sheet.getRow(c.getRow()).getCell(c.getCol()).getNumericCellValue(), EPS);
    }
}
