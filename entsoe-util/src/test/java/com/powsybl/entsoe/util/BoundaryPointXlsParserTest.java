/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Country;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class BoundaryPointXlsParserTest {

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
    void test() throws IOException {

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
    void testMissingBoundaryPointFile() {
        BoundaryPointXlsParser parser = new BoundaryPointXlsParser();
        PowsyblException e = assertThrows(PowsyblException.class, parser::parseDefault);
        assertEquals("Boundary point sheet /work/unittests/BoundaryPoint.xls not found", e.getMessage());
    }
}
