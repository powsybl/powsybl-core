/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.PilotPoint;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.Zone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionedNetworkPath;
import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecondaryVoltageControlXmlTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        network.setCaseDate(DateTime.parse("2023-01-07T20:43:11.819+01:00"));

        SecondaryVoltageControl control = network.newExtension(SecondaryVoltageControlAdder.class)
                .addZone(new Zone("z1", new PilotPoint("NLOAD", 15d), List.of("GEN", "GEN2")))
                .add();

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("/secondaryVoltageControlRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));

        SecondaryVoltageControl control2 = network2.getExtension(SecondaryVoltageControl.class);
        assertNotNull(control2);

        assertEquals(control.getZones().size(), control2.getZones().size());
        assertEquals(control.getZones().get(0).getPilotPoint().getBusbarSectionOrBusId(),
                     control2.getZones().get(0).getPilotPoint().getBusbarSectionOrBusId());
        assertEquals(control.getZones().get(0).getPilotPoint().getTargetV(),
                     control2.getZones().get(0).getPilotPoint().getTargetV(), 0d);
        assertEquals(control.getZones().get(0).getGeneratorsIds(), control2.getZones().get(0).getGeneratorsIds());
    }
}
