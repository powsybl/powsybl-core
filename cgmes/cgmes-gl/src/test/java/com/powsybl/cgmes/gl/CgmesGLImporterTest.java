/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class CgmesGLImporterTest extends AbstractCgmesGLTest {

    private CgmesGLModel cgmesGLModel;

    @BeforeEach
    void setUp() {
        super.setUp();
        cgmesGLModel = Mockito.mock(CgmesGLModel.class);
        Mockito.when(cgmesGLModel.getSubstationsPosition()).thenReturn(substationsPropertyBags);
        Mockito.when(cgmesGLModel.getLinesPositions()).thenReturn(linesPropertyBags);
    }

    @Test
    void test() {
        Network network = GLTestUtils.getNetwork();
        CgmesGLImporter cgmesGLImporter = new CgmesGLImporter(network, cgmesGLModel);
        cgmesGLImporter.importGLData();
        GLTestUtils.checkNetwork(network);
    }

}
