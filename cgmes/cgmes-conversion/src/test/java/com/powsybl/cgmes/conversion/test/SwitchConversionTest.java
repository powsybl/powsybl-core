/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.regex.Pattern;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class SwitchConversionTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/switches/";

    @Test
    void jumperImportTest() {
        Network network = Network.read("jumperTest.xml", getClass().getResourceAsStream("/jumperTest.xml"));

        Switch aswitch = network.getSwitch("Jumper");
        assertEquals(SwitchKind.DISCONNECTOR, aswitch.getKind());
        assertEquals("Jumper", aswitch.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        assertEquals("opened jumper", aswitch.getNameOrId());
        assertTrue(aswitch.isOpen());
        assertFalse(aswitch.isRetained());
    }

    @Test
    void groundDisconnectorTest() throws IOException {
        Network network = ConversionUtil.readCgmesResources("/", "groundTest.xml");
        assertEquals("GroundDisconnector", network.getSwitch("CO").getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));

        String eqFile = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDir);
        Pattern pattern = Pattern.compile("(<cim:GroundDisconnector rdf:ID=\"_CO\">)");
        assertEquals(1, getUniqueMatches(eqFile, pattern).size());
    }

    @Test
    void lineWithZeroImpedanceTest() {
        // CGMES network:
        //   An ACLineSegment ACL with zero impedance between two nodes of the same voltage level.
        // IIDM network:
        //   A branch with 0 impedance inside a VoltageLevel is converted to a Switch.
        Network network = readCgmesResources(DIR, "line_with_0_impedance.xml");
        assertNotNull(network);

        // The line has been imported as a fictitious switch
        assertNull(network.getLine("ACL"));
        assertNotNull(network.getSwitch("ACL"));
        assertTrue(network.getSwitch("ACL").isFictitious());
    }

    @Test
    void fictitiousSwitchForDisconnectedTerminalTest() throws IOException {
        // CGMES network:
        //   A Load, whose terminal T_LD is disconnected, attached to a bus.
        // IIDM network:
        //   Fictitious Switch are created for disconnected terminals, but these shouldn't be exported back to CGMES.
        Network network = readCgmesResources(DIR, "disconnected_terminal_EQ.xml", "disconnected_terminal_SSH.xml");
        assertNotNull(network);

        // A fictitious switch has been created for the disconnected terminal.
        Switch fictitiousSwitch = network.getSwitch("T_LD_SW_fict");
        assertNotNull(fictitiousSwitch);
        assertTrue(fictitiousSwitch.isFictitious());
        assertTrue(fictitiousSwitch.isOpen());
        assertEquals("true", fictitiousSwitch.getProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL));

        // The fictitious switch isn't present in the EQ export and the terminal is disconnected in the SSH.
        String eqExport = writeCgmesProfile(network, "EQ", tmpDir);
        String sshExport = writeCgmesProfile(network, "SSH", tmpDir);
        Pattern switchPattern = Pattern.compile("<cim:Breaker rdf:ID=\"(.*?)\">");
        Pattern terminalPattern = Pattern.compile("<cim:Terminal rdf:about=\"#_T_LD\">.*?" +
                "<cim:ACDCTerminal.connected>(.*?)</cim:ACDCTerminal.connected>.*?</cim:Terminal>", Pattern.DOTALL);
        assertNull(getFirstMatch(eqExport, switchPattern));
        assertEquals("false", getFirstMatch(sshExport, terminalPattern));

        // If the fictitious switch gets closed, it still isn't exported but the terminal now gets connected.
        fictitiousSwitch.setOpen(false);
        eqExport = writeCgmesProfile(network, "EQ", tmpDir);
        sshExport = writeCgmesProfile(network, "SSH", tmpDir);
        assertNull(getFirstMatch(eqExport, switchPattern));
        assertEquals("true", getFirstMatch(sshExport, terminalPattern));
    }
}
