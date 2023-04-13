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
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
class PsseImporterPsseTest extends AbstractConverterTest {

    // Test files obtained from a PSSE installation

    @Test
    void testIeee25version35() {
        testValid("/psse", "ieee_25_bus.rawx");
    }

    private static Network load(String resourcePath, String sample) {
        String baseName = sample.substring(0, sample.lastIndexOf('.'));
        return Network.read(new ResourceDataSource(baseName, new ResourceSet(resourcePath, sample)));
    }

    private static void testValid(String resourcePath, String sample) {
        load(resourcePath, sample);
    }
}
