/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesLineBoundaryNodeSerDeTest extends AbstractCgmesExtensionTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        network.getTieLine("NHV1_NHV2_1").newExtension(CgmesLineBoundaryNodeAdder.class)
                .setHvdc(true)
                .setLineEnergyIdentificationCodeEic("EIC_CODE")
                .add();
        network.getTieLine("NHV1_NHV2_2").newExtension(CgmesLineBoundaryNodeAdder.class)
                .setHvdc(true)
                .add();
        allFormatsRoundTripTest(network, "/eurostag_cgmes_line_boundary_node.xml");
    }
}
