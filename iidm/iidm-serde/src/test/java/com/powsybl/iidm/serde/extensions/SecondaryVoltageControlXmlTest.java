/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SecondaryVoltageControlXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        network.setCaseDate(ZonedDateTime.parse("2023-01-07T20:43:11.819+01:00"));

        SecondaryVoltageControl control = network.newExtension(SecondaryVoltageControlAdder.class)
                .newControlZone()
                    .withName("z1")
                    .newPilotPoint()
                        .withBusbarSectionsOrBusesIds(List.of("NLOAD"))
                        .withTargetV(15d)
                    .add()
                    .newControlUnit()
                        .withId("GEN")
                        .withParticipate(false)
                    .add()
                    .newControlUnit()
                        .withId("GEN2")
                        .add()
                    .add()
                .add();

        Network network2 = allFormatsRoundTripTest(network, "/secondaryVoltageControlRoundTripRef.xml", CURRENT_IIDM_VERSION);

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
