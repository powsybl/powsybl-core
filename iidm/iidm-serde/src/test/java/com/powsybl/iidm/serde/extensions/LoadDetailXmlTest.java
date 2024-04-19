/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.LoadDetailAdder;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.IidmSerDeConstants;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static com.powsybl.commons.test.ComparisonUtils.compareXml;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LoadDetailXmlTest extends AbstractIidmSerDeTest {

    private static Network createTestNetwork() {
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
        vl.newLoad()
                .setId("L")
                .setBus("B")
                .setConnectableBus("B")
                .setP0(100)
                .setQ0(50)
                .add();

        // extends load
        Load load = network.getLoad("L");
        assertNotNull(load);
        load.newExtension(LoadDetailAdder.class)
                .withFixedActivePower(40f)
                .withFixedReactivePower(20f)
                .withVariableActivePower(60f)
                .withVariableReactivePower(30f)
                .add();
        return network;
    }

    @Test
    void test() throws IOException {
        Network network = createTestNetwork();

        LoadDetail detail = network.getLoad("L").getExtension(LoadDetail.class);
        assertNotNull(detail);

        Network network2 = allFormatsRoundTripTest(network, "loadDetailRef.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        Load load2 = network2.getLoad("L");
        assertNotNull(load2);
        LoadDetail detail2 = load2.getExtension(LoadDetail.class);
        assertNotNull(detail2);
        assertEquals(detail.getFixedActivePower(), detail2.getFixedActivePower(), 0f);
        assertEquals(detail.getFixedReactivePower(), detail2.getFixedReactivePower(), 0f);
        assertEquals(detail.getVariableActivePower(), detail2.getVariableActivePower(), 0f);
        assertEquals(detail.getVariableReactivePower(), detail2.getVariableReactivePower(), 0f);
    }

    @Test
    void testOld() throws IOException {
        Network network = NetworkSerDe.read(getVersionedNetworkAsStream("loadDetailRef.xml", IidmVersion.V_1_2));

        LoadDetail detail = network.getLoad("L").getExtension(LoadDetail.class);
        assertNotNull(detail);

        Path tmp = tmpDir.resolve("data");
        NetworkSerDe.write(network, tmp);
        NetworkSerDe.validate(tmp);

        try (InputStream is = Files.newInputStream(tmp)) {
            compareXml(getVersionedNetworkAsStream("loadDetailRef.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION), is);
        }
    }
}
