/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.test.ShuntTestCaseFactory;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ShuntCompensatorXmlTest extends AbstractXmlConverterTest {

    @Test
    public void test() throws IOException {
        Network network = ShuntTestCaseFactory.create();
        ShuntCompensator sc = network.getShuntCompensator("SHUNT");
        sc.setProperty("test", "test");
        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("shuntRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripVersionedXmlFromMinToCurrentVersionTest("shuntRoundTripRef.xml", IidmXmlVersion.V_1_2);
    }

    @Test
    public void unsupportedReadTest() {
        testForAllPreviousVersions(IidmXmlVersion.V_1_2, this::read);
    }

    @Test
    public void unsupportedWriteTest() {
        Network network = ShuntTestCaseFactory.create();
        testForAllPreviousVersions(IidmXmlVersion.V_1_2, v -> write(network, v.toString(".")));
    }

    private void read(IidmXmlVersion version) {
        try {
            NetworkXml.read(getVersionedNetworkAsStream("faultyShunt.xml", version));
            fail();
        } catch (PowsyblException e) {
            assertEquals("shunt.regulatingTerminal is not supported for IIDM-XML version " +
                            version.toString(".") + ". IIDM-XML version should be >= 1.2",
                    e.getMessage());
        }
    }

    private void write(Network network, String version) {
        try {
            ExportOptions options = new ExportOptions().setVersion(version);
            NetworkXml.write(network, options, tmpDir.resolve("/fail.xml"));
            fail();
        } catch (PowsyblException e) {
            assertEquals("shunt.voltageRegulatorOn is not defined as default and not supported for IIDM-XML version " +
                            version + ". IIDM-XML version should be >= 1.2",
                    e.getMessage());
        }
    }
}
