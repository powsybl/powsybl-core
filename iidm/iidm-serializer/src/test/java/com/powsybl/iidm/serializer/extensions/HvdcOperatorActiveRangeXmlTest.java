/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.serializer.extensions;

import com.powsybl.commons.test.AbstractSerializerTest;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRangeAdder;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.serializer.NetworkSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serializer.AbstractIidmSerializerTest.getVersionedNetworkPath;
import static com.powsybl.iidm.serializer.IidmSerializerConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Paul Bui-Quang {@literal <paul.buiquang at rte-france.com>}
 */
class HvdcOperatorActiveRangeXmlTest extends AbstractSerializerTest {

    @Test
    void test() throws IOException {
        Network network = HvdcTestNetwork.createLcc();

        HvdcLine hvdcLine = network.getHvdcLine("L");
        HvdcOperatorActivePowerRange extension = hvdcLine.newExtension(HvdcOperatorActivePowerRangeAdder.class).withOprFromCS1toCS2(1.0f).withOprFromCS2toCS1(2.0f).add();

        Network network2 = roundTripXmlTest(network,
                NetworkSerializer::writeAndValidate,
                NetworkSerializer::read,
                getVersionedNetworkPath("hvdcOperatorActiveRangeRef.xml", CURRENT_IIDM_XML_VERSION));

        HvdcLine hvdcLine2 = network2.getHvdcLine("L");
        assertEquals(hvdcLine2.getExtension(HvdcOperatorActivePowerRange.class), extension);
    }
}
