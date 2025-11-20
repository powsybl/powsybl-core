/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class ControlAreaUpdateTest {

    private static final String DIR = "/update/control-area/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "controlArea_EQ.xml", "controlArea_EQ_BD.xml");
        assertEquals(1, network.getAreaCount());

        assertEq(network.getArea("ControlArea"));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "controlArea_EQ.xml", "controlArea_EQ_BD.xml", "controlArea_SSH.xml");
        assertEquals(1, network.getAreaCount());

        assertSsh(network.getArea("ControlArea"), 235.0, 10.0);
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "controlArea_EQ.xml", "controlArea_EQ_BD.xml");
        assertEquals(1, network.getAreaCount());

        assertEq(network.getArea("ControlArea"));

        readCgmesResources(network, DIR, "controlArea_SSH.xml");
        assertSsh(network.getArea("ControlArea"), 235.0, 10.0);

        readCgmesResources(network, DIR, "controlArea_SSH_1.xml");
        assertSsh(network.getArea("ControlArea"), 200.0, 1.1);
    }

    @Test
    void usePreviousValuesTest() {
        Network network = readCgmesResources(DIR, "controlArea_EQ.xml", "controlArea_EQ_BD.xml", "controlArea_SSH.xml");
        assertEquals(1, network.getAreaCount());
        assertSsh(network.getArea("ControlArea"), 235.0, 10.0);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.use-previous-values-during-update", "true");
        readCgmesResources(network, properties, DIR, "../empty_SSH.xml", "../empty_SV.xml");
        assertSsh(network.getArea("ControlArea"), 235.0, 10.0);
    }

    @Test
    void removeAllPropertiesAndAliasesTest() {
        Network network = readCgmesResources(DIR, "controlArea_EQ.xml", "controlArea_EQ_BD.xml", "controlArea_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, false);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.remove-properties-and-aliases-after-import", "true");
        network = readCgmesResources(properties, DIR, "controlArea_EQ.xml", "controlArea_EQ_BD.xml", "controlArea_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, true);
    }

    private static void assertPropertiesAndAliasesEmpty(Network network, boolean expected) {
        assertEquals(expected, network.getPropertyNames().isEmpty());
        assertTrue(network.getAliases().isEmpty());
        assertEquals(expected, network.getSubstationStream().allMatch(substation -> substation.getPropertyNames().isEmpty()));
        assertTrue(network.getSubstationStream().allMatch(substation -> substation.getAliases().isEmpty()));

        assertEquals(expected, network.getAreaStream().allMatch(area -> area.getPropertyNames().isEmpty()));
        assertTrue(network.getAreaStream().allMatch(area -> area.getAliases().isEmpty()));
    }

    private static void assertEq(Area area) {
        assertNotNull(area);
        assertEquals("ControlAreaTypeKind.Interchange", area.getAreaType());
        assertEquals(2, area.getAreaBoundaryStream().count());
        assertFalse(area.getInterchangeTarget().isPresent());
        assertEquals(1, area.getAreaBoundaryStream().filter(areaBoundary -> areaBoundary.getBoundary().isPresent()).count());
        assertEquals(1, area.getAreaBoundaryStream().filter(areaBoundary -> areaBoundary.getTerminal().isPresent()).count());
        assertEquals(0.0, area.getAcInterchange());
        assertEquals(0.0, area.getDcInterchange());
        assertEquals(0.0, area.getInterchange());
    }

    private static void assertSsh(Area area, double interchangeTarget, double pTolerance) {
        assertNotNull(area);
        double tol = 0.0000001;
        assertTrue(area.getInterchangeTarget().isPresent());
        assertEquals(interchangeTarget, area.getInterchangeTarget().orElseThrow(), tol);
        assertNotNull(area.getProperty(CgmesNames.P_TOLERANCE));
        assertEquals(pTolerance, Double.parseDouble(area.getProperty(CgmesNames.P_TOLERANCE)));
    }
}
