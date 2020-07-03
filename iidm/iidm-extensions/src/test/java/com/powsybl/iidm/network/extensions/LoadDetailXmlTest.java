/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.powsybl.iidm.network.extensions.LoadDetailTest.createTestNetwork;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadDetailXmlTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = createTestNetwork();

        LoadDetail detail = network.getLoad("L").getExtension(LoadDetail.class);
        assertNotNull(detail);

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/loadDetailRef.xml");

        Load load2 = network2.getLoad("L");
        assertNotNull(load2);
        LoadDetail detail2 = load2.getExtension(LoadDetail.class);
        assertNotNull(detail2);
        assertEquals(detail.getFixedActivePower(), detail2.getFixedActivePower(), 0f);
        assertEquals(detail.getFixedReactivePower(), detail2.getFixedReactivePower(), 0f);
        assertEquals(detail.getVariableActivePower(), detail2.getVariableActivePower(), 0f);
        assertEquals(detail.getVariableReactivePower(), detail2.getVariableReactivePower(), 0f);
    }

    @Test
    public void testOld() throws IOException {
        Network network = NetworkXml.read(getClass().getResourceAsStream("/loadDetailOldRef.xml"));

        LoadDetail detail = network.getLoad("L").getExtension(LoadDetail.class);
        assertNotNull(detail);

        Path tmp = tmpDir.resolve("data");
        NetworkXml.writeAndValidate(network, tmp);

        try (InputStream is = Files.newInputStream(tmp)) {
            compareXml(getClass().getResourceAsStream("/loadDetailRef.xml"), is);
        }
    }
}
