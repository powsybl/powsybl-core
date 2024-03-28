/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.util;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.ucte.converter.UcteImporter;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class UcteAliasesCreationTest {

    private static Network loadNetworkFromResourceFile(String filePath) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath), new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
    }

    @Test
    void checkAliasesCreationWhenImportingMergedFile() {
        Network network = loadNetworkFromResourceFile("/uxTestGridForMerging.uct");

        // Dangling lines are there but paired
        assertNotNull(network.getIdentifiable("BBBBBB11 XXXXXX11 1"));
        assertTrue(network.getDanglingLine("BBBBBB11 XXXXXX11 1").isPaired());
        assertNotNull(network.getIdentifiable("FFFFFF11 XXXXXX11 1"));
        assertTrue(network.getDanglingLine("FFFFFF11 XXXXXX11 1").isPaired());

        // No aliases on element name
        assertNull(network.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"));
        assertNull(network.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"));

        UcteAliasesCreation.createAliases(network);

        // Aliases on element name have been created
        assertNotNull(network.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"));
        assertNotNull(network.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"));

        // Aliases on disappeared dangling lines have been created
        assertNotNull(network.getIdentifiable("BBBBBB11 XXXXXX11 1"));
        assertNotNull(network.getIdentifiable("FFFFFF11 XXXXXX11 1"));

        // They are all referencing the same identifiable
        assertEquals(network.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"), network.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"));
        assertEquals(network.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"), network.getDanglingLine("BBBBBB11 XXXXXX11 1").getTieLine().orElse(null));
        assertEquals(network.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"), network.getDanglingLine("FFFFFF11 XXXXXX11 1").getTieLine().orElse(null));
    }

    @Test
    void checkAliasesCreationBeforeIidmMerging() {
        Network networkFR = loadNetworkFromResourceFile("/frTestGridForMerging.uct");
        Network networkBE = loadNetworkFromResourceFile("/beTestGridForMerging.uct");

        UcteAliasesCreation.createAliases(networkBE);
        UcteAliasesCreation.createAliases(networkFR);

        // Aliases on element name have been created
        assertNotNull(networkBE.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"));
        assertNotNull(networkFR.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"));

        Network merge = Network.merge(networkBE, networkFR);

        // Aliases on element name have been kept after merge
        assertNotNull(merge.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"));
        assertNotNull(merge.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"));

        // Aliases on disappeared dangling lines have been created after merge
        assertNotNull(merge.getIdentifiable("BBBBBB11 XXXXXX11 1"));
        assertNotNull(merge.getIdentifiable("FFFFFF11 XXXXXX11 1"));

        // They are all referencing the same identifiable
        assertEquals(merge.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"), merge.getDanglingLine("BBBBBB11 XXXXXX11 1"));
        assertEquals(merge.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"), merge.getDanglingLine("FFFFFF11 XXXXXX11 1"));
    }

    @Test
    void checkAliasesCreationAfterIidmMerging() {
        Network networkFR = loadNetworkFromResourceFile("/frTestGridForMerging.uct");
        Network networkBE = loadNetworkFromResourceFile("/beTestGridForMerging.uct");
        Network merge = Network.merge(networkBE, networkFR);

        // No aliases on dangling lines element name
        assertNull(merge.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"));
        assertNull(merge.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"));

        // Aliases on disappeared dangling lines ids are created
        assertNotNull(merge.getIdentifiable("BBBBBB11 XXXXXX11 1"));
        assertNotNull(merge.getIdentifiable("FFFFFF11 XXXXXX11 1"));
        assertEquals("BBBBBB11 XXXXXX11 1", merge.getIdentifiable("BBBBBB11 XXXXXX11 1").getId());
        assertEquals("FFFFFF11 XXXXXX11 1", merge.getIdentifiable("FFFFFF11 XXXXXX11 1").getId());

        UcteAliasesCreation.createAliases(merge);

        // Aliases on element name have been created
        assertNotNull(merge.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"));
        assertNotNull(merge.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"));

        // Aliases on disappeared dangling lines are still there
        assertNotNull(merge.getIdentifiable("BBBBBB11 XXXXXX11 1"));
        assertNotNull(merge.getIdentifiable("FFFFFF11 XXXXXX11 1"));

        // They are all referencing the same identifiable
        assertEquals(merge.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"), merge.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"));
        assertEquals(merge.getIdentifiable("BBBBBB11 XXXXXX11 ABCDE"), merge.getDanglingLine("BBBBBB11 XXXXXX11 1").getTieLine().orElse(null));
        assertEquals(merge.getIdentifiable("FFFFFF11 XXXXXX11 ABCDE"), merge.getDanglingLine("FFFFFF11 XXXXXX11 1").getTieLine().orElse(null));
    }

    @Test
    void checkThatItDoesNotCreateDuplicatedAliasesNorThrow() {
        Network network = loadNetworkFromResourceFile("/aliasesDuplicationTest.uct");
        UcteAliasesCreation.createAliases(network);

        // Devices exist
        assertNotNull(network.getIdentifiable("FFFFFF11 FFFFFF12 1"));
        assertNotNull(network.getIdentifiable("FFFFFF11 FFFFFF12 2"));
        assertNotNull(network.getIdentifiable("FFFFFF11 FFFFFF12 3"));

        // Duplicated aliases does not exist
        assertNull(network.getIdentifiable("FFFFFF11 FFFFFF12 N/A"));

        // Non duplicated alias exists
        assertNotNull(network.getIdentifiable("FFFFFF11 FFFFFF12 Unique"));
    }
}
