/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.dto;

import com.powsybl.iidm.network.extensions.Coordinate;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class LineGeoDataTest {

    @Test
    public void test() {
        LineGeoData lineGeoData = new LineGeoData("l", "FR", "FR", "ALAMO", "CORAL", Collections.<Coordinate>emptyList());

        assertEquals("l", lineGeoData.getId());
        assertEquals("FR", lineGeoData.getCountry1());
        assertEquals("FR", lineGeoData.getCountry2());
        assertEquals("ALAMO", lineGeoData.getSubstationStart());
        assertEquals("CORAL", lineGeoData.getSubstationEnd());
        assertTrue(lineGeoData.getCoordinates().isEmpty());
    }
}
