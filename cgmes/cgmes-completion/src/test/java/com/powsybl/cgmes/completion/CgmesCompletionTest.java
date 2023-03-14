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
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
class CgmesCompletionTest {

    @Test
    void miniGridNodeBreakerMissingVoltageLevel() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.PRE_PROCESSORS, "CreateMissingContainers");

        // The only way to pass the output folder where we want the fixes to be written is to use a config file
        // Its contents should be:
        //
        //   import-export-parameters-default-value:
        //     iidm.import.cgmes.fixes-for-missing-containers-filename: "/user/working/area/fixes/..."
        //
        // For tests, it is not possible to put a reference to a Jimfs folder,
        // Even if we write the uri of the folder, and read it back as an uri,
        // the default file system is used to interpret it
        //
        // An alternative would be to write in the config a temp folder that we create here in the tests,
        // But that means writing in a resource file (the actual path of the temp folder), it is dirty.
        //
        // Instead, the CGMES completion processor creates itself a temp folder when no parameter is received.
        // We won't receive directly the file with the fixes, but we do not care.

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
