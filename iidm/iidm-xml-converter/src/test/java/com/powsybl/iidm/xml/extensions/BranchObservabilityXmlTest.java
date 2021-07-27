/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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

        BranchObservability<Line> line1BranchObservability = new BranchObservabilityImpl<>(line1, true,
                0.03d, false,
                0.6d, false,
                0.1d, false,
                0.04d, true);
        line1.addExtension(BranchObservability.class, line1BranchObservability);

        Line line2 = network.getLine("NHV1_NHV2_2");
        assertNotNull(line2);

        BranchObservability<Line> line2BranchObservability = new BranchObservabilityImpl<>(line2, false,
                0.1d, true,
                0.2d, true,
                0.3d, true,
                0.4d, false);
        line2.addExtension(BranchObservability.class, line2BranchObservability);

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("/branchObservabilityRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));

        line1 = network2.getLine("NHV1_NHV2_1");
        assertNotNull(line1);
        BranchObservability<Line> line1BranchObservability2 = line1.getExtension(BranchObservability.class);
        assertNotNull(line1BranchObservability2);

        assertEquals(line1BranchObservability.isObservable(), line1BranchObservability2.isObservable());
        assertEquals(line1BranchObservability.getQualityP1().getStandardDeviation(), line1BranchObservability2.getQualityP1().getStandardDeviation(), 0.0d);
        assertEquals(line1BranchObservability.getQualityP1().isRedundant(), line1BranchObservability2.getQualityP1().isRedundant());
        assertEquals(line1BranchObservability.getQualityP2().getStandardDeviation(), line1BranchObservability2.getQualityP2().getStandardDeviation(), 0.0d);
        assertEquals(line1BranchObservability.getQualityP2().isRedundant(), line1BranchObservability2.getQualityP2().isRedundant());

        assertEquals(line1BranchObservability.getQualityQ1().getStandardDeviation(), line1BranchObservability2.getQualityQ1().getStandardDeviation(), 0.0d);
        assertEquals(line1BranchObservability.getQualityQ1().isRedundant(), line1BranchObservability2.getQualityQ1().isRedundant());
        assertEquals(line1BranchObservability.getQualityQ2().getStandardDeviation(), line1BranchObservability2.getQualityQ2().getStandardDeviation(), 0.0d);
        assertEquals(line1BranchObservability.getQualityQ2().isRedundant(), line1BranchObservability2.getQualityQ2().isRedundant());

        assertEquals(line1BranchObservability.getName(), line1BranchObservability2.getName());

        line2 = network2.getLine("NHV1_NHV2_2");
        assertNotNull(line2);
        BranchObservability<Line> line2BranchObservability2 = line2.getExtension(BranchObservability.class);
        assertNotNull(line2BranchObservability2);
        assertEquals(line2BranchObservability.isObservable(), line2BranchObservability2.isObservable());
    }

    @Test
    public void invalidTest() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Unexpected element: qualityV");

        NetworkXml.read(getClass().getResourceAsStream(getVersionedNetworkPath("/branchObservabilityRoundTripRefInvalid.xml", CURRENT_IIDM_XML_VERSION)));
    }
}
