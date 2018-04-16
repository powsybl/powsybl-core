package com.powsybl.entsoe.util;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@rte-france.com>
 */
public class EntsoeAreaXmlSerializerTest extends AbstractConverterTest {

    private static Network createTestNetwork() {
        Network network = NetworkFactory.create("test", "test");
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
        EntsoeArea country = new EntsoeArea(s, EntsoeGeographicalCode.BE);
        s.addExtension(EntsoeArea.class, country);

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
