/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.Country;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class BoundaryPointXlsParserTest {

    private HSSFWorkbook createWorkbook() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();

        // Add dummy lines
        sheet.createRow(0).createCell(0).setCellValue("First dummy row");
        sheet.createRow(1).createCell(0).setCellValue("Second dummy row");

        HSSFRow row = sheet.createRow(2);
        row.createCell(13).setCellValue("BoundaryPoint FR-BE");
        row.createCell(14).setCellValue("France");
        row.createCell(15).setCellValue("Belgium");

        return workbook;
    }

    @Test
    public void test() throws IOException {

        byte[] buffer;
        try (HSSFWorkbook workbook = createWorkbook();
             ByteArrayOutputStream stream = new ByteArrayOutputStream(1024)) {
            workbook.write(stream);
            stream.flush();
            buffer = stream.toByteArray();
        }

        Map<String, BoundaryPoint> boundaryPoints;
        try (InputStream stream = new ByteArrayInputStream(buffer)) {
            BoundaryPointXlsParser parser = new BoundaryPointXlsParser();
            boundaryPoints = parser.parse(stream);
        }

        assertEquals(1, boundaryPoints.size());
        BoundaryPoint point = boundaryPoints.get("BoundaryPoint FR-BE");
        assertNotNull(point);
        assertEquals("BoundaryPoint FR-BE", point.getName());
        assertEquals(Country.FR, point.getBorderFrom());
        assertEquals(Country.BE, point.getBorderTo());
    }

    @Test
    public void testMissingBoundaryPointFile() {
        BoundaryPointXlsParser parser = new BoundaryPointXlsParser();

        PlatformConfig mockedPc = Mockito.mock(PlatformConfig.class);
        Mockito.when(mockedPc.getConfigDir()).thenReturn(Optional.empty());

        try (MockedStatic<PlatformConfig> staticMockedPc = Mockito.mockStatic(PlatformConfig.class)) {
            staticMockedPc.when(PlatformConfig::defaultConfig).thenReturn(mockedPc);
            PowsyblException e = assertThrows(PowsyblException.class, parser::parseDefault);
            assertEquals("Cannot access boundary point sheet as configuration directory is not defined in platform config", e.getMessage());
        }

        PowsyblException e = assertThrows(PowsyblException.class, parser::parseDefault);
        assertEquals("Boundary point sheet /work/unittests/BoundaryPoint.xls not found", e.getMessage());
    }
}
