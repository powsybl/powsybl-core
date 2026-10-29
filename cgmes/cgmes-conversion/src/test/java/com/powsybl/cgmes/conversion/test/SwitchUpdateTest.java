/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_CGMES_ORIGINAL_CLASS;
import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_NORMAL_OPEN;
import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SwitchUpdateTest extends AbstractSerDeTest {

    private static final String DIR = "/update/switch/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml");
        assertEquals(1, network.getSwitchCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherAndSsh1LaterTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml", "switch_SSH.xml");
        assertEquals(3, network.getSwitchCount());

        assertEqSsh(network);

        readCgmesResources(network, DIR, "switch_SSH_1.xml");
        assertEqSshSsh1(network);
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml");
        assertEquals(1, network.getSwitchCount()); // No fictitious switch was created

        assertEq(network);

        readCgmesResources(network, DIR, "switch_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "switch_SSH_1.xml");
        assertSecondSsh(network);
    }

    @Test
    void usePreviousValuesTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml", "switch_SSH.xml");
        assertEquals(3, network.getSwitchCount());
        assertEqSsh(network);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.use-previous-values-during-update", "true");
        readCgmesResources(network, properties, DIR, "../empty_SSH.xml", "../empty_SV.xml");
        assertEqSsh(network);
    }

    @Test
    void removeAllPropertiesAndAliasesTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml", "switch_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, false);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.remove-properties-and-aliases-after-import", "true");
        network = readCgmesResources(properties, DIR, "switch_EQ.xml", "switch_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, true);
    }

    // Test to verify the compatibility of networks imported from XIIDM files generated with version V7.4.0,
    // in which zero-impedance branches were imported as switches.
    // This test can be removed in the future
    @Test
    void exportSwitchAsBranchTest() throws IOException {
        Network network = createFictitiousSwitchNetwork();
        Switch sw = network.getSwitch("fictitiousSwitch");
        assertNotNull(sw);
        sw.setProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.AC_LINE_SEGMENT);

        Properties exportParameters = new Properties();
        exportParameters.put(CgmesExport.PROFILES, List.of("EQ", "SSH"));

        String baseName = "fictitious-switch";
        network.write("CGMES", exportParameters, tmpDir.toAbsolutePath().resolve(baseName));
        assertEqSsh(baseName, sw.getId(), CgmesNames.AC_LINE_SEGMENT);

        sw.setProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.EQUIVALENT_BRANCH);
        network.write("CGMES", exportParameters, tmpDir.toAbsolutePath().resolve(baseName));
        assertEqSsh(baseName, sw.getId(), CgmesNames.EQUIVALENT_BRANCH);

        sw.setProperty(PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.SERIES_COMPENSATOR);
        network.write("CGMES", exportParameters, tmpDir.toAbsolutePath().resolve(baseName));
        assertEqSsh(baseName, sw.getId(), CgmesNames.SERIES_COMPENSATOR);
    }

    void assertEqSsh(String baseName, String switchId, String cgmesClassName) throws IOException {
        String eqXml = Files.readString(tmpDir.toAbsolutePath().resolve(baseName + "_EQ.xml"));
        String acLineSegment = getElement(eqXml, cgmesClassName, switchId);
        assertEquals("0", getAttribute(acLineSegment, cgmesClassName + ".r"));
        assertEquals("0", getAttribute(acLineSegment, cgmesClassName + ".x"));

        String sshXml = Files.readString(tmpDir.toAbsolutePath().resolve(baseName + "_SSH.xml"));
        String swTerminal1 = getElement(sshXml, "Terminal", switchId + "_SW_T_1");
        assertEquals("true", getAttribute(swTerminal1, "ACDCTerminal.connected"));
        String swTerminal2 = getElement(sshXml, "Terminal", switchId + "_SW_T_2");
        assertEquals("true", getAttribute(swTerminal2, "ACDCTerminal.connected"));
    }

    private static Network createFictitiousSwitchNetwork() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(115.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newSwitch()
                .setId("fictitiousSwitch")
                .setNode1(1)
                .setNode2(2)
                .setKind(SwitchKind.BREAKER)
                .setFictitious(true)
                .add();
        return network;
    }

    private static void assertPropertiesAndAliasesEmpty(Network network, boolean expected) {
        assertEquals(expected, network.getSubstationStream().allMatch(substation -> substation.getPropertyNames().isEmpty()));
        assertTrue(network.getSubstationStream().allMatch(substation -> substation.getAliases().isEmpty()));

        assertEquals(expected, network.getSwitchStream().allMatch(sw -> sw != null && sw.getPropertyNames().isEmpty()));
        assertEquals(expected, network.getSwitchStream().allMatch(sw -> sw != null && sw.getAliases().isEmpty()));
    }

    private static void assertEq(Network network) {
        assertEq(network.getSwitch("Breaker"));
    }

    private static void assertEqSsh(Network network) {
        assertSsh(network.getSwitch("Breaker"), false);
        assertSsh(network.getSwitch("Breaker-T2_SW_fict"), true);
        assertSsh(network.getSwitch("EnergyConsumer-T_SW_fict"), true);
    }

    private static void assertEqSshSsh1(Network network) {
        assertSsh(network.getSwitch("Breaker"), true);
        assertSsh(network.getSwitch("Breaker-T2_SW_fict"), false);
        assertSsh(network.getSwitch("EnergyConsumer-T_SW_fict"), false);
    }

    private static void assertFirstSsh(Network network) {
        assertSsh(network.getSwitch("Breaker"), false);
    }

    private static void assertSecondSsh(Network network) {
        assertSsh(network.getSwitch("Breaker"), true);
    }

    private static void assertEq(Switch sw) {
        assertNotNull(sw);
        assertNotNull(sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS));
        assertNotNull(sw.getProperty(PROPERTY_NORMAL_OPEN));
    }

    private static void assertSsh(Switch sw, boolean isOpen) {
        assertNotNull(sw);
        assertEquals(isOpen, sw.isOpen());
    }
}
