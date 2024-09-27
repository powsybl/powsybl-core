/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.OverloadManagementSystem;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.extensions.TrippingTempoContainerAdder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

public class TrippingTempoSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        allFormatsRoundTripTest(createTestNetwork(), "trippingTempoRoundtripRef.xml", CURRENT_IIDM_VERSION);
    }

    Network createTestNetwork() {
        Network network = NetworkSerDe.read(Paths.get("src/test/resources/tripping-tempo-base-network.xiidm"));
        OverloadManagementSystem automaton = network.getSubstation("Substation").newOverloadManagementSystem()
                .setId("OMS_1")
                .setEnabled(true)
                .newSwitchTripping()
                .setName("tutu")
                .setKey("OMS_1_tripping_1")
                .setCurrentLimit(620)
                .setSwitchToOperateId("Switch1_ID")
                .setOpenAction(false)
                .add()
                .setMonitoredElementId("Transformer_1")
                .setMonitoredElementSide(ThreeSides.TWO)
                .add();
        automaton.newExtension(TrippingTempoContainerAdder.class).withTempo("OMS_1_tripping_1", 2).add();
        return network;
    }
}
