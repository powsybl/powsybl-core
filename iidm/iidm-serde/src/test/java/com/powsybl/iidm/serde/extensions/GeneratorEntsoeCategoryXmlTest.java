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
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.IidmSerDeConstants;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorEntsoeCategoryXmlTest extends AbstractIidmSerDeTest {

    static Network createTestNetwork() {
        Network network = NetworkFactory.create("test", "test");
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

    @Test
    void test() throws IOException {
        Network network = createTestNetwork();

        // extends generator
        Generator generator = network.getGenerator("G");
        assertNotNull(generator);
        GeneratorEntsoeCategory entsoeCategory = generator.newExtension(GeneratorEntsoeCategoryAdder.class).withCode(4).add();

        Network network2 = allFormatsRoundTripTest(network, "/generatorEntsoeCategoryRef.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        Generator generator2 = network2.getGenerator("G");
        assertNotNull(generator2);
        GeneratorEntsoeCategory entsoeCategory2 = generator2.getExtension(GeneratorEntsoeCategory.class);
        assertNotNull(entsoeCategory2);
        assertEquals(entsoeCategory.getCode(), entsoeCategory2.getCode());
    }
}
