/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class CgmesGLImportPostProcessorTest extends CgmesGLModelTest {

    @Test
    void process() {
        Network network = GLTestUtils.getNetwork();
        new CgmesGLImportPostProcessor(queryCatalog).process(network, tripleStore);
        GLTestUtils.checkNetwork(network);
    }

}
