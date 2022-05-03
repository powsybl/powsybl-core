package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ZipFileDataSource;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertArrayEquals;

public class ExportTest extends AbstractConverterTest {

    @Test
    public void testExportSwitchesBusBreaker() throws IOException {
        Network network = createSwitchesBBNetwork();

        String cimZipFilename = "SwitchesBB_CIM100";
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");
        ZipFileDataSource zip = new ZipFileDataSource(tmpDir.resolve("."), cimZipFilename);
        new CgmesExport().export(network, params, zip);
        Network network100 = Importers.loadNetwork(tmpDir.resolve(cimZipFilename + ".zip"));
        String[] expected = {"voltageLevel1_0", "voltageLevel1_1", "voltageLevel1_2", "voltageLevel1_3", "voltageLevel1_4"};
        assertArrayEquals(expected, network100.getBusBreakerView().getBusStream().map(Bus::getId).sorted().toArray());
    }

    @Test
    public void testExportParallelSwitchesNodeBreaker(boolean printPerformance) throws IOException {
        Network network = createParallelSwitchesNBNetwork();

        String cimZipFilename = "ParallelSwitchesNB_CIM100";
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");
        ZipFileDataSource zip = new ZipFileDataSource(tmpDir.resolve("."), cimZipFilename);
        new CgmesExport().export(network, params, zip);
        Network network100 = Importers.loadNetwork(tmpDir.resolve(cimZipFilename + ".zip"));
        String[] expected = {"voltageLevel1_0", "voltageLevel1_6", "voltageLevel1_7"};
        assertArrayEquals(expected, network100.getBusBreakerView().getBusStream().map(Bus::getId).sorted().toArray());
    }

    @Test
    public void testExportSwitchesNodeBreaker() throws IOException {
        Network network = createSwitchesNBNetwork();

        String cimZipFilename = "SwitchesNB_CIM100";
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");
        ZipFileDataSource zip = new ZipFileDataSource(tmpDir.resolve("."), cimZipFilename);
        new CgmesExport().export(network, params, zip);
        Network network100 = Importers.loadNetwork(tmpDir.resolve(cimZipFilename + ".zip"));
        String[] expected = {"voltageLevel1_0", "voltageLevel1_10", "voltageLevel1_11", "voltageLevel1_2", "voltageLevel1_8"};
        assertArrayEquals(expected, network100.getBusBreakerView().getBusStream().map(Bus::getId).sorted().toArray());
    }

    @Test
    public void testExportGeneratorTransformerBusBreaker() throws IOException {
        Network network = createGeneratorTransformerBBNetwork();

        String cimZipFilename = "GeneratorTransformerBB_CIM100";
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");
        ZipFileDataSource zip = new ZipFileDataSource(tmpDir.resolve("."), cimZipFilename);
        new CgmesExport().export(network, params, zip);
        Network network100 = Importers.loadNetwork(tmpDir.resolve(cimZipFilename + ".zip"));
        String[] expected = {"voltageLevel1_0", "voltageLevel2_0", "voltageLevel2_1"};
        assertArrayEquals(expected, network100.getBusBreakerView().getBusStream().map(Bus::getId).sorted().toArray());
    }

    @Test
    public void testExportGeneratorTransformerNodeBreaker() throws IOException {
        Network network = createGeneratorTransformerNodeNetwork();

        String cimZipFilename = "GeneratorTransformerNB_CIM100";
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");
        ZipFileDataSource zip = new ZipFileDataSource(tmpDir.resolve("."), cimZipFilename);
        new CgmesExport().export(network, params, zip);
        Network network100 = Importers.loadNetwork(tmpDir.resolve(cimZipFilename + ".zip"));
        String[] expected = {"voltageLevel1_0", "voltageLevel2_0"};
        assertArrayEquals(expected, network100.getBusBreakerView().getBusStream().map(Bus::getId).sorted().toArray());
    }

    @Test
    public void testExportDisconnectedLoadBusBreaker() throws IOException {
        Network network = createDisconnectedLoadBBNetwork();

        String cimZipFilename = "DisconnectedLoadBB_CIM100";
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");
        ZipFileDataSource zip = new ZipFileDataSource(tmpDir.resolve("."), cimZipFilename);
        new CgmesExport().export(network, params, zip);
        Network network100 = Importers.loadNetwork(tmpDir.resolve(cimZipFilename + ".zip"));
        String[] expected = {"voltageLevel1_0", "voltageLevel1_1"};
        assertArrayEquals(expected, network100.getBusBreakerView().getBusStream().map(Bus::getId).sorted().toArray());
    }

    @Test
    public void testExportDisconnectedLoadNodeBreaker() throws IOException {
        Network network = createDisconnectedLoadNBNetwork();

        String cimZipFilename = "DisconnectedLoadNB_CIM100";
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");
        ZipFileDataSource zip = new ZipFileDataSource(tmpDir.resolve("."), cimZipFilename);
        new CgmesExport().export(network, params, zip);
        Network network100 = Importers.loadNetwork(tmpDir.resolve(cimZipFilename + ".zip"));
        String[] expected = {"voltageLevel1_0", "voltageLevel1_1", "voltageLevel1_3"};
        assertArrayEquals(expected, network100.getBusBreakerView().getBusStream().map(Bus::getId).sorted().toArray());
    }

    private static Network createGeneratorTransformerBBNetwork() {
        Network network = createBaseNetwork("network", "generator_transformer_network", TopologyKind.BUS_BREAKER);
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
        generator1.getTerminal().disconnect();
        createTwoWindingsTransformerAdder(substation1, "twoWindingsTransformer1", "voltageLevel1", "voltageLevel2")
                .setBus1("voltageLevel1Bus1")
                .setBus2("voltageLevel2Bus1")
                .add();
        return network;
    }

    private static Network createDisconnectedLoadBBNetwork() {
        Network network = createBaseNetwork("network", "disconnect_load_network", TopologyKind.BUS_BREAKER);

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
        Network network = createBaseNetwork("network", "switches_network", TopologyKind.BUS_BREAKER);

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

    private static Network createGeneratorTransformerNodeNetwork() {
        Network network = createBaseNetwork("network", "generator_transformer_network", TopologyKind.NODE_BREAKER);
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
                //.setId("generator1Switch1_SW_fict")
                //.setFictitious(true)
                .setNode1(generator1.getTerminal().getNodeBreakerView().getNode())
                .setNode2(twoWindingsTransformer1.getTerminal2().getNodeBreakerView().getNode())
                //.setOpen(true)
                //.setKind(SwitchKind.BREAKER)
                .add();

        return network;
    }

    private static Network createDisconnectedLoadNBNetwork() {
        Network network = createBaseNetwork("network", "disconnect_load_network", TopologyKind.NODE_BREAKER);

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
                .setId("load2Switch1_SW_fict")
                .setFictitious(true)
                .setNode1(load2.getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1Bus1.getTerminal().getNodeBreakerView().getNode())
                .setOpen(true)
                .setKind(SwitchKind.BREAKER)
                .add();

        return network;
    }

    private static Network createParallelSwitchesNBNetwork() {
        Network network = createBaseNetwork("network", "switches_network", TopologyKind.NODE_BREAKER);

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
        Network network = createBaseNetwork("network", "switches_network", TopologyKind.NODE_BREAKER);

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

    private static Network createBaseNetwork(String id, String sourceFormat, TopologyKind topologyKind) {
        Network network = NetworkFactory.findDefault().createNetwork(id, sourceFormat);

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
