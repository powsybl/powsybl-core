/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.VLTEST;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateVoltageLevelTopologyTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/create-vl-topo-test.xiidm");
    }

    @Test
    public void testComplete() throws IOException {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withBusbarCount(3)
                .withSectionCount(4)
                .withBusbarSectionPrefixId("BBS_TEST")
                .withSwitchPrefixId("SW_TEST")
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        modification.apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/create-vl-topo-test-complete.xiidm");
    }

    @Test
    public void testWithNullSwitchKind() {
        CreateVoltageLevelTopologyBuilder builder = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, null, SwitchKind.DISCONNECTOR);
        PowsyblException e = assertThrows(PowsyblException.class, builder::build);
        assertEquals("All switch kinds must be defined", e.getMessage());
    }

    @Test
    public void testWithUnsupportedSwitchKind() {
        CreateVoltageLevelTopologyBuilder builder = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.LOAD_BREAK_SWITCH, SwitchKind.DISCONNECTOR);
        PowsyblException e = assertThrows(PowsyblException.class, builder::build);
        assertEquals("Switch kinds must be DISCONNECTOR or BREAKER", e.getMessage());
    }

    @Test
    public void testWithNegativeCount() {
        CreateVoltageLevelTopologyBuilder modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withBusbarCount(-1)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR);
        PowsyblException e = assertThrows(PowsyblException.class, modification::build);
        assertEquals("busBar count must be >= 0", e.getMessage());
    }

    @Test
    public void testWithUnexpectedSwitchKindsSize() {
        CreateVoltageLevelTopologyBuilder modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER);
        PowsyblException e = assertThrows(PowsyblException.class, modification::build);
        assertEquals("Unexpected switch kinds count (1). Should be 3", e.getMessage());
    }

    @Test
    public void testWithNotExistingVoltageLevel() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("NOT_EXISTING")
                .withBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        assertEquals("Voltage level NOT_EXISTING is not found", e.getMessage());
    }

    @Test
    public void testWithBusBreakerVoltageLevel() {
        Network network = EurostagTutorialExample1Factory.create();
        CreateVoltageLevelTopology modification = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId("VLHV1")
                .withBusbarCount(3)
                .withSectionCount(4)
                .withSwitchKinds(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR, SwitchKind.DISCONNECTOR)
                .build();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modification.apply(network, true, Reporter.NO_OP));
        assertEquals("Voltage Level VLHV1 has an unsupported topology BUS_BREAKER. Should be NODE_BREAKER", e.getMessage());
    }
}
