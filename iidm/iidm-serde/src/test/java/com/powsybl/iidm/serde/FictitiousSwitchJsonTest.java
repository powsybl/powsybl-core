/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class FictitiousSwitchJsonTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        ExportOptions exportOptions = new ExportOptions().setFormat(TreeDataFormat.JSON);
        ImportOptions importOptions = new ImportOptions().setFormat(TreeDataFormat.JSON);
        roundTripTest(FictitiousSwitchFactory.create(),
                (n, jsonFile) -> NetworkSerDe.write(n, exportOptions, jsonFile),
                jsonFile -> NetworkSerDe.read(jsonFile, importOptions),
                getVersionedNetworkPath("fictitiousSwitchRef.jiidm", CURRENT_IIDM_VERSION));

        //backward compatibility
        roundTripVersionedJsonFromMinToCurrentVersionTest("fictitiousSwitchRef.jiidm", IidmVersion.V_1_11);
        roundTripVersionedJsonFromMinToCurrentVersionTest("fictitiousSwitchRef.jiidm", IidmVersion.V_1_12);
    }

}
