/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeFileNameTest {

    @Test
    public void testValidName() {
        String fileName = "20140213_0830_SN4_D20";
        EntsoeFileName ucteFileName = EntsoeFileName.parse(fileName);
        assertTrue(ucteFileName.getDate().isEqual(DateTime.parse("2014-02-13T08:30:00.000+01:00")));
        assertEquals(0, ucteFileName.getForecastDistance());
        assertEquals("DE", ucteFileName.getCountry());
        assertSame(EntsoeGeographicalCode.D2, ucteFileName.getGeographicalCode());
    }

    @Test
    public void testForecast() {
        String fileName = "20140213_0830_FO4_DE0";
        EntsoeFileName ucteFileName = EntsoeFileName.parse(fileName);
        assertEquals(870, ucteFileName.getForecastDistance());
    }

    @Test
    public void testInvalidName() {
        String fileName = "???";
        EntsoeFileName ucteFileName = EntsoeFileName.parse(fileName);
        assertEquals(0, ucteFileName.getForecastDistance());
        assertNull(ucteFileName.getCountry());
    }

    @Test
    public void testIdcf() {
        String fileName = "20200314_0030_026_FR0.uct";
        EntsoeFileName ucteFileName = EntsoeFileName.parse(fileName);
        assertTrue(ucteFileName.getDate().isEqual(DateTime.parse("2020-03-14T00:30:00.000+01:00")));
    }

    @Test
    public void testGetCountry() {
        String fileName = "20200608_0730_2D1_UX0.uct";
        EntsoeFileName ucteFileName = EntsoeFileName.parse(fileName);
        assertNotNull(ucteFileName.getGeographicalCode());
        assertNull(ucteFileName.getCountry());
    }
}
