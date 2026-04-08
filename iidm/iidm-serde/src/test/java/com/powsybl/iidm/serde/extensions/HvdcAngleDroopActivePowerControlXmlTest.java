/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Paul Bui-Quang {@literal <paul.buiquang at rte-france.com>}
 */
class HvdcAngleDroopActivePowerControlXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = HvdcTestNetwork.createLcc();

        HvdcLine hvdcLine = network.getHvdcLine("L");
        HvdcAngleDroopActivePowerControl extension = hvdcLine.newExtension(HvdcAngleDroopActivePowerControlAdder.class).withEnabled(true).withDroop(0.1f).withP0(200).add();

        Network network2 = allFormatsRoundTripTest(network, "hvdcAngleDroopActivePowerControlRef.xml", CURRENT_IIDM_VERSION);

        HvdcLine hvdcLine2 = network2.getHvdcLine("L");
        assertEquals(hvdcLine2.getExtension(HvdcAngleDroopActivePowerControl.class), extension);
    }
}
