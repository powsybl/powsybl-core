/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.PilotPoint;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl.Zone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractSecondaryVoltageControlTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        SecondaryVoltageControl control = network.newExtension(SecondaryVoltageControlAdder.class)
                .addZone(new Zone("z1", new PilotPoint("NLOAD", 15d), List.of("GEN", "GEN2")))
                .add();
        assertEquals(1, control.getZones().size());
        Zone z1 = control.getZones().get(0);
        assertEquals("z1", z1.getName());
        assertNotNull(z1.getPilotPoint());
        assertEquals("NLOAD", z1.getPilotPoint().getBusbarSectionOrBusId());
        assertEquals(15d, z1.getPilotPoint().getTargetV(), 0d);
        assertEquals(List.of("GEN", "GEN2"), z1.getGeneratorsIds());
        z1.getPilotPoint().setTargetV(16);
        assertEquals(16d, z1.getPilotPoint().getTargetV(), 0d);
    }
}
