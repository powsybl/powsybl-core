/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ReferenceTerminalsAdder;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.IidmSerDeConstants;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ReferenceTerminalsXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        // network for serialization test purposes only, no load-flow would converge on this.
        // also having multiple reference terminals on the same synchronous component is not really representative.
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        vl.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        var gen = vl.newGenerator()
                .setId("G")
                .setBus("B1")
                .setConnectableBus("B1")
                .setTargetP(100)
                .setTargetQ(100)
                .setTargetV(400)
                .setMinP(0)
                .setMaxP(200)
                .setVoltageRegulatorOn(true)
                .add();
        var line = network.newLine()
                .setId("L12")
                .setVoltageLevel1("VL").setBus1("B1")
                .setVoltageLevel2("VL").setBus2("B2")
                .setR(0).setX(1).setB1(0).setB2(0).setG1(0).setG2(0)
                .add();

        Set<Terminal> terminals = new LinkedHashSet<>();
        terminals.add(gen.getTerminal());
        terminals.add(line.getTerminal1());

        network.newExtension(ReferenceTerminalsAdder.class)
                .withTerminals(terminals)
                .add();

        allFormatsRoundTripTest(network, "referenceTerminalsRef.xiidm", IidmSerDeConstants.CURRENT_IIDM_VERSION);
    }
}
