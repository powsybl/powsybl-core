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
class CgmesBoundaryNodeSerDeTest extends AbstractCgmesExtensionTest {

    @Test
    void testTieLine() throws IOException {
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

    @Test
    void testDanglingLine() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        var tl = network.getTieLine("NHV1_NHV2_1");
        tl.getDanglingLine1().newExtension(CgmesDanglingLineBoundaryNodeAdder.class)
                .setHvdc(false)
                .setLineEnergyIdentificationCodeEic("EIC_CODE")
                .add();
        tl.getDanglingLine2().newExtension(CgmesDanglingLineBoundaryNodeAdder.class)
                .setHvdc(false)
                .add();
        tl.remove();

        allFormatsRoundTripTest(network, "/eurostag_cgmes_dangling_line_boundary_node.xml");
    }
}
