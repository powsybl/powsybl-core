/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorXmlTest extends AbstractConverterTest {

    @Test
    public void roundTripTest() throws IOException {
        roundTripXmlTest(SvcTestCaseFactory.create(),
                         NetworkXml::writeAndValidate,
                         NetworkXml::read,
                         "/staticVarCompensatorRoundTripRef.xml");
    }

    @Test
    public void testReadV10() {
        Network n1 = SvcTestCaseFactory.create();
        Network n2 = NetworkXml.read(getClass().getResourceAsStream("/refs_V1_0/staticVarCompensatorRoundTripRef.xml"));
        StaticVarCompensator expected = n1.getStaticVarCompensator("SVC2");
        StaticVarCompensator actual = n2.getStaticVarCompensator("SVC2");
        assertEquals(expected.getVoltageSetpoint(), actual.getVoltageSetpoint(), 0.0);
        assertEquals(expected.getReactivePowerSetpoint(), actual.getReactivePowerSetpoint(), 0.0);
    }
}
