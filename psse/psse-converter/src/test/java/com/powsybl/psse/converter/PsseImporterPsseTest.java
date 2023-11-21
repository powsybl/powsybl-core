/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class PsseImporterPsseTest extends AbstractSerDeTest {

    // Test files obtained from a PSSE installation

    @Test
    void testIeee25version35() {
        Network n = testValid("/psse", "ieee_25_bus.rawx");
        ShuntCompensator sh = n.getShuntCompensator("B106-SwSH1");
        // The original PSSE shunt compensator has 1 block with 5 steps
        // This must be translated to an IIDM shunt compensator that has 6 sections
        // One section for each of the (accumulated) steps
        // plus one more section where there are not connected capacitors
        assertEquals(6, sh.getSectionCount());
        assertEquals(0.0, sh.getB(6));
    }

    private static Network load(String resourcePath, String sample) {
        String baseName = sample.substring(0, sample.lastIndexOf('.'));
        return Network.read(new ResourceDataSource(baseName, new ResourceSet(resourcePath, sample)));
    }

    private static Network testValid(String resourcePath, String sample) {
        return load(resourcePath, sample);
    }
}
