/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.cgmes.conversion.extensions.CgmesSshControlAreasImpl.ControlArea;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class CgmesSshControlAreasTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        ControlArea controlArea1 = new ControlArea("_a5bed00f-624f-4c92-8bac-5a4d66fc3213", 274.5, 0.5);
        ControlArea controlArea2 = new ControlArea("_5694535c-442f-4b47-97bc-673296d068bb", 43.5, 0.25);
        network.newExtension(CgmesSshControlAreasAdder.class)
            .addControlArea(controlArea1)
            .addControlArea(controlArea2)
            .add();
        CgmesSshControlAreas extension = network.getExtension(CgmesSshControlAreas.class);
        assertNotNull(extension);
        assertEquals(2, extension.getControlAreas().size());
        assertEquals("_a5bed00f-624f-4c92-8bac-5a4d66fc3213", extension.getControlAreas().get(0).getId());
        assertEquals(274.5, extension.getControlAreas().get(0).getNetInterchange(), 0.0001);
        assertEquals(0.5, extension.getControlAreas().get(0).getPTolerance(), 0.0001);

        assertEquals("_5694535c-442f-4b47-97bc-673296d068bb", extension.getControlAreas().get(1).getId());
        assertEquals(43.5, extension.getControlAreas().get(1).getNetInterchange(), 0.0001);
        assertEquals(0.25, extension.getControlAreas().get(1).getPTolerance(), 0.0001);
    }

    @Test(expected = PowsyblException.class)
    public void invalid() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(CgmesSshControlAreasAdder.class)
            .add();
    }
}
