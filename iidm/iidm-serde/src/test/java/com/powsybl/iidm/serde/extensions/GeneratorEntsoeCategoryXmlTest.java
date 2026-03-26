/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategoryAdder;
import com.powsybl.iidm.serde.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorEntsoeCategoryXmlTest extends AbstractIidmSerDeTest {

    static Network createTestNetwork() {
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
                .setId("B")
                .add();
        vl.newGenerator()
            .setId("G0")
            .setBus("B")
            .setConnectableBus("B")
            .setTargetP(100)
            .setTargetV(380)
            .setVoltageRegulatorOn(true)
            .setMaxP(100)
            .setMinP(0)
            .add();
        vl.newGenerator()
                .setId("G4")
                .setBus("B")
                .setConnectableBus("B")
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();
        return network;
    }

    @Test
    void test() throws IOException {
        Network network = createTestNetwork();

        // extend generators
        network.getGenerator("G0").newExtension(GeneratorEntsoeCategoryAdder.class).withCode(0).add();
        network.getGenerator("G4").newExtension(GeneratorEntsoeCategoryAdder.class).withCode(4).add();

        // Check with latest version
        Network network2 = allFormatsRoundTripTest(network, "/generatorEntsoeCategoryRef.xml", IidmVersion.V_1_16);
        assertEqualsEntsoeCategory(network2.getGenerator("G0"), 0);
        assertEqualsEntsoeCategory(network2.getGenerator("G4"), 4);

        // Check backward compatibility
        // Code equal to 0 are not supported in version 1.0 of the extension
        Network network3 = allFormatsRoundTripTest(network, "/generatorEntsoeCategoryRef.xml", IidmVersion.V_1_15,
            new ExportOptions().addExtensionVersion(GeneratorEntsoeCategory.NAME, "1.0"));
        assertNull(network3.getGenerator("G0").getExtension(GeneratorEntsoeCategory.class));
        assertEqualsEntsoeCategory(network3.getGenerator("G4"), 4);
    }

    private void assertEqualsEntsoeCategory(Generator roundTripGenerator, int originalCode) {
        assertNotNull(roundTripGenerator);
        GeneratorEntsoeCategory roundTripExtension = roundTripGenerator.getExtension(GeneratorEntsoeCategory.class);
        assertNotNull(roundTripExtension);
        assertEquals(originalCode, roundTripExtension.getCode());
    }
}
