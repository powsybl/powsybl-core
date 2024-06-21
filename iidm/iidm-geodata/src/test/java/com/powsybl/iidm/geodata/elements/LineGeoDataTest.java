/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.elements;

import com.powsybl.iidm.network.extensions.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Chamseddine Benhamed {@literal <chamseddine.benhamed at rte-france.com>}
 */
class LineGeoDataTest {

    @Test
    void test() {
        LineGeoData lineGeoData = new LineGeoData("l", "FR", "FR", "ALAMO", "CORAL", Collections.<Coordinate>emptyList());

        assertEquals("l", lineGeoData.id());
        assertEquals("FR", lineGeoData.country1());
        assertEquals("FR", lineGeoData.country2());
        assertEquals("ALAMO", lineGeoData.substationStart());
        assertEquals("CORAL", lineGeoData.substationEnd());
        assertTrue(lineGeoData.coordinates().isEmpty());
    }
}
