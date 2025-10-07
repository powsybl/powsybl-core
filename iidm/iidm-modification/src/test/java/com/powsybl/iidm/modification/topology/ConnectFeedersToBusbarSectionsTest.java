/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNetworkWithForkFeeder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class ConnectFeedersToBusbarSectionsTest extends AbstractModificationTest {

    @Test
    void testConnectFeedersToBusbarSectionsWithCouplingDevice() throws IOException {
        Network network = Network.read("testNetworkNodeBreakerWithCouplingDevices.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithCouplingDevices.xiidm"));

        NetworkModification modification = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(c -> !(c instanceof BusbarSection)).toList())
                .withBusbarSectionsToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(BusbarSection.class::isInstance).map(c -> (BusbarSection) c).toList())
                .withConnectCouplingDevices(true)
                .build();

        modification.apply(network);
        writeXmlTest(network, "/testNetworkNodeBreakerWithConnectFeedersToBusbarSectionsWithCouplingDevices.xiidm");

        // Check that nothing changes if we apply the modification a second time
        modification.apply(network);
        writeXmlTest(network, "/testNetworkNodeBreakerWithConnectFeedersToBusbarSectionsWithCouplingDevices.xiidm");
    }

    @Test
    void testConnectFeedersToBusbarSectionsWithoutCouplingDevicesAndOnlyLoads() throws IOException {
        Network network = Network.read("testNetworkNodeBreakerWithCouplingDevices.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithCouplingDevices.xiidm"));

        NetworkModification modification = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(Load.class::isInstance).toList())
                .withBusbarSectionsToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(BusbarSection.class::isInstance).map(c -> (BusbarSection) c).toList())
                .withConnectCouplingDevices(false)
                .build();

        modification.apply(network);
        writeXmlTest(network, "/testNetwork3BusbarSectionsWithCouplingDeviceAndConnectedLoads.xiidm");

        // Check that nothing changes if we apply the modification a second time
        modification.apply(network);
        writeXmlTest(network, "/testNetwork3BusbarSectionsWithCouplingDeviceAndConnectedLoads.xiidm");
    }

    @Test
    void testConnectFeedersToBusbarSectionsWithoutCouplingDevicesAndThreeWindingsTransformersOnFirstSection() {
        Network network = Network.read("testNetworkNodeBreakerWithCouplingDevices.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithCouplingDevices.xiidm"));

        long disconnectorCountBeforeModification = network.getVoltageLevel("vl1").getNodeBreakerView().getSwitchStream().filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR).count();

        // List of busbar sections on the first section
        List<BusbarSection> bbsToConnect = network.getVoltageLevel("vl1").getConnectableStream()
                .filter(BusbarSection.class::isInstance)
                .map(c -> (BusbarSection) c)
                .filter(bbs -> bbs.getExtension(BusbarSectionPosition.class).getSectionIndex() == 1)
                .toList();

        NetworkModification modification = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(ThreeWindingsTransformer.class::isInstance).toList())
                .withBusbarSectionsToConnect(bbsToConnect)
                .withConnectCouplingDevices(false)
                .build();

        modification.apply(network);
        long disconnectorCountAfterModification = network.getVoltageLevel("vl1").getNodeBreakerView().getSwitchStream().filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR).count();
        assertEquals(disconnectorCountBeforeModification + 2, disconnectorCountAfterModification);

        // Check that nothing changes if we apply the modification a second time
        modification.apply(network);
        long disconnectorCountAfterReaplyingModification = network.getVoltageLevel("vl1").getNodeBreakerView().getSwitchStream().filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR).count();
        assertEquals(disconnectorCountAfterModification, disconnectorCountAfterReaplyingModification);
    }

    @Test
    void testConnectFeedersToBusbarSectionsWithoutCouplingDevicesAndThreeWindingsTransformersOnFirstBusbarSection() {
        Network network = Network.read("testNetworkNodeBreakerWithCouplingDevices.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithCouplingDevices.xiidm"));

        long disconnectorCountBeforeModification = network.getVoltageLevel("vl1").getNodeBreakerView().getSwitchStream().filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR).count();

        // List of busbar sections on the first section
        List<BusbarSection> bbsToConnect = network.getVoltageLevel("vl1").getConnectableStream()
                .filter(BusbarSection.class::isInstance)
                .map(c -> (BusbarSection) c)
                .filter(bbs -> bbs.getExtension(BusbarSectionPosition.class).getBusbarIndex() == 1)
                .toList();

        NetworkModification modification = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(ThreeWindingsTransformer.class::isInstance).toList())
                .withBusbarSectionsToConnect(bbsToConnect)
                .withConnectCouplingDevices(false)
                .build();

        modification.apply(network);
        long disconnectorCountAfterModification = network.getVoltageLevel("vl1").getNodeBreakerView().getSwitchStream().filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR).count();
        assertEquals(disconnectorCountBeforeModification + 1, disconnectorCountAfterModification);

        // Check that nothing changes if we apply the modification a second time
        modification.apply(network);
        long disconnectorCountAfterReaplyingModification = network.getVoltageLevel("vl1").getNodeBreakerView().getSwitchStream().filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR).count();
        assertEquals(disconnectorCountAfterModification, disconnectorCountAfterReaplyingModification);
    }

    @Test
    void testHasImpactOnNetwork() {
        Network network = Network.read("testNetworkNodeBreakerWithCouplingDevices.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreakerWithCouplingDevices.xiidm"));

        NetworkModification modification = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(Load.class::isInstance).toList())
                .withBusbarSectionsToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(BusbarSection.class::isInstance).map(c -> (BusbarSection) c).toList())
                .withConnectCouplingDevices(false)
                .build();

        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));
    }

    @Test
    void testThrowExceptionAndHasImpactOnNetwork() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

        // No busbar section
        NetworkModification modificationWithoutBbs = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(c -> !(c instanceof BusbarSection)).toList())
                .withBusbarSectionsToConnect(Collections.emptyList())
                .withConnectCouplingDevices(true)
                .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modificationWithoutBbs.hasImpactOnNetwork(network));
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> modificationWithoutBbs.apply(network, true, ReportNode.NO_OP));
        assertEquals("No busbar section provided.", e1.getMessage());

        // Wrong network
        Network network1 = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        NetworkModification modificationWrongNetwork = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(c -> !(c instanceof BusbarSection)).toList())
                .withBusbarSectionsToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(BusbarSection.class::isInstance).map(c -> (BusbarSection) c).toList())
                .withConnectCouplingDevices(true)
                .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modificationWrongNetwork.hasImpactOnNetwork(network1));
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> modificationWrongNetwork.apply(network1, true, ReportNode.NO_OP));
        assertEquals("All busbar sections must be in the network passed to the method.", e2.getMessage());

        // Voltage level with no BusbarSectionPosition extension
        Network network2 = FourSubstationsNodeBreakerFactory.create();
        NetworkModification modificationNetworkWithNoBusbarSectionPosition = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network2.getVoltageLevel("S1VL2").getConnectableStream().filter(c -> !(c instanceof BusbarSection)).toList())
                .withBusbarSectionsToConnect(network2.getVoltageLevel("S1VL2").getConnectableStream().filter(BusbarSection.class::isInstance).map(c -> (BusbarSection) c).toList())
                .withConnectCouplingDevices(true)
                .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modificationNetworkWithNoBusbarSectionPosition.hasImpactOnNetwork(network));
        PowsyblException e3 = assertThrows(PowsyblException.class, () -> modificationNetworkWithNoBusbarSectionPosition.apply(network2, true, ReportNode.NO_OP));
        assertEquals("All busbar sections must have a BusbarSectionPosition extension.", e3.getMessage());

        // Busbar sections do not belong to the same voltage level
        NetworkModification modificationWithBbsInDifferentVl = new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(network.getVoltageLevel("vl1").getConnectableStream().filter(c -> !(c instanceof BusbarSection)).toList())
                .withBusbarSectionsToConnect(network.getBusbarSectionStream().toList())
                .withConnectCouplingDevices(true)
                .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modificationWithBbsInDifferentVl.hasImpactOnNetwork(network));
        PowsyblException e4 = assertThrows(PowsyblException.class, () -> modificationWithBbsInDifferentVl.apply(network, true, ReportNode.NO_OP));
        assertEquals("All busbar sections must all belong to the same voltage level.", e4.getMessage());
    }

    @Test
    void testConnectFeedersToBusbarSectionsWithFork() {
        Network network = createNetworkWithForkAndExtensions();
        VoltageLevel vl = network.getVoltageLevel("VL");
        assertEquals(1, vl.getNodeBreakerView().getSwitchStream().filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR).count());

        // Connect the fork to BBS1 using all the connectables
        new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(vl.getConnectableStream().filter(c -> !(c instanceof BusbarSection)).toList())
                .withBusbarSectionsToConnect(Collections.singletonList(network.getBusbarSection("BBS1")))
                .build()
                .apply(network);

        assertEquals(2, network.getSwitchStream().filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR).count());

        // Connect the fork to BBS1 using only the load
        Network network1 = createNetworkWithForkAndExtensions();
        new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(Collections.singletonList(network1.getLoad("LD")))
                .withBusbarSectionsToConnect(Collections.singletonList(network1.getBusbarSection("BBS1")))
                .build()
                .apply(network1);

        assertEquals(2, network1.getSwitchStream().filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR).count());

        // Connect the fork to BBS1 using only the generator
        Network network2 = createNetworkWithForkAndExtensions();
        new ConnectFeedersToBusbarSectionsBuilder()
                .withConnectablesToConnect(Collections.singletonList(network2.getGenerator("G")))
                .withBusbarSectionsToConnect(Collections.singletonList(network2.getBusbarSection("BBS1")))
                .build()
                .apply(network2);

        assertEquals(2, network2.getSwitchStream().filter(sw -> sw.getKind() == SwitchKind.DISCONNECTOR).count());
    }

    private Network createNetworkWithForkAndExtensions() {
        Network network = createNetworkWithForkFeeder();

        VoltageLevel vl = network.getVoltageLevel("VL");

        // Add a new busbar section
        int firstAvailableNode = vl.getNodeBreakerView().getMaximumNodeIndex() + 1;
        vl.getNodeBreakerView().newBusbarSection().setId("BBS1").setNode(firstAvailableNode).add();

        // Add the BusbarSectionPosition extension to the busbar sections
        network.getBusbarSection("BBS").newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(1).add();
        network.getBusbarSection("BBS1").newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(2).withSectionIndex(1).add();
        return network;
    }
}
