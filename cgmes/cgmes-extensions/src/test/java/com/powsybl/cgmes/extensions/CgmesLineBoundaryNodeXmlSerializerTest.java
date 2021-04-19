/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesLineBoundaryNodeXmlSerializerTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        network.getLine("NHV1_NHV2_1").newExtension(CgmesLineBoundaryNodeAdder.class)
                .setHvdc(true)
                .setLineEnergyIdentificationCodeEic("EIC_CODE")
                .add();
        roundTripTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead, "/eurostag_cgmes_line_boundary_node.xml");
    }
}
