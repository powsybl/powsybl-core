/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DcLine;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class DcLineSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testNetworkDcLine() throws IOException {
        Network network = createBaseNetwork();

        // Test for the current version
        Network readNetwork = allFormatsRoundTripTest(network, "/dcLineRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // the round trip re-exports the original network, not the one read from XML,
        // so the XML reader is only checked here
        assertTrue(Double.isNaN(readNetwork.getDcLine("dcLineWithoutSolvedV").getPermanentCurrentLimit()));
        assertEquals(2106., readNetwork.getDcLine("dcLineWithSolvedV").getPermanentCurrentLimit());

        // backward compatibility - checks from version 1.15
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("/dcLineRoundTripRef.xml", IidmVersion.V_1_15);
    }

    @Test
    void testNotSupported() throws IOException {
        Network network = createBaseNetwork();

        // Note: we do not test here failing for all versions < 1.15: DcLine cannot exist without DcNode,
        // hence the DcNode SerDe test is sufficient.

        // check it doesn't fail for version 1.14 if IidmVersionIncompatibilityBehavior is to log error
        var options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        testWriteVersionedXml(network, options, "dcLineNotSupported.xml", IidmVersion.V_1_14);
    }

    @Test
    void testPermanentCurrentLimitNotSupported() {
        Network network = createBaseNetwork();

        testForAllVersionsBetween(IidmVersion.V_1_15, IidmVersion.V_1_18, version -> {
            ExportOptions options = new ExportOptions().setVersion(version.toString("."));
            Path path = tmpDir.resolve("fail");
            PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.write(network, options, path));
            assertEquals("dcLine.permanentCurrentLimit is not defined as default and not supported for IIDM version "
                    + version.toString(".") + ". IIDM version should be >= 1.19", e.getMessage());
        });

        // a DC line declaring no limit is unaffected
        network.getDcLine("dcLineWithSolvedV").setPermanentCurrentLimit(Double.NaN);
        ExportOptions options = new ExportOptions().setVersion(IidmVersion.V_1_18.toString("."));
        assertDoesNotThrow(() -> NetworkSerDe.write(network, options, tmpDir.resolve("noLimit.xml")));
    }

    private static Network createBaseNetwork() {
        Network network = Network.create("dcLineTest", "code");
        network.setCaseDate(ZonedDateTime.parse("2025-01-02T03:04:05.000+01:00"));
        DcNode dcNode1 = network.newDcNode()
                .setId("dcNode1")
                .setNominalV(500.)
                .add();
        DcNode dcNode2 = network.newDcNode()
                .setId("dcNode2")
                .setNominalV(500.)
                .add();
        DcLine dcLine1 = network.newDcLine()
                .setId("dcLineWithoutSolvedV")
                .setName("A DC Line without solved values")
                .setFictitious(true)
                .setDcNode1(dcNode1.getId())
                .setConnected1(false)
                .setDcNode2(dcNode2.getId())
                .setConnected2(false)
                .setR(3.0)
                .add();
        dcLine1.setProperty("prop name", "prop value");
        dcLine1.addAlias("someAlias");
        DcLine dcLine2 = network.newDcLine()
                .setId("dcLineWithSolvedV")
                .setName("A DC Line with solved values")
                .setDcNode1(dcNode1.getId())
                .setConnected1(true)
                .setDcNode2(dcNode2.getId())
                .setConnected2(true)
                .setR(4.0)
                .setPermanentCurrentLimit(2106.)
                .add();
        dcLine2.getDcTerminal1().setP(100.).setI(200.);
        dcLine2.getDcTerminal2().setP(-98.).setI(-195.);
        return network;
    }

}
