/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesConversionContextExtensionTest {

    @Test
    public void test() {
        Properties properties = new Properties();
        properties.put(CgmesImport.STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION, "true");
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), NetworkFactory.findDefault(), properties);
        CgmesConversionContextExtension extension = network.getExtension(CgmesConversionContextExtension.class);
        assertNotNull(extension);
        assertTrue(extension.getContext().config().storeCgmesConversionContextAsNetworkExtension());
    }
}
