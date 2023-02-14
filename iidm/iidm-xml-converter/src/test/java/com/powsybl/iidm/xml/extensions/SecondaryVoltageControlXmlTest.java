/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.ControlUnit;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.ControlZone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.PilotPoint;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionedNetworkPath;
import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SecondaryVoltageControlXmlTest extends AbstractConverterTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        network.setCaseDate(DateTime.parse("2023-01-07T20:43:11.819+01:00"));

        SecondaryVoltageControl control = network.newExtension(SecondaryVoltageControlAdder.class)
                .addControlZone(new ControlZone("z1",
                                                new PilotPoint(List.of("NLOAD"), 15d),
                                                List.of(new ControlUnit("GEN", false), new ControlUnit("GEN2"))))
                .add();

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("/secondaryVoltageControlRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));

        SecondaryVoltageControl control2 = network2.getExtension(SecondaryVoltageControl.class);
        assertNotNull(control2);

        assertEquals(control.getControlZones().size(), control2.getControlZones().size());
        assertEquals(control.getControlZones().get(0).getPilotPoint().getBusbarSectionsOrBusesIds(),
                     control2.getControlZones().get(0).getPilotPoint().getBusbarSectionsOrBusesIds());
        assertEquals(control.getControlZones().get(0).getPilotPoint().getTargetV(),
                     control2.getControlZones().get(0).getPilotPoint().getTargetV(), 0d);
        assertEquals(control.getControlZones().get(0).getControlUnits().size(),
                     control2.getControlZones().get(0).getControlUnits().size());
        assertEquals(control.getControlZones().get(0).getControlUnits().get(0).getId(),
                     control2.getControlZones().get(0).getControlUnits().get(0).getId());
        assertEquals(control.getControlZones().get(0).getControlUnits().get(0).isParticipate(),
                     control2.getControlZones().get(0).getControlUnits().get(0).isParticipate());
        assertEquals(control.getControlZones().get(0).getControlUnits().get(1).getId(),
                     control2.getControlZones().get(0).getControlUnits().get(1).getId());
        assertEquals(control.getControlZones().get(0).getControlUnits().get(1).isParticipate(),
                     control2.getControlZones().get(0).getControlUnits().get(1).isParticipate());
    }
}
