/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.conformity;

import com.powsybl.cgmes.conformity.CgmesConformity3Catalog;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity3ConversionTest {
    @Test
    public void microGridBaseCaseBEMergedWithNL() {
        Network be = Network.read(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource());
        assertNotEquals("unknown", be.getId());
        int nSubBE = be.getSubstationCount();
        int nDlBE = be.getDanglingLineCount();
        Network nl = Network.read(CgmesConformity3Catalog.microGridBaseCaseNL().dataSource());
        assertNotEquals("unknown", nl.getId());
        int nSubNL = nl.getSubstationCount();
        int nDlNL = nl.getDanglingLineCount();
        // Both networks have the same number of dangling lines
        assertEquals(nDlBE, nDlNL);
        be.merge(nl);
        int nSub = be.getSubstationCount();
        assertEquals(nSubBE + nSubNL, nSub);
        long nTl = be.getLineStream().filter(l -> l instanceof TieLine).count();
        // All dangling lines must have been converted to tie lines
        assertEquals(nDlBE, nTl);
    }
}
