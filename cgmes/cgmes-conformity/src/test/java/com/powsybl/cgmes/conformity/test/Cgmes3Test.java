/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conformity.test;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.model.test.CgmesModelTester;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class Cgmes3Test {

    @Test
    void microGrid() throws IOException {
        new CgmesModelTester(Cgmes3Catalog.microGrid()).test();
    }

    @Test
    void microGridWithoutTp() throws IOException {
        new CgmesModelTester(Cgmes3Catalog.microGridWithoutTpSv()).test();
    }

    @Test
    void miniGrid() throws IOException {
        new CgmesModelTester(Cgmes3Catalog.miniGrid()).test();
    }

    @Test
    void miniGridWithoutTp() throws IOException {
        new CgmesModelTester(Cgmes3Catalog.miniGridWithoutTpSv()).test();
    }

    @Test
    void smallGrid() throws IOException {
        new CgmesModelTester(Cgmes3Catalog.smallGrid()).test();
    }

    @Test
    void smallGridWithoutTp() throws IOException {
        new CgmesModelTester(Cgmes3Catalog.smallGridWithoutTpSv()).test();
    }

    @Test
    void svedala() throws IOException {
        new CgmesModelTester(Cgmes3Catalog.svedala()).test();
    }

    @Test
    void svedalaWithoutTp() throws IOException {
        new CgmesModelTester(Cgmes3Catalog.svedalaWithoutTpSv()).test();
    }
}
