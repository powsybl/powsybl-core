/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesConversionExtensionsTest {

    private static final String DIR = "/issues/";

    @Test
    void testCgmesConversionContextExtension() {
        Properties properties = new Properties();
        properties.put(CgmesImport.STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION, "true");
        Network network = readCgmesResources(properties, DIR, "empty_EQ.xml");
        CgmesConversionContextExtension extension = network.getExtension(CgmesConversionContextExtension.class);
        assertNotNull(extension);
        assertTrue(extension.getContext().config().storeCgmesConversionContextAsNetworkExtension());
        assertTrue(extension.getContext().cgmes().isNodeBreaker());
    }

    @Test
    void testCgmesModelExtensionEnabled() {
        Properties properties = new Properties();
        properties.put(CgmesImport.STORE_CGMES_MODEL_AS_NETWORK_EXTENSION, true);
        Network network = readCgmesResources(properties, DIR, "empty_EQ.xml");
        CgmesModelExtension extension = network.getExtension(CgmesModelExtension.class);
        assertNotNull(extension);
        assertTrue(extension.getCgmesModel().isNodeBreaker());
    }

    @Test
    void testCgmesModelExtensionDisabled() {
        Properties properties = new Properties();
        properties.put(CgmesImport.STORE_CGMES_MODEL_AS_NETWORK_EXTENSION, false);
        Network network = readCgmesResources(properties, DIR, "empty_EQ.xml");
        CgmesModelExtension extension = network.getExtension(CgmesModelExtension.class);
        assertNull(extension);
    }
}
