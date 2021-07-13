/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.impl.extensions.BranchObservabilityImpl;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionedNetworkPath;
import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BranchObservabilityXmlTest extends AbstractConverterTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() throws IOException {
        Network network = BatteryNetworkFactory.create();
        Line line1 = network.getLine("NHV1_NHV2_1");
        assertNotNull(line1);

        BranchObservability<Line> branchObservability = new BranchObservabilityImpl<>(line1, true,
                0.03f, false,
                0.6f, false,
                0.1f, false,
                0.04f, true,
                0.61f, true,
                0.11f, true);
        line1.addExtension(BranchObservability.class, branchObservability);

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("/branchObservabilityRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));

        Line line2 = network2.getLine("NHV1_NHV2_1");
        assertNotNull(line2);
        BranchObservability<Line> branchObservability2 = line2.getExtension(BranchObservability.class);
        assertNotNull(branchObservability2);

        assertEquals(branchObservability.isObservable(), branchObservability2.isObservable());
        assertEquals(branchObservability.getStandardDeviationP(Branch.Side.ONE), branchObservability2.getStandardDeviationP(Branch.Side.ONE), 0.0f);
        assertEquals(branchObservability.isRedundantP(Branch.Side.ONE), branchObservability2.isRedundantP(Branch.Side.ONE));
        assertEquals(branchObservability.getStandardDeviationP(Branch.Side.TWO), branchObservability2.getStandardDeviationP(Branch.Side.TWO), 0.0f);
        assertEquals(branchObservability.isRedundantP(Branch.Side.TWO), branchObservability2.isRedundantP(Branch.Side.TWO));

        assertEquals(branchObservability.getStandardDeviationQ(Branch.Side.ONE), branchObservability2.getStandardDeviationQ(Branch.Side.ONE), 0.0f);
        assertEquals(branchObservability.isRedundantQ(Branch.Side.ONE), branchObservability2.isRedundantQ(Branch.Side.ONE));
        assertEquals(branchObservability.getStandardDeviationQ(Branch.Side.TWO), branchObservability2.getStandardDeviationQ(Branch.Side.TWO), 0.0f);
        assertEquals(branchObservability.isRedundantQ(Branch.Side.TWO), branchObservability2.isRedundantQ(Branch.Side.TWO));

        assertEquals(branchObservability.getStandardDeviationV(Branch.Side.ONE), branchObservability2.getStandardDeviationV(Branch.Side.ONE), 0.0f);
        assertEquals(branchObservability.isRedundantV(Branch.Side.ONE), branchObservability2.isRedundantV(Branch.Side.ONE));
        assertEquals(branchObservability.getStandardDeviationV(Branch.Side.TWO), branchObservability2.getStandardDeviationV(Branch.Side.TWO), 0.0f);
        assertEquals(branchObservability.isRedundantV(Branch.Side.TWO), branchObservability2.isRedundantV(Branch.Side.TWO));

        assertEquals(branchObservability.getName(), branchObservability2.getName());
    }

    @Test
    public void invalidTest() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Unexpected element: qualityZ");

        NetworkXml.read(getClass().getResourceAsStream(getVersionedNetworkPath("/branchObservabilityRoundTripRefInvalid.xml", CURRENT_IIDM_XML_VERSION)));
    }
}
