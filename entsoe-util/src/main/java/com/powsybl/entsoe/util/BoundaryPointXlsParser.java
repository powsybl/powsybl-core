/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.Country;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BoundaryPointXlsParser {

    private static Country toCountry(String border) {
        switch (border) {
            case "Albania": return Country.AL;
            case "Austria": return Country.AT;
            case "Belarus": return Country.BY;
            case "Belgium": return Country.BE;
            case "Bosnia Herzegovina": return Country.BA;
            case "Bulgaria": return Country.BG;
            case "Croatia": return Country.HR;
            case "Czech Republic": return Country.CZ;
            case "Denmark": return Country.DK;
            case "Estonia": return Country.EE;
            case "Finland": return Country.FI;
            case "France": return Country.FR;
            case "Germany": return Country.DE;
            case "Greece": return Country.GR;
            case "Hungary": return Country.HU;
            case "IE": return Country.IE;
            case "Italy": return Country.IT;
            case "Latvia": return Country.LV;
            case "Lithuania": return Country.LT;
            case "Luxembourg": return Country.LU;
            case "Montenegro": return Country.ME;
            case "Netherlands": return Country.NL;
            case "Norway": return Country.NO;
            case "Poland": return Country.PL;
            case "Romania": return Country.RO;
            case "Serbia": return Country.RS;
            case "Slovakia": return Country.SK;
            case "Slovenia": return Country.SI;
            case "Spain": return Country.ES;
            case "Sweden": return Country.SE;
            case "Switzerland": return Country.CH;
            case "The Former Yugoslav Republic of Macedonia": return Country.MK;
            case "Tunisia": return Country.TN;
            case "Turkey": return Country.TR;
            case "United Kingdom": return Country.GB;
            case "Russian Federation": return Country.RU;
            case "Portugal": return Country.PT;
            case "Ukraine": return Country.UA;
            case "Morocco": return Country.MA;
            case "Republic of Moldova": return Country.MD;
            case "Cyprus": return Country.CY;
            default: throw new AssertionError(border);
        }
    }

    public Map<String, BoundaryPoint> parse(InputStream is) throws IOException {
        Map<String, BoundaryPoint> boundaryPoints = new HashMap<>();
        HSSFWorkbook workbook = new HSSFWorkbook(is);
        HSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next();
        rowIterator.next();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell boundaryPointNameCell = row.getCell(13);
            Cell borderFromCell = row.getCell(14);
            Cell borderToCell = row.getCell(15);
            String boundaryPointName = boundaryPointNameCell.getStringCellValue();
            if (boundaryPointName.equals("-")) {
                continue;
            }
            Country borderFrom = toCountry(borderFromCell.getStringCellValue());
            Country borderTo = toCountry(borderToCell.getStringCellValue());
            boundaryPoints.put(boundaryPointName, new BoundaryPoint(boundaryPointName, borderFrom, borderTo));
        }
        return boundaryPoints;
    }

    public Map<String, BoundaryPoint> parseDefault() throws IOException {
        Path defaultBoundaryPoint = PlatformConfig.defaultConfig().getConfigDir().resolve("BoundaryPoint.xls");
        if (!Files.exists(defaultBoundaryPoint)) {
            throw new PowsyblException("Boundary point sheet " + defaultBoundaryPoint + " not found");
        }
        try (InputStream is = Files.newInputStream(defaultBoundaryPoint)) {
            return parse(is);
        }
    }

}
