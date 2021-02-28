/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CimCharacteristicsXmlSerializerTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create().setCaseDate(DateTime.parse("2020-09-08T14:28:13.738+02:00"));
        network.newExtension(CimCharacteristicsAdder.class)
                .setTopologyKind(CgmesTopologyKind.BUS_BRANCH)
                .setCimVersion(14)
                .add();
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag_cim_characteristics.xml");
    }
}
