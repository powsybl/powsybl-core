/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.extensions.BaseVoltageMappingAdder;
import com.powsybl.cgmes.extensions.Source;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class BaseVoltageConversionTest extends AbstractSerDeTest {

    @Test
    void baseVoltageExportTest() throws IOException {
        Network network = createBaseNetwork();
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);

        assertEquals(3, getElementCount(eqFile, "BaseVoltage"));
        assertBaseVoltage(eqFile, "400_BV", "400");
        assertBaseVoltage(eqFile, "200_BV", "200");
        assertBaseVoltage(eqFile, "100_BV", "100");
    }

    @Test
    void baseVoltageWithExtensionExportTest() throws IOException {
        Network network = createWithExtension();
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);

        assertEquals(3, getElementCount(eqFile, "BaseVoltage"));
        assertBaseVoltage(eqFile, "400kV_BaseVoltageId", "400");
        assertBaseVoltage(eqFile, "200_BV", "200");
        assertBaseVoltage(eqFile, "100_BV", "100");
    }

    private Network createBaseNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("regionsTest", "test");
        network.setCaseDate(ZonedDateTime.parse("2021-12-07T19:43:00.0000+02:00"));

        Substation s1 = network.newSubstation().setId("S1").add();
        s1.newVoltageLevel().setId("S1-VL400").setNominalV(400).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        s1.newVoltageLevel().setId("S1-VL200").setNominalV(200).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        Substation s2 = network.newSubstation().setId("S2").add();
        s2.newVoltageLevel().setId("S2-VL400").setNominalV(400).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        s2.newVoltageLevel().setId("S2-VL100").setNominalV(100).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        return network;
    }

    private Network createWithExtension() {
        Network network = createBaseNetwork();
        BaseVoltageMappingAdder bvAdder = network.newExtension(BaseVoltageMappingAdder.class);
        bvAdder.addBaseVoltage("400kV_BaseVoltageId", 400, Source.IGM);
        bvAdder.addBaseVoltage("300kV_BaseVoltageId", 300, Source.IGM);  // not used, shouldn't be exported.
        bvAdder.add();

        return network;
    }

    private void assertBaseVoltage(String eqFile, String baseVoltageId, String nominalVoltage) {
        String baseVoltage = getElement(eqFile, "BaseVoltage", baseVoltageId);
        assertNotNull(baseVoltage);
        assertEquals(nominalVoltage, getAttribute(baseVoltage, "BaseVoltage.nominalVoltage"));
    }
}
