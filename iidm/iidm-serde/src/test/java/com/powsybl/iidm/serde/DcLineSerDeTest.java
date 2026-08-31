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
import java.util.List;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class DcLineSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testNetworkDcLine() throws IOException {
        Network network = createBaseNetwork();

        // Test for the current version
        allFormatsRoundTripTest(network, "/dcLineRoundTripRef.xml", CURRENT_IIDM_VERSION);

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
    void testExportOnlySelectedGroups() throws IOException {
        Network network = createBaseNetwork();

        Network read = allFormatsRoundTripTest(network, "/dcLineOnlySelectedGroupsRef.xml", CURRENT_IIDM_VERSION,
                new ExportOptions().setOnlySelectedOperationalLimitsGroups(true));

        DcLine dcLine = read.getDcLine("dcLineWithSolvedV");
        assertEquals(1, dcLine.getOperationalLimitsGroups().size());
        assertEquals("summer", dcLine.getSelectedOperationalLimitsGroupId().orElseThrow());
        assertEquals(2106.0, dcLine.getCurrentLimits().orElseThrow().getPermanentLimit());
    }

    @Test
    void testMultipleSelectedGroups() throws IOException {
        Network network = createBaseNetwork();
        network.getDcLine("dcLineWithSolvedV").addSelectedOperationalLimitsGroups("winter");

        Network read = allFormatsRoundTripTest(network, "/dcLineMultipleSelectedGroupsRef.xml", CURRENT_IIDM_VERSION);

        DcLine dcLine = read.getDcLine("dcLineWithSolvedV");
        assertEquals(List.of("summer", "winter"), dcLine.getAllSelectedOperationalLimitsGroupIdsOrdered());
        assertEquals(2, dcLine.getAllSelectedCurrentLimits().size());
        assertEquals(2400.0, dcLine.getCurrentLimits().orElseThrow().getPermanentLimit());
    }

    @Test
    void testOnlyCurrentLimitsCanBeRead() {
        // a DcLine refuses the other kinds, so such a file can only be hand-written; reading must refuse it too
        PowsyblException e = assertThrows(PowsyblException.class,
                () -> NetworkSerDe.read(getVersionedNetworkAsStream("dcLineApparentPowerLimits.xml", CURRENT_IIDM_VERSION)));
        assertEquals("DC Line 'dcLineWithSolvedV': apparent power limits are not supported: a DC line carries no reactive power",
                e.getMessage());
    }

    @Test
    void testLimitsNotSupported() {
        Network network = createBaseNetwork();

        // versions 1.15 to 1.17 support the DC line but not its operational limits, so exporting a DC line
        // that declares limits must fail rather than silently drop them
        testForAllVersionsBetween(IidmVersion.V_1_15, IidmVersion.V_1_17, version -> {
            ExportOptions options = new ExportOptions().setVersion(version.toString("."));
            Path path = tmpDir.resolve("fail");
            PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.write(network, options, path));
            assertEquals("dcLine.operationalLimitsGroup is not defined as default and not supported for IIDM version "
                    + version.toString(".") + ". IIDM version should be >= 1.18", e.getMessage());
        });
    }

    @Test
    void testExportToOlderVersionSucceedsWithNoLimitToDrop() {
        // with no limits at all there is nothing to lose, so the version error must not fire
        Network noLimits = Network.create("dcLineTest", "code");
        noLimits.setCaseDate(ZonedDateTime.parse("2025-01-02T03:04:05.000+01:00"));
        noLimits.newDcNode().setId("dcNode1").setNominalV(500.).add();
        noLimits.newDcNode().setId("dcNode2").setNominalV(500.).add();
        noLimits.newDcLine().setId("dcLine").setDcNode1("dcNode1").setDcNode2("dcNode2").setR(1.0).add();
        ExportOptions options = new ExportOptions().setVersion(IidmVersion.V_1_17.toString("."));
        assertDoesNotThrow(() -> NetworkSerDe.write(noLimits, options, tmpDir.resolve("noLimits.xml")));

        // same when exporting only the selected groups and none is selected
        Network network = createBaseNetwork();
        network.getDcLine("dcLineWithSolvedV").cancelSelectedOperationalLimitsGroup();
        ExportOptions selectedOnly = new ExportOptions()
                .setVersion(IidmVersion.V_1_17.toString("."))
                .setOnlySelectedOperationalLimitsGroups(true);
        assertDoesNotThrow(() -> NetworkSerDe.write(network, selectedOnly, tmpDir.resolve("noSelected.xml")));
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
                .add();
        dcLine2.getDcTerminal1().setP(100.).setI(200.);
        dcLine2.getDcTerminal2().setP(-98.).setI(-195.);
        dcLine2.newOperationalLimitsGroup("summer")
                .newCurrentLimits()
                .setPermanentLimit(2106.0)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(2350.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(2600.0)
                .endTemporaryLimit()
                .add();
        dcLine2.newOperationalLimitsGroup("winter")
                .newCurrentLimits()
                .setPermanentLimit(2400.0)
                .add();
        dcLine2.setSelectedOperationalLimitsGroup("summer");
        return network;
    }

}
