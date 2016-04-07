/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.ucte.util;

import com.google.common.collect.Sets;
import eu.itesla_project.iidm.network.Country;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteGeographicalCodeTest {

    @Test
    public void testForCountry() throws Exception {
        assertTrue(UcteGeographicalCode.forCountry(Country.FR).equals(Sets.newHashSet(UcteGeographicalCode.FR)));
        assertTrue(UcteGeographicalCode.forCountry(Country.DE).equals(Sets.newHashSet(
                UcteGeographicalCode.D1, UcteGeographicalCode.D2, UcteGeographicalCode.D4, UcteGeographicalCode.D7, UcteGeographicalCode.D8)));
    }
}