/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity3Catalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.networkModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class LoadConversionTest {

    @Test
    void microGridBaseCaseNL() {
        Conversion.Config config = new Conversion.Config();
        for (var referenceResource : List.of(CgmesConformity1Catalog.microGridBaseCaseNL(),
                                             CgmesConformity3Catalog.microGridBaseCaseNL())) {
            Network n = networkModel(referenceResource, config);
            Load l1 = n.getLoad("69add5b4-70bd-4360-8a93-286256c0d38b");
            Load l2 = n.getLoad("25ab1b5b-6803-47e2-805a-ab7b2072e034");
            Load l3 = n.getLoad("b1e03a8f-6a11-4454-af58-4a4a680e857f");
            assertTrue(l1.getModel().isEmpty());
            assertTrue(l2.getModel().isEmpty());
            assertTrue(l3.getModel().isPresent());
            LoadModel loadModel = l3.getModel().orElseThrow();
            assertEquals(LoadModelType.ZIP, loadModel.getType());
            ZipLoadModel zipLoadModel = (ZipLoadModel) loadModel;
            assertEquals(0.8, zipLoadModel.getC0p());
            assertEquals(0.2, zipLoadModel.getC1p());
            assertEquals(0, zipLoadModel.getC2p());
            assertEquals(0.7, zipLoadModel.getC0q());
            assertEquals(0.3, zipLoadModel.getC1q());
            assertEquals(0, zipLoadModel.getC2q());
        }
    }
}
