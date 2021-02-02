/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class CgmesControlAreaXmlSerializerTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2021-02-02T09:27:39.856+01:00"));
        CgmesControlAreaAdder adder = network.newExtension(CgmesControlAreaAdder.class);
        adder.newCgmesControlArea("cgmesControlAreaId", "cgmesControlAreaName",
                "energyIdentCodeEic", 100.0)
                .addTerminal("equipmentId", 1);
        adder.add();

        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead, "/extensions/eurostag_cgmes_control_area.xml");
    }
}
