package com.powsybl.iidm.network.impl;

//import com.powsybl.commons.PowsyblException;
//import com.powsybl.commons.ref.RefChain;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class DcTopologyModelTest {

    @Test
    void testDisconnectedNodes() {
        Network network = network2Nodes();
        // disconnected nodes do not form buses
        assertEquals(0, network.getDcBusCount());
    }

    @Test
    void testSingleLine() {
        Network network = network2Nodes();
        network.newDcLine().setId("dcLine")
                .setR(1.1)
                .setDcNode1("n1")
                .setDcNode2("n2")
                .add();

        final String bus1Name = "n1_dcBus";
        final String bus2Name = "n2_dcBus";
        List<String> refBusNames = List.of(bus1Name, bus2Name);
        assertExpected(refBusNames, network);

        // Test helper routines
        DcTopologyModel topo = ((AbstractNetwork) network).getDcTopologyModel();
        assertEquals(bus1Name, topo.getDcBusOfDcNode("n1").getId());
        assertEquals(bus2Name, topo.getDcBusOfDcNode("n2").getId());
        assertEquals(bus1Name, topo.getDcBus(bus1Name).getId());
    }

    @Test
    void test3Lines() {
        Network network = network4Nodes();
        network.newDcLine().setId("dcLine12")
                .setR(1.2)
                .setDcNode1("n1")
                .setDcNode2("n2")
                .add();

        network.newDcLine().setId("dcLine23")
                .setR(2.3)
                .setDcNode1("n2")
                .setDcNode2("n3")
                .add();

        network.newDcLine().setId("dcLine34")
                .setR(3.4)
                .setDcNode1("n3")
                .setDcNode2("n4")
                .add();

        List<String> refBusNames = List.of("n1_dcBus", "n2_dcBus", "n3_dcBus", "n4_dcBus");
        assertExpected(refBusNames, network);
    }

    @Test
    void testDisConnectedLines() {
        Network network = network4Nodes();

        network.newDcLine().setId("dcLine12")
                .setR(1.2)
                .setDcNode1("n1")
                .setDcNode2("n2")
                .add();

        network.newDcLine().setId("dcLine34")
                .setR(3.4)
                .setDcNode1("n3")
                .setDcNode2("n4")
                .add();

        List<String> refBusNames = List.of("n1_dcBus", "n2_dcBus", "n3_dcBus", "n4_dcBus");
        assertExpected(refBusNames, network);
    }

    @Test
    void testOpenSwitch() {

        Network network = network4Nodes();

        network.newDcLine().setId("dcLine12")
                .setR(1.2)
                .setDcNode1("n1")
                .setDcNode2("n2")
                .add();

        network.newDcLine().setId("dcLine34")
                .setR(3.4)
                .setDcNode1("n3")
                .setDcNode2("n4")
                .add();

        network.newDcSwitch().setId("dcSwitch12")
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setOpen(true)
                .setDcNode1("n2")
                .setDcNode2("n3")
                .add();

        List<String> refBusNames = List.of("n1_dcBus", "n2_dcBus", "n3_dcBus", "n4_dcBus");
        assertExpected(refBusNames, network);

    }

    @Test
    void testClosedSwitchNoResistance() {

        Network network = network4Nodes();

        network.newDcLine().setId("dcLine12")
                .setR(1.2)
                .setDcNode1("n1")
                .setDcNode2("n2")
                .add();

        network.newDcLine().setId("dcLine34")
                .setR(3.4)
                .setDcNode1("n3")
                .setDcNode2("n4")
                .add();

        network.newDcSwitch().setId("dcSwitch12")
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setOpen(false)
                .setDcNode1("n2")
                .setDcNode2("n3")
                .add();

        // Sanity check
        assertEquals(0.0, network.getDcSwitch("dcSwitch12").getR());

        // No full check because there is no guarantee which nodes are selected to create the
        // buses, so bus names are not guaranteed either.
        assertEquals(3, network.getDcBusCount());

    }

    @Test
    void testClosedSwitchResistance() {
        // When DcSwitch resistance != 0, DC buses are differentiated even when
        // the switch is closed
        Network network = network4Nodes();

        network.newDcLine().setId("dcLine12")
                .setR(1.2)
                .setDcNode1("n1")
                .setDcNode2("n2")
                .add();

        network.newDcLine().setId("dcLine34")
                .setR(3.4)
                .setDcNode1("n3")
                .setDcNode2("n4")
                .add();

        network.newDcSwitch().setId("dcSwitch12")
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setOpen(false)
                .setDcNode1("n2")
                .setDcNode2("n3")
                .setR(0.9)
                .add();

        List<String> refBusNames = List.of("n1_dcBus", "n2_dcBus", "n3_dcBus", "n4_dcBus");
        assertExpected(refBusNames, network);
    }

    @Test
    void testHotChangeTopology() {
        // Test that the topology changes on the fly when the network is modified
        Network network = network4Nodes();

        network.newDcLine().setId("dcLine12")
                .setR(1.2)
                .setDcNode1("n1")
                .setDcNode2("n2")
                .add();

        network.newDcLine().setId("dcLine34")
                .setR(3.4)
                .setDcNode1("n3")
                .setDcNode2("n4")
                .add();

        network.newDcSwitch().setId("dcSwitch12")
                .setKind(DcSwitchKind.DISCONNECTOR)
                .setOpen(true)
                .setDcNode1("n2")
                .setDcNode2("n3")
                .setR(0.0)
                .add();

        List<String> refBusNames = List.of("n1_dcBus", "n2_dcBus", "n3_dcBus", "n4_dcBus");
        assertExpected(refBusNames, network);

        network.getDcSwitch("dcSwitch12").setOpen(false);
        assertEquals(3, network.getDcBusCount());

        network.getDcSwitch("dcSwitch12").setR(0.8);
        assertExpected(refBusNames, network);

        network.getDcSwitch("dcSwitch12").setR(0.0);
        assertEquals(3, network.getDcBusCount());

        network.getDcLine("dcLine12").remove();
        assertEquals(2, network.getDcBusCount());

        network.getDcNode("n1").remove();
        assertEquals(2, network.getDcBusCount());
    }

    static Network network2Nodes() {
        Network network = Network.create("id", "test");
        network.newDcNode().setId("n1").setNominalV(500.).add();
        network.newDcNode().setId("n2").setNominalV(500.).add();
        return network;
    }

    static Network network4Nodes() {
        Network network = Network.create("id", "test");
        network.newDcNode().setId("n1").setNominalV(500.).add();
        network.newDcNode().setId("n2").setNominalV(500.).add();
        network.newDcNode().setId("n3").setNominalV(500.).add();
        network.newDcNode().setId("n4").setNominalV(500.).add();
        return network;
    }

    static void assertExpected(List<String> busNamesList, Network network) {
        assertEquals(busNamesList.size(), network.getDcBusCount());
        assertEquals(busNamesList, network.getDcBusStream().map(Object::toString).toList());
        List<String> toCompare = StreamSupport.stream(network.getDcBuses().spliterator(), false).map(Object::toString).toList();
        assertEquals(busNamesList, toCompare);

    }

}
