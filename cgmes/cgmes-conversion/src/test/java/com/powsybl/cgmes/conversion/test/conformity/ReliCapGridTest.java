/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test.conformity;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.powsybl.cgmes.conformity.ReliCapGridCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.elements.SwitchConversion;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class ReliCapGridTest {

    private Properties importParams;

    @BeforeEach
    void setUp() {
        importParams = new Properties();
        importParams.put(CgmesImport.SILENCE_FREQUENT_ISSUES_WARNINGS, "true"); // for coverage of this option
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(SwitchConversion.class).setLevel(Level.DEBUG); // for coverage of boundary switch debug log
    }

    @AfterEach
    void restoreLogger() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger(SwitchConversion.class).setLevel(null);
    }

    @Test
    void igmBelgoviaTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.belgovia().dataSource();
        Network network = Network.read(ds, importParams);

        assertNotNull(network);
        assertEquals(2, network.getSubstationCount());
    }

    @Test
    void igmBritheimTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.britheim().dataSource();
        Network network = Network.read(ds, importParams);

        assertNotNull(network);
        assertEquals(2, network.getSubstationCount());
    }

    @Test
    void igmEspheimTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.espheim().dataSource();
        Network network = Network.read(ds, importParams);

        assertNotNull(network);
        assertEquals(104, network.getSubstationCount());
    }

    @Test
    void igmGaliaTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.galia().dataSource();
        Network network = Network.read(ds, importParams);

        assertNotNull(network);
        assertEquals(1, network.getSubstationCount());
    }

    @Test
    void igmNordheimTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.nordheim().dataSource();
        Network network = Network.read(ds, importParams);

        assertNotNull(network);
        assertEquals(1, network.getSubstationCount());
    }

    @Test
    void igmSvedalaTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.svedala().dataSource();
        Network network = Network.read(ds, importParams);

        assertNotNull(network);
        assertEquals(56, network.getSubstationCount());
    }

    @Test
    void igmHvdcEspheimSvedalaTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.hvdcEspheimSvedala().dataSource();
        Network network = Network.read(ds, importParams);

        assertNotNull(network);
        assertEquals(1, network.getSubstationCount());
    }

    @Test
    void igmHvdcNordheimGaliaTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.hvdcNordheimGalia().dataSource();
        Network network = Network.read(ds, importParams);

        assertNotNull(network);
        assertEquals(1, network.getSubstationCount());
    }

    @Test
    void cgmNineRealmsSeparatingByFilenameTest() {
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS_DEFINED_BY, CgmesImport.SubnetworkDefinedBy.FILENAME.name());
        ReadOnlyDataSource ds = ReliCapGridCatalog.nineRealms().dataSource();
        Network network = Network.read(ds, importParams);

        assertNotNull(network);
        assertEquals(8, network.getSubnetworks().size());
        assertEquals(168, network.getSubstationCount());
        // Subnetworks are sorted by IGM name: Belgovia, Britheim, Espheim, Galia,
        // HVDC-Espheim-Svedala, HVDC-Nordheim-Galia, Nordheim, Svedala
        assertEquals(List.of(
                "urn:uuid:15c8a8ad-08d5-4b59-8417-db80e273bd1a",
                "urn:uuid:33bd4875-01b7-4442-a5a2-dc04419143bf",
                "urn:uuid:e3beb946-afc6-4304-9660-517d5115cbb0",
                "urn:uuid:b89a6150-43bd-442c-8c58-9efd3d537d11",
                "urn:uuid:105893e4-668b-4b00-a351-0e436b53cbc9",
                "urn:uuid:9212d819-3f09-4062-aa13-82d4be4e2f4a",
                "urn:uuid:b2d38b16-0d3a-4d6d-8bf2-e894632a2912",
                "urn:uuid:bea45848-a05d-496b-9ab2-f42c6714183e"),
                network.getSubnetworks().stream().map(Network::getId).toList());
    }

    @Test
    void cgmNineRealmsSeparatingByModelingAuthorityTest() {
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS_DEFINED_BY, CgmesImport.SubnetworkDefinedBy.MODELING_AUTHORITY.name());
        ReadOnlyDataSource ds = ReliCapGridCatalog.nineRealms().dataSource();
        Network network = Network.read(ds, importParams);

        assertNotNull(network);
        assertEquals(8, network.getSubnetworks().size());
        assertEquals(168, network.getSubstationCount());
        // Subnetworks are sorted by modeling authority:
        // " http://belgovia.bo/CGMES" (Belgovia, note the leading space),
        // "http://britheim.bh/CGMES" (Britheim),
        // "http://espheim-svedala.es/CGMES" (HVDC-Espheim-Svedala),
        // "http://espheim.eh/CGMES" (Espheim),
        // "http://galia.ga/CGMES" (Galia),
        // "http://nordheim-galia.ng/CGMES" (HVDC-Nordheim-Galia),
        // "http://nordheim.nh/CGMES" (Nordheim),
        // "http://svedala.sd/CGMES" (Svedala)
        assertEquals(List.of(
                "urn:uuid:15c8a8ad-08d5-4b59-8417-db80e273bd1a",
                "urn:uuid:33bd4875-01b7-4442-a5a2-dc04419143bf",
                "urn:uuid:105893e4-668b-4b00-a351-0e436b53cbc9",
                "urn:uuid:e3beb946-afc6-4304-9660-517d5115cbb0",
                "urn:uuid:b89a6150-43bd-442c-8c58-9efd3d537d11",
                "urn:uuid:9212d819-3f09-4062-aa13-82d4be4e2f4a",
                "urn:uuid:b2d38b16-0d3a-4d6d-8bf2-e894632a2912",
                "urn:uuid:bea45848-a05d-496b-9ab2-f42c6714183e"),
                network.getSubnetworks().stream().map(Network::getId).toList());
    }
}
