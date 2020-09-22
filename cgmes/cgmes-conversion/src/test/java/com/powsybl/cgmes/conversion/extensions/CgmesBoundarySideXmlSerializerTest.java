/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesBoundarySideXmlSerializerTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = DanglingLineNetworkFactory.create();
        network.setCaseDate(DateTime.parse("2020-09-22T09:41:46.853+02:00"));
        network.getDanglingLine("DL")
                .newExtension(CgmesBoundarySideAdder.class)
                .setBoundarySide(2)
                .add();
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead, "/extensions/dl_cgmes_boundary_side.xml");
    }
}
