/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conformity.test;

import com.powsybl.cgmes.conformity.CgmesConformity2Catalog;
import com.powsybl.cgmes.model.test.CgmesModelTester;
import org.junit.jupiter.api.Test;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesConformity2Test {

    @Test
    void microGridType2Assembled() {
        new CgmesModelTester(CgmesConformity2Catalog.microGridType2Assembled()).test();
    }
}
