/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import java.util.List;
import java.util.Map;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public record OdreConfig(
        String equipmentTypeColumn,
        String nullEquipmentType,
        String aerialEquipmentType,
        String undergroundEquipmentType,
        String lineId1Column,
        String lineId2Column,
        String lineId3Column,
        String lineId4Column,
        String lineId5Column,
        String geoShapeColumn,
        String substationIdColumn,
        String substationLongitudeColumn,
        String substationLatitudeColumn
) {

    public Map<String, String> idsColumnNames() {
        return Map.of(LINE_ID_KEY_1, lineId1Column,
                LINE_ID_KEY_2, lineId2Column,
                LINE_ID_KEY_3, lineId3Column,
                LINE_ID_KEY_4, lineId4Column,
                LINE_ID_KEY_5, lineId5Column);
    }

    public List<String> substationsExpectedHeaders() {
        return List.of(substationIdColumn, substationLongitudeColumn, substationLatitudeColumn);
    }

    public List<String> aerialLinesExpectedHeaders() {
        return List.of(lineId1Column, lineId2Column, lineId3Column, lineId4Column, lineId5Column, geoShapeColumn);
    }

    public List<String> undergroundLinesExpectedHeaders() {
        return List.of(lineId1Column, lineId2Column, lineId3Column, lineId4Column, lineId5Column, geoShapeColumn);
    }

    public static final String LINE_ID_KEY_1 = "id1";
    public static final String LINE_ID_KEY_2 = "id2";
    public static final String LINE_ID_KEY_3 = "id3";
    public static final String LINE_ID_KEY_4 = "id4";
    public static final String LINE_ID_KEY_5 = "id5";

    public static OdreConfig getDefaultOdreConfig() {
        return new OdreConfig(
        "Type ouvrage",
        "NULL",
        "AERIEN",
        "SOUTERRAIN",
        "Code ligne 1",
        "Code ligne 2",
        "Code ligne 3",
        "Code ligne 4",
        "Code ligne 5",
        "Geo Shape",
        "Code poste",
        "Longitude poste (DD)",
        "Latitude poste (DD)");
    }
}
