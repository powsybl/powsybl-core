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
 * @author Luma Zamarreno {@literal <zamarrenolm at aia.es>}
 */

class BadFilenamesTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/bad-file-names/";

    @Test
    void badFilenamesTest() {
        // From issue #3205
        // CGMES instance files have a bad naming scheme: "_EQ_" is present in the SSH instance filename:
        //   <dateTime>_EQ_<sourcingActor>_SSH.xml
        // Default import tries to split data source with potentially multiple IGMs
        // using the modelling authority of all EQ instance files
        // In this attempt to split the EQ instance files were checked only using its filename
        // To fix the issue reported in #3205 we need to check if a file is EQ instance looking at its model.Profile

        Network network = readCgmesResources(DIR, "dateTime_EQ_sourcingActor_EQ.xml", "dateTime_EQ_sourcingActor_SSH.xml");
        assertNotNull(network);
        assertEquals(0, network.getSubnetworks().size());
    }

}
