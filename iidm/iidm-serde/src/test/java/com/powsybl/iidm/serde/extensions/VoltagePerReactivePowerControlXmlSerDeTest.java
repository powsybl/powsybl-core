/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
class VoltagePerReactivePowerControlXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = SvcTestCaseFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2019-05-27T12:17:02.504+02:00"));
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);

        svc.newExtension(VoltagePerReactivePowerControlAdder.class).withSlope(0.5).add();

        Network network2 = allFormatsRoundTripTest(network, "/voltagePerReactivePowerControl.xml", CURRENT_IIDM_VERSION);

        StaticVarCompensator svc2 = network2.getStaticVarCompensator("SVC2");
        assertNotNull(svc2);
        VoltagePerReactivePowerControl control2 = svc2.getExtension(VoltagePerReactivePowerControl.class);
        assertNotNull(control2);

        assertEquals(0.5, control2.getSlope(), 0.0);
        assertEquals("voltagePerReactivePowerControl", control2.getName());
    }

}
