/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class SubstationConversionTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/substation/";

    @Test
    void duplicateRegionsTest() {
        // CGMES network:
        //   3 Substations, each appearing once in EQ file and once in EQ_BD file.
        //   ST_1 has duplicate SubGeographicalRegion.rdf:Id associated (SGR_1 // SGR_1_duplicate).
        //   ST_2 has duplicate GeographicalRegion.rdf:Id associated (GR_2 // GR_2_duplicate).
        //   ST_3 has duplicate GeographicalRegion.name associated (Geographical Region 3 // Geographical Region 3 duplicate).
        // IIDM network:
        //   Import succeeds.
        //   Only 1 of the duplicated Substation is kept.
        Network network = readCgmesResources(DIR, "duplicate_regions_EQ.xml", "duplicate_regions_EQ_BD.xml");
        assertNotNull(network);

        // Only the min substation is kept in case of duplicates.
        assertEquals(3, network.getSubstationCount());
        assertEquals("SGR_1", network.getSubstation("ST_1").getProperty("CGMES.subRegionId"));
        assertEquals("GR_2", network.getSubstation("ST_2").getProperty("CGMES.regionId"));
        assertEquals("Geographical region 3", network.getSubstation("ST_3").getProperty("CGMES.regionName"));
    }

}
