/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.Phase;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoad;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoads;
import com.powsybl.iidm.network.extensions.PhaseToPhaseLoadsAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class PhaseToPhaseLoadsXmlTest extends AbstractIidmSerDeTest {

    private static Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        return network;
    }

    @Test
    void test() throws IOException {
        Network network = createNetwork();
        PhaseToPhaseLoads extension = network.getLoad("LOAD").newExtension(PhaseToPhaseLoadsAdder.class).add();
        extension.newPhaseToPhaseLoad().withPhase1(Phase.A).withPhase2(Phase.B).withP(10).withQ(5).add();
        extension.newPhaseToPhaseLoad().withPhase1(Phase.C).withPhase2(Phase.B).withP(3).withQ(1).add();

        Network network2 = allFormatsRoundTripTest(network, "/phaseToPhaseLoadsRef_V1_0.xml");

        Load load = network2.getLoad("LOAD");
        PhaseToPhaseLoads extension2 = load.getExtension(PhaseToPhaseLoads.class);
        assertNotNull(extension2);
        assertEquals(2, extension2.getPhaseToPhaseLoads().size());

        PhaseToPhaseLoad ab = extension2.findPhaseToPhaseLoad(Phase.A, Phase.B).orElseThrow();
        assertEquals(Phase.A, ab.getPhase1());
        assertEquals(Phase.B, ab.getPhase2());
        assertEquals(10, ab.getP(), 0);
        assertEquals(5, ab.getQ(), 0);

        PhaseToPhaseLoad cb = extension2.findPhaseToPhaseLoad(Phase.B, Phase.C).orElseThrow();
        assertEquals(Phase.C, cb.getPhase1());
        assertEquals(Phase.B, cb.getPhase2());
        assertEquals(3, cb.getP(), 0);
        assertEquals(1, cb.getQ(), 0);
    }

    @Test
    void emptyExtensionIsNotSerializedTest() {
        Network network = createNetwork();
        network.getLoad("LOAD").newExtension(PhaseToPhaseLoadsAdder.class).add();

        PhaseToPhaseLoadsSerDe serDe = new PhaseToPhaseLoadsSerDe();
        assertFalse(serDe.isSerializable(network.getLoad("LOAD").getExtension(PhaseToPhaseLoads.class)));
    }
}
