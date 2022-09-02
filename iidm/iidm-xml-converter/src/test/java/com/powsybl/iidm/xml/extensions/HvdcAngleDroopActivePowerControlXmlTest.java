/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionedNetworkPath;
import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public class HvdcAngleDroopActivePowerControlXmlTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = HvdcTestNetwork.createLcc();

        HvdcLine hvdcLine = network.getHvdcLine("L");
        HvdcAngleDroopActivePowerControl extension = hvdcLine.newExtension(HvdcAngleDroopActivePowerControlAdder.class).withEnabled(true).withDroop(0.1f).withP0(200).add();

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("hvdcAngleDroopActivePowerControlRef.xml", CURRENT_IIDM_XML_VERSION));

        HvdcLine hvdcLine2 = network2.getHvdcLine("L");
        assertEquals(hvdcLine2.getExtension(HvdcAngleDroopActivePowerControl.class), extension);
    }
}
