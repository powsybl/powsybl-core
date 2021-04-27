/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeAreaXmlSerializerTest extends AbstractConverterTest {

    private static Network createTestNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        return network;
    }

    @Test
    public void test() throws IOException {
        Network network = createTestNetwork();

        // extends substation
        Substation s = network.getSubstation("S");
        s.newExtension(EntsoeAreaAdder.class).withCode(EntsoeGeographicalCode.BE).add();
        EntsoeArea country = s.getExtension(EntsoeArea.class);

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/entsoeAreaRef.xml");

        Substation s2 = network2.getSubstation("S");
        EntsoeArea country2 = s2.getExtension(EntsoeArea.class);
        assertNotNull(country2);
        assertEquals(country.getCode(), country2.getCode());
    }
}
