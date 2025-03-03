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

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class SwitchConversionTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/switches/";

    @Test
    void basicSwitchTest() {
        // CGMES network:
        //   A Breaker with all optional fields defined, and a Disconnector with just the required fields defined.
        // IIDM network:
        //   All fields have been correctly read.
        Network network = readCgmesResources(DIR, "basic_switch.xml");
        assertNotNull(network);

        // Breaker has all optional fields defined.
        Switch breaker = network.getSwitch("BR");
        assertEquals(SwitchKind.BREAKER, breaker.getKind());
        assertEquals("Breaker", breaker.getNameOrId());
        assertTrue(breaker.isOpen());
        assertTrue(breaker.isRetained());

        // Disconnector has only the required fields defined.
        Switch disconnector = network.getSwitch("DIS");
        assertEquals(SwitchKind.DISCONNECTOR, disconnector.getKind());
        assertEquals("DIS", disconnector.getNameOrId());  // Returns ID since name is undefined
        assertFalse(disconnector.isOpen());  // default value if undefined is false
        assertFalse(disconnector.isRetained());  // default value if undefined is false
    }

    @Test
    void switchKindTest() throws IOException {
        // CGMES network:
        //   A Breaker BR, Disconnector DIS, LoadBreakSwitch LBS (direct map to IIDM),
        //   Switch SW, ProtectedSwitch PSW, GroundDisconnector GRD, Jumper JUM (indirect map to IIDM).
        // IIDM network:
        //   All Switch are imported.
        //   If the CGMES original class doesn't correspond to an IIDM kind, it is saved in a property.
        Network network = readCgmesResources(DIR, "switch_kind.xml");
        assertNotNull(network);

        // Check that the switch kind is correct.
        assertEquals(SwitchKind.BREAKER, network.getSwitch("BR").getKind());
        assertEquals(SwitchKind.DISCONNECTOR, network.getSwitch("DIS").getKind());
        assertEquals(SwitchKind.LOAD_BREAK_SWITCH, network.getSwitch("LBS").getKind());
        assertEquals(SwitchKind.BREAKER, network.getSwitch("SW").getKind());
        assertEquals(SwitchKind.BREAKER, network.getSwitch("PSW").getKind());
        assertEquals(SwitchKind.DISCONNECTOR, network.getSwitch("GRD").getKind());
        assertEquals(SwitchKind.DISCONNECTOR, network.getSwitch("JUM").getKind());

        // For Switch, ProtectedSwitch, GroundDisconnector, Jumper (indirect mapping), the CGMES original class is stored.
        assertNull(network.getSwitch("BR").getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        assertNull(network.getSwitch("DIS").getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        assertNull(network.getSwitch("LBS").getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        assertEquals("Switch", network.getSwitch("SW").getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        assertEquals("ProtectedSwitch", network.getSwitch("PSW").getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        assertEquals("GroundDisconnector", network.getSwitch("GRD").getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        assertEquals("Jumper", network.getSwitch("JUM").getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));

        // Correct class is restored in CGMES EQ and SSH export.
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);
        String sshFile = writeCgmesProfile(network, "SSH", tmpDir);
        assertTrue(containsObject(eqFile, sshFile, "Breaker", "BR"));
        assertTrue(containsObject(eqFile, sshFile, "Disconnector", "DIS"));
        assertTrue(containsObject(eqFile, sshFile, "LoadBreakSwitch", "LBS"));
        assertTrue(containsObject(eqFile, sshFile, "Switch", "SW"));
        assertTrue(containsObject(eqFile, sshFile, "ProtectedSwitch", "PSW"));
        assertTrue(containsObject(eqFile, sshFile, "GroundDisconnector", "GRD"));
        assertTrue(containsObject(eqFile, sshFile, "Jumper", "JUM"));
    }

    private boolean containsObject(String eqFile, String sshFile, String className, String rdfId) {
        return eqFile.contains("<cim:" + className + " rdf:ID=\"_" + rdfId + "\">")
                && sshFile.contains("<cim:" + className + " rdf:about=\"#_" + rdfId + "\">");
    }

    @Test
    void switchInBusBranchTest() throws IOException {
        // CGMES network:
        //   A bus-branch network with a non-retained (doesn't make sense) Disconnector DIS.
        // IIDM network:
        //   In bus breaker topology kind, all switches are Breaker and retained.
        Network network = readCgmesResources(DIR, "switch_in_bus_branch_EQ.xml", "switch_in_bus_branch_TP.xml");
        assertNotNull(network);

        // The Switch is imported with kind breaker, and its original CGMES class is stored.
        Switch disconnector = network.getSwitch("DIS");
        assertEquals(SwitchKind.BREAKER, disconnector.getKind());
        assertEquals("Disconnector", disconnector.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));

        // The Switch is retained in IIDM, even though it is not in the original CGMES file.
        assertTrue(disconnector.isRetained());

        // It is a retained Disconnector in the CGMES export.
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);
        String xmlDisconnector = getElement(eqFile, "Disconnector", "DIS");
        assertTrue(xmlDisconnector.contains("<cim:Switch.retained>true</cim:Switch.retained>"));
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
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);
        String sshFile = writeCgmesProfile(network, "SSH", tmpDir);
        assertFalse(eqFile.contains("<cim:Breaker rdf:ID="));
        String xmlLoadTerminal = getElement(sshFile, "Terminal", "T_LD");
        assertTrue(xmlLoadTerminal.contains("<cim:ACDCTerminal.connected>false</cim:ACDCTerminal.connected>"));

        // If the fictitious switch gets closed, it still isn't exported but the terminal now gets connected.
        fictitiousSwitch.setOpen(false);
        eqFile = writeCgmesProfile(network, "EQ", tmpDir);
        sshFile = writeCgmesProfile(network, "SSH", tmpDir);
        assertFalse(eqFile.contains("<cim:Breaker rdf:ID="));
        xmlLoadTerminal = getElement(sshFile, "Terminal", "T_LD");
        assertTrue(xmlLoadTerminal.contains("<cim:ACDCTerminal.connected>true</cim:ACDCTerminal.connected>"));
    }

    @Test
    void retainedSwitchTest() throws IOException {
        // IIDM network:
        //   Two BusbarSections BBS_1 and BBS_2 connected by a COUPLER Switch.
        //   A feeder bay with two Disconnectors, 1 Breaker, 1 Load also can couple the two bars.
        // CGMES export:
        //   A retained switch cannot have both its terminals associated to the same topological node.
        Network network = readCgmesResources(DIR, "retained_switch.xml");

        // Open one disconnector so that the 2 ends of the retained switch are on different buses/topological nodes.
        network.getSwitch("DIS_1").setOpen(true);
        VoltageLevel.BusBreakerView bbv = network.getVoltageLevel("VL").getBusBreakerView();
        assertNotEquals(bbv.getBus1("COUPLER"), bbv.getBus2("COUPLER"));

        // The retained switch can be exported as such.
        String eqExport = writeCgmesProfile(network, "EQ", tmpDir);
        String xmlCoupler = getElement(eqExport, "Breaker", "COUPLER");
        assertTrue(xmlCoupler.contains("<cim:Switch.retained>true</cim:Switch.retained>"));

        // Now close the disconnector so that the 2 ends of the retained switch are on the same bus/topological node.
        network.getSwitch("DIS_1").setOpen(false);
        assertEquals(bbv.getBus1("COUPLER"), bbv.getBus2("COUPLER"));

        // The retained switch can't be exported as such.
        eqExport = writeCgmesProfile(network, "EQ", tmpDir);
        xmlCoupler = getElement(eqExport, "Breaker", "COUPLER");
        assertTrue(xmlCoupler.contains("<cim:Switch.retained>false</cim:Switch.retained>"));
    }
}
