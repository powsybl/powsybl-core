/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class DcNodeSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testNetworkDcNode() throws IOException {
        Network network = createBaseNetwork();

        // Test for the current version
        Network readNetwork = allFormatsRoundTripTest(network, "/dcNodeRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // the round trip re-exports the original network, not the one read from XML,
        // so the XML reader is only checked here
        DcNode withoutLimits = readNetwork.getDcNode("dcNodeWithoutSolvedV");
        assertTrue(Double.isNaN(withoutLimits.getLowVoltageLimit()));
        assertTrue(Double.isNaN(withoutLimits.getHighVoltageLimit()));
        DcNode withBothLimits = readNetwork.getDcNode("dcNodeWithSolvedV");
        assertEquals(480., withBothLimits.getLowVoltageLimit());
        assertEquals(520., withBothLimits.getHighVoltageLimit());
        DcNode negativePole = readNetwork.getDcNode("dcNodeOnNegativePole");
        assertEquals(-502., negativePole.getV());
        assertTrue(Double.isNaN(negativePole.getLowVoltageLimit()));
        assertEquals(520., negativePole.getHighVoltageLimit());

        // backward compatibility - checks from version 1.15
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("/dcNodeRoundTripRef.xml", IidmVersion.V_1_15);
    }

    @Test
    void testNotSupported() throws IOException {
        Network network = createBaseNetwork();

        // check it fails for all versions < 1.15
        testForAllPreviousVersions(IidmVersion.V_1_15, version -> {
            ExportOptions options = new ExportOptions().setVersion(version.toString("."));
            Path path = tmpDir.resolve("fail");
            PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.write(network, options, path));
            assertEquals("network.dcNode is not supported for IIDM version " + version.toString(".") + ". IIDM version should be >= 1.15", e.getMessage());
        });

        // check it doesn't fail for version 1.14 if IidmVersionIncompatibilityBehavior is to log error
        var options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        testWriteVersionedXml(network, options, "dcNodeNotSupported.xml", IidmVersion.V_1_14);
    }

    @Test
    void testVoltageLimitsNotSupported() {
        Network network = createBaseNetwork();

        // versions 1.15 to 1.18 support the DC node but not its voltage limits, so exporting a DC node that
        // declares limits must fail rather than silently drop them
        testForAllVersionsBetween(IidmVersion.V_1_15, IidmVersion.V_1_18, version -> {
            ExportOptions options = new ExportOptions().setVersion(version.toString("."));
            Path path = tmpDir.resolve("fail");
            PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.write(network, options, path));
            assertEquals("dcNode.lowVoltageLimit is not defined as default and not supported for IIDM version "
                    + version.toString(".") + ". IIDM version should be >= 1.19", e.getMessage());
        });

        // a DC node declaring no limit is unaffected
        Network noLimits = Network.create("dcNodeTest", "code");
        noLimits.setCaseDate(ZonedDateTime.parse("2025-01-02T03:04:05.000+01:00"));
        noLimits.newDcNode().setId("dcNode").setNominalV(500.).add();
        ExportOptions options = new ExportOptions().setVersion(IidmVersion.V_1_18.toString("."));
        assertDoesNotThrow(() -> NetworkSerDe.write(noLimits, options, tmpDir.resolve("noLimits.xml")));
    }

    private static Network createBaseNetwork() {
        Network network = Network.create("dcNodeTest", "code");
        network.setCaseDate(ZonedDateTime.parse("2025-01-02T03:04:05.000+01:00"));
        var n = network.newDcNode()
                .setId("dcNodeWithoutSolvedV")
                .setName("A DC Node without solved V")
                .setNominalV(500.)
                .setFictitious(true)
                .add();
        n.setProperty("prop name", "prop value");
        n.addAlias("someAlias");
        network.newDcNode()
                .setId("dcNodeWithSolvedV")
                .setName("A DC Node with solved V")
                .setNominalV(500.)
                .setLowVoltageLimit(480.)
                .setHighVoltageLimit(520.)
                .add()
                .setV(498.);
        // the limits bound the voltage magnitude, so they are positive on a negative pole too
        network.newDcNode()
                .setId("dcNodeOnNegativePole")
                .setName("A DC Node on a negative pole")
                .setNominalV(500.)
                .setHighVoltageLimit(520.)
                .add()
                .setV(-502.);
        return network;
    }

}
