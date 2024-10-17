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
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.regex.Pattern;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class SwitchConversionTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/switches/";

    @Test
    void lineWithZeroImpedanceTest() {
        // CGMES network:
        //   An ACLineSegment ACL_12 with zero impedance between two nodes of the same voltage level.
        // IIDM network:
        //   A branch with 0 impedance inside a VoltageLevel is converted to a Switch.
        Network network = readCgmesResources(DIR, "line_with_0_impedance.xml");
        assertNotNull(network);

        // The line has been imported as a fictitious switch
        assertNull(network.getLine("ACL_12"));
        assertNotNull(network.getSwitch("ACL_12"));
        assertTrue(network.getSwitch("ACL_12").isFictitious());
    }

    @Test
    void switchKindTest() throws IOException {
        // CGMES network:
        //   A LoadBreakSwitch, a generic Switch, and a Disconnector without name.
        // IIDM network:
        //   The switch kind is preserved. A generic CGMES switch is considered of kind breaker.
        Network network = readCgmesResources(DIR, "switch_kind.xml");
        assertNotNull(network);

        // Disconnector has been imported even though it has no name. Method getNameOrId() returns its id.
        assertNotNull(network.getSwitch("DIS"));
        assertEquals("DIS", network.getSwitch("DIS").getNameOrId());

        // The switch kind has been preserved if supported, or replaced by breaker otherwise.
        assertEquals(SwitchKind.DISCONNECTOR, network.getSwitch("DIS").getKind());
        assertEquals(SwitchKind.LOAD_BREAK_SWITCH, network.getSwitch("LBS").getKind());
        assertEquals(SwitchKind.BREAKER, network.getSwitch("SW").getKind());
        assertEquals("Switch", network.getSwitch("SW").getProperty("CGMES.switchType"));

        // The original switch kind is restored in CGMES export.
        String eqExport = writeCgmesProfile(network, "EQ", tmpDir);
        Pattern switchPattern = Pattern.compile("<cim:Switch rdf:ID=\"_(.*?)\">");
        assertEquals("SW", getFirstMatch(eqExport, switchPattern));
    }

    @Test
    void fictitiousSwitchForDisconnectedTerminalTest() throws IOException {
        // CGMES network:
        //   A Load, whose terminal T_LD is disconnected, attached to a bus.
        // IIDM network:
        //   Fictitious switches are created for disconnecter terminals, but these shouldn't be exported back to CGMES.
        Network network = readCgmesResources(DIR,
                "disconnected_terminal_EQ.xml", "disconnected_terminal_SSH.xml");
        assertNotNull(network);

        // Check that a fictitious switch has been created for the disconnected terminal.
        Switch fictSwitch = network.getSwitch("T_LD_SW_fict");
        assertNotNull(fictSwitch);
        assertTrue(fictSwitch.isFictitious());
        assertTrue(fictSwitch.isOpen());
        assertEquals("true", fictSwitch.getProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL));

        // Check that the fictitious switch isn't present in the EQ export and the terminal is disconnected in the SSH.
        String eqExport = writeCgmesProfile(network, "EQ", tmpDir);
        String sshExport = writeCgmesProfile(network, "SSH", tmpDir);
        Pattern switchPattern = Pattern.compile("<cim:Breaker rdf:ID=\"(.*?)\">");
        Pattern terminalPattern = Pattern.compile("<cim:Terminal rdf:about=\"#_T_LD\">.*?" +
                "<cim:ACDCTerminal.connected>(.*?)</cim:ACDCTerminal.connected>.*?</cim:Terminal>", Pattern.DOTALL);
        assertNull(getFirstMatch(eqExport, switchPattern));
        assertEquals("false", getFirstMatch(sshExport, terminalPattern));

        // Check that if the fictitious switch gets closed, it still isn't exported but the terminal now gets connected.
        fictSwitch.setOpen(false);
        eqExport = writeCgmesProfile(network, "EQ", tmpDir);
        sshExport = writeCgmesProfile(network, "SSH", tmpDir);
        assertNull(getFirstMatch(eqExport, switchPattern));
        assertEquals("true", getFirstMatch(sshExport, terminalPattern));
    }
}
