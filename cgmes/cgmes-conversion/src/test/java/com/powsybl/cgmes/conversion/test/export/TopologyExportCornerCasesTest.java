package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.ZipDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class TopologyExportCornerCasesTest extends AbstractSerDeTest {

    @Test
    void testExportSwitchesBusBreaker() {
        test(createSwitchesBBNetwork(), true, true,
                new String[] {"voltageLevel1_0", "voltageLevel1_1", "voltageLevel1_2", "voltageLevel1_3", "voltageLevel1_4"});
    }

    @Test
    void testExportParallelSwitchesNodeBreaker() {
        test(createParallelSwitchesNBNetwork(), true, true,
                new String[] {"voltageLevel1_0"});
    }

    @Test
    void testExportSwitchesNodeBreaker() {
        test(createSwitchesNBNetwork(), true, true,
                new String[] {"voltageLevel1_0", "voltageLevel1_2", "voltageLevel1_8"});
    }

    @Test
    void testExportGeneratorDisconnectedTransformerBusBreaker() {
        // The calculated BusView from bus-breaker iidm and from node-breaker iidm is different
        // The condition for a valid bus in the BusView for bus-breaker and node-breaker is slightly different
        // So we end up with different bus-view buses
        test(createGeneratorDisconnectedTransformerBBNetwork(), false, false,
                new String[] {"voltageLevel1_0", "voltageLevel2_0", "voltageLevel2_1"});
    }

    @Test
    void testExportGeneratorTransformerNodeBreaker() {
        test(createGeneratorTransformerNBNetwork(), true, true,
                new String[] {"voltageLevel1_0", "voltageLevel2_0"});
    }

    @Disabled("Mismatch in bus view bus definition from node/breaker and bus/breaker topologies")
    // FIXME(Luma): consider adding busbar section to exported EQ when we save a bus/breaker topology as node/breaker
    @Test
    void testExportDisconnectedLoadBusBreaker() {
        test(createDisconnectedLoadBBNetwork(), false, true,
                new String[] {"voltageLevel1_0", "voltageLevel1_1"});
    }

    @Test
    void testExportDisconnectedLoadNodeBreaker() {
        test(createDisconnectedLoadNBNetwork(), false, true,
                new String[] {"voltageLevel1_0", "voltageLevel1_1", "voltageLevel1_3"});
    }

    private void test(Network network,
                      boolean checkAllTerminalsConnected,
                      boolean checkSameNumberOfBusViewBuses,
                      String[] expectedBusBreakerViewBuses) {
        String name = network.getId();

        // Some terminals may show as disconnected even if everything is connected but the bus is not valid
        // We perform the check to verify that the networks where we want everything connected have valid buses
        // and do not introduce additional noise in the validation
        if (checkAllTerminalsConnected) {
            checkAllTerminalsConnected(network, name);
        }

        // Export as CGMES 3
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");
        ZipDataSource zip = new ZipDataSource(tmpDir.resolve("."), name, "");
        new CgmesExport().export(network, params, zip);
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network networkFromCgmes = Network.read(tmpDir.resolve(name + ".zip"), LocalComputationManager.getDefault(), ImportConfig.CACHE.get(), importParams);
        if (checkAllTerminalsConnected) {
            checkAllTerminalsConnected(network, name + "_from_CGMES");
        }

        // Original network and re-imported must have the same number of buses in the bus view
        // Additional buses in the bus-breaker view may have been introduced because some terminals disconnected
        if (checkSameNumberOfBusViewBuses) {
            assertEquals(
                    network.getBusView().getBusStream().count(),
                    networkFromCgmes.getBusView().getBusStream().count());
        }

        // And the list of buses should be the expected one
        assertArrayEquals(
                expectedBusBreakerViewBuses,
                networkFromCgmes.getBusBreakerView().getBusStream().map(Bus::getId).sorted().toArray());
    }

    private static void checkAllTerminalsConnected(Network network, String name) {
        for (Connectable<?> c : network.getConnectables()) {
            for (Terminal t : c.getTerminals()) {
                if (!t.isConnected()) {
                    fail("Terminal is disconnected in equipment " + c.getId() + " in network " + name);
                }
            }
        }
    }

    private static Network createGeneratorDisconnectedTransformerBBNetwork() {
        Network network = createBaseNetwork("disconnected_generator_transformer_bb", TopologyKind.BUS_BREAKER);
        VoltageLevel.BusBreakerView topology1 = network.getVoltageLevel("voltageLevel1").getBusBreakerView();
        topology1.newBus()
                .setId("voltageLevel1Bus1")
                .add();
        Substation substation1 = network.getSubstation("substation1");
        VoltageLevel voltageLevel2 = substation1.newVoltageLevel()
                .setId("voltageLevel2")
                .setNominalV(15)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        VoltageLevel.BusBreakerView topology2 = network.getVoltageLevel("voltageLevel2").getBusBreakerView();
        topology2.newBus()
                .setId("voltageLevel2Bus1")
                .add();
        Generator generator1 = createGeneratorAdder(voltageLevel2, "generator1")
                .setConnectableBus("voltageLevel2Bus1")
                .add();
        createReactiveCapabilityCurve(generator1);
        // We already said the generator1 is not connected to its bus by only setting its ConnectableBus, but anyway:
        generator1.getTerminal().disconnect();
        createTwoWindingsTransformerAdder(substation1, "twoWindingsTransformer1", "voltageLevel1", "voltageLevel2")
                .setBus1("voltageLevel1Bus1")
                .setBus2("voltageLevel2Bus1")
                .add();
        return network;
    }

    private static Network createDisconnectedLoadBBNetwork() {
        Network network = createBaseNetwork("disconnected_load_bb", TopologyKind.BUS_BREAKER);

        VoltageLevel.BusBreakerView topology1 = network.getVoltageLevel("voltageLevel1").getBusBreakerView();
        Bus voltageLevel1Bus1 = topology1.newBus()
                .setId("voltageLevel1Bus1")
                .add();
        createLoadAdder(network.getVoltageLevel("voltageLevel1"), "load1")
                .setBus(voltageLevel1Bus1.getId())
                .add();
        Load load2 = createLoadAdder(network.getVoltageLevel("voltageLevel1"), "load2")
                .setConnectableBus(voltageLevel1Bus1.getId())
                .add();
        load2.getTerminal().disconnect();

        return network;
    }

    private static Network createSwitchesBBNetwork() {
        Network network = createBaseNetwork("switches_network_bb", TopologyKind.BUS_BREAKER);

        VoltageLevel.BusBreakerView topology1 = network.getVoltageLevel("voltageLevel1").getBusBreakerView();
        Bus voltageLevel1Bus1 = topology1.newBus()
                .setId("voltageLevel1Bus1")
                .add();
        Bus busSwitch1 = topology1.newBus()
                .setId("BusSwitch1")
                .add();
        topology1.newSwitch()
                .setId("voltageLevel1Switch1")
                .setOpen(false)
                .setBus1(voltageLevel1Bus1.getId())
                .setBus2(busSwitch1.getId())
                .add();
        Bus busSwitch2 = topology1.newBus()
                .setId("BusSwitch2")
                .add();
        topology1.newSwitch()
                .setId("voltageLevel1Switch2")
                .setOpen(false)
                .setBus1(busSwitch1.getId())
                .setBus2(busSwitch2.getId())
                .add();
        Bus busSwitch3 = topology1.newBus()
                .setId("BusSwitch3")
                .add();
        topology1.newSwitch()
                .setId("voltageLevel1Switch3")
                .setOpen(false)
                .setBus1(busSwitch2.getId())
                .setBus2(busSwitch3.getId())
                .add();
        Bus voltageLevel1Bus2 = topology1.newBus()
                .setId("voltageLevel1Bus2")
                .add();
        topology1.newSwitch()
                .setId("voltageLevel1Switch4")
                .setOpen(false)
                .setBus1(busSwitch3.getId())
                .setBus2(voltageLevel1Bus2.getId())
                .add();
        return network;
    }

    private static Network createGeneratorTransformerNBNetwork() {
        Network network = createBaseNetwork("generator_transformer_nb", TopologyKind.NODE_BREAKER);
        VoltageLevel.NodeBreakerView topology1 = network.getVoltageLevel("voltageLevel1").getNodeBreakerView();
        BusbarSection voltageLevel1Bus1 = topology1.newBusbarSection()
                .setId("voltageLevel1Bus1")
                .setNode(0)
                .add();
        Substation substation1 = network.getSubstation("substation1");
        VoltageLevel voltageLevel2 = substation1.newVoltageLevel()
                .setId("voltageLevel2")
                .setNominalV(15)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        TwoWindingsTransformer twoWindingsTransformer1 = createTwoWindingsTransformerAdder(substation1, "twoWindingsTransformer1", "voltageLevel1", "voltageLevel2")
                .setNode1(1)
                .setNode2(1)
                .add();
        topology1.newInternalConnection()
                .setNode1(voltageLevel1Bus1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(twoWindingsTransformer1.getTerminal1().getNodeBreakerView().getNode())
                .add();

        VoltageLevel.NodeBreakerView topology2 = network.getVoltageLevel("voltageLevel2").getNodeBreakerView();
        Generator generator1 = createGeneratorAdder(voltageLevel2, "generator1")
                .setNode(0)
                .add();
        createReactiveCapabilityCurve(generator1);
        topology2.newInternalConnection()
                .setNode1(generator1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(twoWindingsTransformer1.getTerminal2().getNodeBreakerView().getNode())
                .add();

        return network;
    }

    private static Network createDisconnectedLoadNBNetwork() {
        Network network = createBaseNetwork("disconnected_load_nb", TopologyKind.NODE_BREAKER);

        VoltageLevel.NodeBreakerView topology1 = network.getVoltageLevel("voltageLevel1").getNodeBreakerView();
        BusbarSection voltageLevel1Bus1 = topology1.newBusbarSection()
                .setId("voltageLevel1Bus1")
                .setNode(0)
                .add();
        Load load1 = createLoadAdder(network.getVoltageLevel("voltageLevel1"), "load1")
                .setNode(1)
                .add();
        topology1.newInternalConnection()
                .setNode1(load1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1Bus1.getTerminal().getNodeBreakerView().getNode())
                .add();
        Load load2 = createLoadAdder(network.getVoltageLevel("voltageLevel1"), "load2")
                .setNode(2)
                .add();
        topology1.newSwitch()
                .setId("load2Switch")
                .setNode1(load2.getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1Bus1.getTerminal().getNodeBreakerView().getNode())
                .setOpen(true)
                .setKind(SwitchKind.BREAKER)
                .add();

        return network;
    }

    private static Network createParallelSwitchesNBNetwork() {
        Network network = createBaseNetwork("parallel_switches_nb", TopologyKind.NODE_BREAKER);

        // We create a load at node 3 to avoid bbs terminals being considered disconnected.
        // If there are only bbs the calculated bus in the busview is not valid,
        // and the terminals of bbs are considered disconnected.
        // By adding a "feeder" element to the graph the bus is valid and the bbs terminals connected.
        network.getVoltageLevel("voltageLevel1").newLoad()
                .setId("load1")
                .setNode(3)
                .setP0(0)
                .setQ0(0)
                .add();

        VoltageLevel.NodeBreakerView topology1 = network.getVoltageLevel("voltageLevel1").getNodeBreakerView();
        BusbarSection voltageLevel1Bus1 = topology1.newBusbarSection()
                .setId("voltageLevel1Bus1")
                .setNode(0)
                .add();
        BusbarSection voltageLevel1Bus2 = topology1.newBusbarSection()
                .setId("voltageLevel1Bus2")
                .setNode(1)
                .add();
        topology1.newDisconnector()
                .setId("voltageLevel1Switch11")
                .setRetained(false)
                .setOpen(false)
                .setNode1(voltageLevel1Bus1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(2)
                .add();
        topology1.newBreaker()
                .setId("voltageLevel1Switch21")
                .setRetained(true)
                .setOpen(false)
                .setNode1(2)
                .setNode2(3)
                .add();
        topology1.newDisconnector()
                .setId("voltageLevel1Switch31")
                .setRetained(false)
                .setOpen(false)
                .setNode1(3)
                .setNode2(voltageLevel1Bus2.getTerminal().getNodeBreakerView().getNode())
                .add();
        topology1.newDisconnector()
                .setId("voltageLevel1Switch12")
                .setRetained(false)
                .setOpen(false)
                .setNode1(voltageLevel1Bus1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(4)
                .add();
        topology1.newBreaker()
                .setId("voltageLevel1Switch22")
                .setRetained(false)
                .setOpen(false)
                .setNode1(4)
                .setNode2(5)
                .add();
        topology1.newDisconnector()
                .setId("voltageLevel1Switch32")
                .setRetained(false)
                .setOpen(false)
                .setNode1(5)
                .setNode2(voltageLevel1Bus2.getTerminal().getNodeBreakerView().getNode())
                .add();

        return network;
    }

    private static Network createSwitchesNBNetwork() {
        Network network = createBaseNetwork("switches_nb", TopologyKind.NODE_BREAKER);

        VoltageLevel.NodeBreakerView topology1 = network.getVoltageLevel("voltageLevel1").getNodeBreakerView();

        // We create a load at node 3 to avoid bbs terminals being considered disconnected.
        // If there are only bbs the calculated bus in the busview is not valid,
        // and the terminals of bbs are considered disconnected.
        // By adding a "feeder" element to the graph the bus is valid and the bbs terminals connected.
        network.getVoltageLevel("voltageLevel1").newLoad()
                .setId("load1")
                .setNode(3)
                .setP0(0)
                .setQ0(0)
                .add();

        BusbarSection voltageLevel1Bus1 = topology1.newBusbarSection()
                .setId("voltageLevel1Bus1")
                .setNode(0)
                .add();
        BusbarSection voltageLevel1Bus2 = topology1.newBusbarSection()
                .setId("voltageLevel1Bus2")
                .setNode(1)
                .add();
        topology1.newDisconnector()
                .setId("voltageLevel1Switch1")
                .setRetained(false)
                .setOpen(false)
                .setNode1(voltageLevel1Bus1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(2)
                .add();
        topology1.newBreaker()
                .setId("voltageLevel1Switch2")
                .setRetained(true)
                .setOpen(false)
                .setNode1(2)
                .setNode2(3)
                .add();
        topology1.newDisconnector()
                .setId("voltageLevel1Switch3")
                .setRetained(false)
                .setOpen(false)
                .setNode1(3)
                .setNode2(4)
                .add();
        topology1.newDisconnector()
                .setId("voltageLevel1Switch4")
                .setRetained(false)
                .setOpen(false)
                .setNode1(4)
                .setNode2(5)
                .add();
        topology1.newBreaker()
                .setId("voltageLevel1Switch5")
                .setRetained(false)
                .setOpen(false)
                .setNode1(5)
                .setNode2(6)
                .add();
        topology1.newDisconnector()
                .setId("voltageLevel1Switch6")
                .setRetained(false)
                .setOpen(false)
                .setNode1(6)
                .setNode2(7)
                .add();
        topology1.newDisconnector()
                .setId("voltageLevel1Switch7")
                .setRetained(false)
                .setOpen(false)
                .setNode1(7)
                .setNode2(8)
                .add();
        topology1.newBreaker()
                .setId("voltageLevel1Switch8")
                .setRetained(true)
                .setOpen(false)
                .setNode1(8)
                .setNode2(9)
                .add();
        topology1.newDisconnector()
                .setId("voltageLevel1Switch9")
                .setRetained(false)
                .setOpen(false)
                .setNode1(9)
                .setNode2(voltageLevel1Bus2.getTerminal().getNodeBreakerView().getNode())
                .add();

        return network;
    }

    private static Network createBaseNetwork(String id, TopologyKind topologyKind) {
        Network network = NetworkFactory.findDefault().createNetwork(id, "iidm");

        Substation substation1 = network.newSubstation()
                .setId("substation1")
                .setCountry(Country.FR)
                .setTso("TSO1")
                .setGeographicalTags("region1")
                .add();
        substation1.newVoltageLevel()
                .setId("voltageLevel1")
                .setNominalV(400)
                .setTopologyKind(topologyKind)
                .add();

        return network;
    }

    private static LoadAdder createLoadAdder(VoltageLevel voltageLevel, String name) {
        return voltageLevel.newLoad()
                .setId(name)
                .setP0(10)
                .setQ0(5);
    }

    private static GeneratorAdder createGeneratorAdder(VoltageLevel voltageLevel, String name) {
        return voltageLevel.newGenerator()
                .setId(name)
                .setEnergySource(EnergySource.NUCLEAR)
                .setMinP(200.0)
                .setMaxP(900.0)
                .setVoltageRegulatorOn(true)
                .setTargetP(900.0)
                .setTargetV(380.0);
    }

    private static TwoWindingsTransformerAdder createTwoWindingsTransformerAdder(Substation substation, String name, String voltageLevel1, String voltageLevel2) {
        return substation.newTwoWindingsTransformer()
                .setId(name)
                .setR(0.10368000715971)
                .setX(8.2943999999999996)
                .setB(-2.0576486349455101e-05)
                .setG(6.8962194745836297e-06)
                .setRatedS(300)
                .setRatedU1(405)
                .setRatedU2(17)
                .setVoltageLevel1(voltageLevel1)
                .setVoltageLevel2(voltageLevel2);
    }

    private static void createReactiveCapabilityCurve(Generator generator) {
        generator.newReactiveCapabilityCurve()
                .beginPoint().setP(200.0).setMinQ(300.0).setMaxQ(500.0).endPoint()
                .beginPoint().setP(900.0).setMinQ(300.0).setMaxQ(500.0).endPoint()
                .add();
    }
}
