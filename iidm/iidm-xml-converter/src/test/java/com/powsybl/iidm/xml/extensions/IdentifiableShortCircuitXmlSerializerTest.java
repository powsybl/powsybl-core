/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Coline Piloquet <coline.piloquet@rte-france.com>
 */
public class IdentifiableShortCircuitXmlSerializerTest extends AbstractConverterTest {
    @Test
    public void testXmlSerializer() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2016-12-07T11:18:52.881+01:00"));
        VoltageLevel vlhv1 = network.getVoltageLevel("VLHV1");
        assertNotNull(vlhv1);
        vlhv1.newExtension(IdentifiableShortCircuitAdder.class)
                .withIpMin(500)
                .withIpMax(1500)
                .add();
        IdentifiableShortCircuit voltageLevelShortCircuits = vlhv1.getExtension(IdentifiableShortCircuit.class);

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read, "/shortcircuits/voltageLevelShortCircuitRef.xml");

        VoltageLevel vlhv2 = network2.getVoltageLevel("VLHV1");
        assertNotNull(vlhv2);
        IdentifiableShortCircuit voltageLevelShortCircuits2 = vlhv2.getExtension(IdentifiableShortCircuit.class);
        assertNotNull(voltageLevelShortCircuits2);

        assertEquals(voltageLevelShortCircuits.getIpMax(), voltageLevelShortCircuits2.getIpMax(), 0);
        assertEquals(voltageLevelShortCircuits.getIpMin(), voltageLevelShortCircuits2.getIpMin(), 0);
    }
}
