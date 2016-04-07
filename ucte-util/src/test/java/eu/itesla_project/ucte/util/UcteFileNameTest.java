/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.ucte.util;

import eu.itesla_project.iidm.network.Horizon;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteFileNameTest {

    @Test
    public void testValidName() {
        String fileName = "20140213_0830_SN4_D20";
        UcteFileName ucteFileName = UcteFileName.parse(fileName);
        assertTrue(ucteFileName.getDate().isEqual(DateTime.parse("2014-02-13T08:30:00.000+01:00")));
        assertTrue(ucteFileName.getHorizon() == Horizon.SN);
        assertTrue(ucteFileName.getForecastDistance() == 0);
        assertTrue(ucteFileName.getCountry().equals("DE"));
        assertTrue(ucteFileName.getGeographicalCode() == UcteGeographicalCode.D2);
    }

    @Test
    public void testInvalidName() {
        String fileName = "???";
        UcteFileName ucteFileName = UcteFileName.parse(fileName);
        assertTrue(ucteFileName.getHorizon() == Horizon.OTHER);
        assertTrue(ucteFileName.getForecastDistance() == 0);
        assertTrue(ucteFileName.getCountry() == null);
    }
}