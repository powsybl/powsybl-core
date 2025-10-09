/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesConversionContextExtensionTest {

    @Test
    void test() {
        Properties properties = new Properties();
        properties.put(CgmesImport.STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION, "true");
        properties.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), NetworkFactory.findDefault(), properties);
        CgmesConversionContextExtension extension = network.getExtension(CgmesConversionContextExtension.class);
        assertNotNull(extension);
        assertTrue(extension.getContext().config().storeCgmesConversionContextAsNetworkExtension());
    }
}
