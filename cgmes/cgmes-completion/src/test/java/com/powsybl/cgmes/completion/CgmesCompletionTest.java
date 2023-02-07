/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.completion;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesCompletionTest {

    @Test
    public void miniGridNodeBreakerMissingVoltageLevel() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.PRE_PROCESSORS, "DefineMissingContainers");
        Network network = Network.read(CgmesConformity1ModifiedCatalog.miniGridNodeBreakerMissingVoltageLevel().dataSource(), importParams);
        assertNotNull(network);

        // Check that a specific terminal has a voltage level, navigating the CGMES model
        CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();
        String terminalId = "4915762d-133e-4209-8545-2822d095d7cd";
        String voltageLevelId = cgmes.voltageLevel(cgmes.terminal(terminalId), cgmes.isNodeBreaker());
        if (voltageLevelId == null || voltageLevelId.isEmpty()) {
            fail("Missing voltage level for terminal " + terminalId);
        }
    }

}
