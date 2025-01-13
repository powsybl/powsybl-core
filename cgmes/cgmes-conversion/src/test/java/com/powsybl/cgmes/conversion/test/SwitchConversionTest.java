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

import static com.powsybl.cgmes.conversion.test.ConversionUtil.getUniqueMatches;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */

class SwitchConversionTest extends AbstractSerDeTest {

    @Test
    void jumperImportTest() {
        Network network = Network.read("jumperTest.xml", getClass().getResourceAsStream("/jumperTest.xml"));

        Switch aswitch = network.getSwitch("Jumper");
        assertEquals(SwitchKind.DISCONNECTOR, aswitch.getKind());
        assertEquals("Jumper", aswitch.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "switchType"));
        assertEquals("opened jumper", aswitch.getNameOrId());
        assertTrue(aswitch.isOpen());
        assertFalse(aswitch.isRetained());
    }

    @Test
    void groundDisconnectorTest() throws IOException {
        Network network = ConversionUtil.readCgmesResources("/", "groundTest.xml");
        assertEquals("GroundDisconnector", network.getSwitch("CO").getProperty("CGMES.switchType"));

        String eqFile = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDir);
        Pattern pattern = Pattern.compile("(<cim:GroundDisconnector rdf:ID=\"_CO\">)");
        assertEquals(1, getUniqueMatches(eqFile, pattern).size());
    }
}
