/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
public class VoltagePerReactivePowerControlXmlSerializerTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = SvcTestCaseFactory.create();
        network.setCaseDate(DateTime.parse("2019-05-27T12:17:02.504+02:00"));
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);

        svc.newExtension(VoltagePerReactivePowerControlAdder.class).withSlope(0.5).add();

        // FIXME
        /*Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/voltagePerReactivePowerControl.xml");

        StaticVarCompensator svc2 = network2.getStaticVarCompensator("SVC2");
        assertNotNull(svc2);
        VoltagePerReactivePowerControl control2 = svc2.getExtension(VoltagePerReactivePowerControl.class);
        assertNotNull(control2);

        assertEquals(0.2, control2.getSlope(), 0.0);
        assertEquals("voltagePerReactivePowerControl", control2.getName());*/
    }

}
