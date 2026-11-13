/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.api.TripleStoreOptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_CGMES_ORIGINAL_CLASS;
import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_NORMAL_OPEN;
import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SwitchUpdateTest extends AbstractSerDeTest {

    private static final String DIR = "/update/switch/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml");
        assertEquals(4, network.getSwitchCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherAndSsh1LaterTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml", "switch_SSH.xml");
        assertEquals(9, network.getSwitchCount());

        assertEqSsh(network);

        readCgmesResources(network, DIR, "switch_SSH_1.xml");
        assertEqSshSsh1(network);
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml");
        assertEquals(4, network.getSwitchCount()); // No fictitious switch was created

        assertEq(network);

        readCgmesResources(network, DIR, "switch_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "switch_SSH_1.xml");
        assertSecondSsh(network);
    }

    @Test
    void terminalsQueriedOnceDuringUpdateTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml");
        assertEquals(4, network.getSwitchCount());

        // Same model as CgmesImport.update builds, spied to count the terminals query
        TripleStoreOptions options = new TripleStoreOptions();
        options.setQueryCatalog(Conversion.QUERY_CATALOG_NAME_UPDATE);
        CgmesModel cgmes = spy(CgmesModelFactory.create(new ResourceDataSource("SSH", new ResourceSet(DIR, "switch_SSH.xml")),
                null, TripleStoreFactory.DEFAULT_IMPLEMENTATION, ReportNode.NO_OP, options));
        new Conversion(cgmes, new Conversion.Config()).update(network, ReportNode.NO_OP);

        // The update cache and the creation of fictitious switches for disconnected terminals share one query
        verify(cgmes, times(1)).terminals();
        assertFirstSsh(network);
        assertEquals(9, network.getSwitchCount()); // The 5 fictitious switches for the disconnected terminals are still created
    }

    @Test
    void usePreviousValuesTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml", "switch_SSH.xml");
        assertEquals(9, network.getSwitchCount());
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

    @Test
    void seriesCompensatorOpenStateRoundTripTest() {
        Network network = readCgmesResources("/update/switch/", "switch_EQ.xml", "switch_SSH_1.xml");

        Switch breaker = network.getSwitch("Breaker");
        Switch seriesCompensator = network.getSwitch("SeriesCompensator");
        Switch equivalentBranch = network.getSwitch("EquivalentBranch");
        Switch acLineSegment = network.getSwitch("ACLineSegment");

        assertTrue(breaker.isOpen());
        assertFalse(seriesCompensator.isOpen());
        assertTrue(equivalentBranch.isOpen());
        assertFalse(acLineSegment.isOpen());

        breaker.setOpen(false);
        seriesCompensator.setOpen(true);
        equivalentBranch.setOpen(false);
        acLineSegment.setOpen(true);

        Properties exportParameters = new Properties();
        exportParameters.put(CgmesExport.PROFILES, List.of("SSH"));

        String baseName = "openState-roundtrip";
        network.write("CGMES", exportParameters, tmpDir.toAbsolutePath().resolve(baseName));

        breaker.setOpen(true);
        seriesCompensator.setOpen(false);
        equivalentBranch.setOpen(true);
        acLineSegment.setOpen(false);

        Properties importParameters = new Properties();
        importParameters.put(CgmesImport.USE_PREVIOUS_VALUES_DURING_UPDATE, "true");
        network.update(new GenericReadOnlyDataSource(tmpDir.toAbsolutePath(), baseName), importParameters);

        assertFalse(network.getSwitch("Breaker").isOpen());
        assertTrue(network.getSwitch("SeriesCompensator").isOpen());
        assertFalse(network.getSwitch("EquivalentBranch").isOpen());
        assertTrue(network.getSwitch("ACLineSegment").isOpen());
    }

    private static void assertPropertiesAndAliasesEmpty(Network network, boolean expected) {
        assertEquals(expected, network.getSubstationStream().allMatch(substation -> substation.getPropertyNames().isEmpty()));
        assertTrue(network.getSubstationStream().allMatch(substation -> substation.getAliases().isEmpty()));

        assertEquals(expected, network.getSwitchStream().allMatch(sw -> sw != null && sw.getPropertyNames().isEmpty()));
        assertEquals(expected, network.getSwitchStream().allMatch(sw -> sw != null && sw.getAliases().isEmpty()));
    }

    private static void assertEq(Network network) {
        assertEq(network.getSwitch("SeriesCompensator"));
        assertEq(network.getSwitch("Breaker"));
        assertEq(network.getSwitch("EquivalentBranch"));
        assertEq(network.getSwitch("ACLineSegment"));
    }

    private static void assertEqSsh(Network network) {
        assertSsh(network.getSwitch("SeriesCompensator"), true);
        assertSsh(network.getSwitch("Breaker"), false);
        assertSsh(network.getSwitch("SeriesCompensator-T1_SW_fict"), true);
        assertSsh(network.getSwitch("Breaker-T2_SW_fict"), true);
        assertSsh(network.getSwitch("EnergyConsumer-T_SW_fict"), true);
        assertSsh(network.getSwitch("EquivalentBranch"), false);
        assertSsh(network.getSwitch("ACLineSegment"), true);
        assertSsh(network.getSwitch("ACLineSegment-T1_SW_fict"), true);
        assertSsh(network.getSwitch("ACLineSegment-T2_SW_fict"), true);
    }

    private static void assertEqSshSsh1(Network network) {
        assertSsh(network.getSwitch("SeriesCompensator"), false);
        assertSsh(network.getSwitch("Breaker"), true);
        assertSsh(network.getSwitch("SeriesCompensator-T1_SW_fict"), false);
        assertSsh(network.getSwitch("Breaker-T2_SW_fict"), false);
        assertSsh(network.getSwitch("EnergyConsumer-T_SW_fict"), false);
        assertSsh(network.getSwitch("EquivalentBranch"), true);
        assertSsh(network.getSwitch("ACLineSegment"), false);
        assertSsh(network.getSwitch("ACLineSegment-T1_SW_fict"), false);
        assertSsh(network.getSwitch("ACLineSegment-T2_SW_fict"), false);
    }

    private static void assertFirstSsh(Network network) {
        assertSsh(network.getSwitch("SeriesCompensator"), true);
        assertSsh(network.getSwitch("Breaker"), false);
        assertSsh(network.getSwitch("EquivalentBranch"), false);
        assertSsh(network.getSwitch("ACLineSegment"), true);
    }

    private static void assertSecondSsh(Network network) {
        assertSsh(network.getSwitch("SeriesCompensator"), false);
        assertSsh(network.getSwitch("Breaker"), true);
        assertSsh(network.getSwitch("EquivalentBranch"), true);
        assertSsh(network.getSwitch("ACLineSegment"), false);
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
