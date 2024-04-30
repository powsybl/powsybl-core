/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.GeneratorStartupAdder;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.IidmSerDeConstants;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorStartupXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = createTestNetwork();

        // extends generator
        Generator generator = network.getGenerator("G");
        assertNotNull(generator);
        GeneratorStartup startup = generator.newExtension(GeneratorStartupAdder.class)
                .withPlannedActivePowerSetpoint(90)
                .withStartupCost(5)
                .withMarginalCost(10)
                .withPlannedOutageRate(0.8)
                .withForcedOutageRate(0.7)
                .add();

        Network network2 = allFormatsRoundTripTest(network, "generatorStartupRef.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        Generator generator2 = network2.getGenerator("G");
        assertNotNull(generator2);
        GeneratorStartup startup2 = generator2.getExtension(GeneratorStartup.class);
        assertNotNull(startup2);
        assertEquals(startup.getPlannedActivePowerSetpoint(), startup2.getPlannedActivePowerSetpoint(), 0);
        assertEquals(startup.getStartupCost(), startup2.getStartupCost(), 0);
        assertEquals(startup.getMarginalCost(), startup2.getMarginalCost(), 0);
        assertEquals(startup.getPlannedOutageRate(), startup2.getPlannedOutageRate(), 0);
        assertEquals(startup.getForcedOutageRate(), startup2.getForcedOutageRate(), 0);

        // backward compatibility
        allFormatsRoundTripTest(network, "generatorStartupRef-1.0.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION,
                new ExportOptions().addExtensionVersion(GeneratorStartup.NAME, "1.0"));
        allFormatsRoundTripTest(network, "generatorStartupRef-1.0-itesla.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION,
                new ExportOptions().addExtensionVersion(GeneratorStartup.NAME, "1.0-itesla"));
    }

    private static Network createTestNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("test", "test");
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
                .setId("G")
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
}
