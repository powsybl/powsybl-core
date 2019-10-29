package com.powsybl.cgmes.conversion.test.update;

//import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Network;

public final class UpdateNetworkFromCatalog14 {
    private UpdateNetworkFromCatalog14() {
    }

    public static void updateNetwork(Network network) throws IOException {
        /**
         * Test onCreation
         */
//        Substation substation = network.newSubstation()
//            .setCountry(network.getSubstation("_INF______SS").getCountry().get())
//            .setGeographicalTags("_SGR_01")
//            .setName("BUS   15_SS")
//            .setId("_BUS____15_SS")
//            .add();
//        VoltageLevel voltageLevel = substation.newVoltageLevel()
//            .setTopologyKind(TopologyKind.BUS_BREAKER)
//            .setId("_BUS____15_VL")
//            .setName("BUS   15_VL")
//            .setHighVoltageLimit(380.0)
//            .setNominalV(200.0f)
//            .add();
//        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
//            .setTopologyKind(TopologyKind.BUS_BREAKER)
//            .setId("_BUS____25_VL")
//            .setName("BUS   25_VL")
//            .setHighVoltageLimit(385.0)
//            .setNominalV(205.0f)
//            .add();
//        BusbarSection busbarSection = voltageLevel.getNodeBreakerView()
//            .newBusbarSection()
//            .setId("BE-Busbar_1")
//            .setName("BE-Busbar_1")
//            .setNode(1)
//            .add(); //"Vertex " + v + " not found"
//        Bus bus = voltageLevel.getBusBreakerView()
//            .newBus()
//            .setName("bus1Name")
//            .setId("bus1")
//            .add();
//        Bus bus2 = voltageLevel2.getBusBreakerView()
//            .newBus()
//            .setName("bus2Name")
//            .setId("bus2")
//            .add();
//        Generator generator = voltageLevel.newGenerator()
//            .setId("_GEN____15_SM")
//            .setName("GEN    15")
//            .setBus("bus1")
//            .setVoltageRegulatorOn(false)
//            .setRatedS(150.0)
//            .setTargetP(1.0)
//            .setTargetQ(2.0)
//            .setMaxP(300.0)
//            .setMinP(-300.0)
//            .add();
//        Terminal terminal = generator.getTerminal();
//        generator.setRegulatingTerminal(terminal);
//        ShuntCompensator shuntCompensator = voltageLevel.newShuntCompensator()
//            .setId("_BANK___15_SC")
//            .setName("BANK   15")
//            .setbPerSection(1)
//            .setMaximumSectionCount(2)
//            .setBus("bus1")
//            .setCurrentSectionCount(1)
//            .add();
//        TwoWindingsTransformer tWTransformer = substation.newTwoWindingsTransformer()
//            .setId("_BUS____4-BUS____15-1_PT")
//            .setName("BUS    4-BUS    15-1")
//            .setVoltageLevel1("_BUS____15_VL")
//            .setVoltageLevel2("_BUS____25_VL")
//            .setConnectableBus1("bus1")
//            .setConnectableBus2("bus2")
//            .setR(2.0)
//            .setX(14.745)
//            .setG(4.0)
//            .setB(3.2E-5)
//            .setRatedU1(111.0)
//            .setRatedU2(222.0)
//            .add();
//        RatioTapChanger ratioTapChanger = tWTransformer.newRatioTapChanger()
//            .setLowTapPosition(0)
//            .setTapPosition(1)
//            .setRegulating(false)
//            .setRegulationTerminal(tWTransformer.getTerminal(Branch.Side.ONE))
//            .setTargetV(3)
//            .setLoadTapChangingCapabilities(false)
//            .beginStep().setR(-28.1).setX(-28.2).setG(0.1).setB(0.2).setRho(1.1).endStep()
//            .beginStep().setR(-28.3).setX(-28.4).setG(0.2).setB(0.3).setRho(1.3).endStep()
//            .add();
//        PhaseTapChanger phaseTapChanger = tWTransformer.newPhaseTapChanger()
//            .setLowTapPosition(0)
//            .setTapPosition(1)
//            .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
//            .setRegulationValue(930.6667)
//            .setRegulating(false)
//            .setRegulationTerminal(tWTransformer.getTerminal(Branch.Side.ONE))
//            .beginStep().setR(-28.091503).setX(-28.091503).setG(0.0).setB(0.0).setRho(1.0).setAlpha(5.42).endStep()
//            .beginStep().setR(39.78473).setX(39.784725).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-42.8).endStep()
//            .add();
////////        tWTransformer.newCurrentLimits1()
////////            .setPermanentLimit(931.0)
////////            .add();
////////        tWTransformer.newCurrentLimits2()
////////            .setPermanentLimit(931.0)
////////            .add();
////////        BusbarSection busbarSection = voltageLevel.getNodeBreakerView().newBusbarSection()
////////            .setId("_64901aec-5a8a-4bcb-8ca7-a3ddbfcd0e6c")
////////            .setName("BE-Busbar_1")
////////            .add();
//        Load load = voltageLevel.newLoad()
//            .setId("_LOAD___15_EC")
//            .setName("LOAD  15")
//            .setBus("bus1")
//            .setP0(20.0)
//            .setQ0(15.0)
//            .add();
//        Line line = network.newLine()
//            .setId("_27086487-56ba-4979-b8de-064025a6b4da")
//            .setName("BE-Line_9")
//            .setVoltageLevel1("_BUS____15_VL")
//            .setVoltageLevel2("_BUS____9_VL")
//            .setBus1("bus1")
//            .setBus2("_BUS____9_TN")
//            .setR(2.2)
//            .setX(68.1)
//            .setG1(0.01)
//            .setG2(0.02)
//            .setB1(0.03)
//            .setB2(0.04)
//            .add();

//        assertTrue(changes.size() == 1);
//
        /**
         * Test onUpdate
         */
        // assertTrue(changes.size() == 6);
////        network.getVoltageLevel("_BUS___10_VL").getNodeBreakerView().getNodes();
//        network.getBusBreakerView().getBus("_BUS___10_TN").setV(3.3).setAngle(3.33);// --> in _SV
//        network.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").setB(1.0).setG(2.0)
//            .setR(3.0).setX(4.0).setRatedU1(11.1).setRatedU2(22.2);
//        network.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").getRatioTapChanger()
//            .setTapPosition(7);// ?? --> not in change
        network.getGenerator("_GEN____3_SM").setRatedS(100).setMaxP(200.0).setMinP(-200.0);
        // .setTargetP(1.0);
//        network.getGenerator("_GEN____2_SM").setTargetP(11.1);// ??setTargetP --> not in change
//        network.getSubstation("_BUS____4_SS").setCountry(Country.GR);
//        network.getLoad("_LOAD__10_EC").setP0(100.0).setQ0(5.0);// ??? --> not in change
//        network.getVoltageLevel("_BUS___10_VL").setHighVoltageLimit(1.2 * 380.0)
//            .setLowVoltageLimit(301.0).setNominalV(10.50);
//        network.getLine("_BUS___10-BUS___11-1_AC").setB1(1.0).setB2(2.0).setG1(1.1).setG2(2.1)
//            .setR(3.0).setX(4.0);
//        for (ShuntCompensator sc : network.getVoltageLevel("_BUS____9_VL").getShuntCompensators()) {
//            if (sc.getId().equals("_BANK___9_SC")) {
//                sc.setbPerSection(1.1)
//                    .setMaximumSectionCount(2);
//            }
//        }
//        LOG.info("checkBusBreakerView "
//            + network.getLoad("_LOAD__10_EC").getTerminal().getBusBreakerView()
//                .getBus().getId() // --> cim:TopologicalNode in TP
////            + "; checkNodeBreakerView "
////            + network.getLoad("_LOAD__10_EC").getTerminal().getNodeBreakerView().getNode()
//            + "; checkBusView "
//            + network.getLoad("_LOAD__10_EC").getTerminal().getBusView().getBus().getId());
        /**
         * Test onRemove
         */
//        network.getLine("_BUS____2-BUS____3-1_AC").remove();
//        network.getVoltageLevel("_BUS____3_VL").remove();
//        network.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").remove();

//        assertTrue(changes.size() == 9);

    }

    private static final Logger LOG = LoggerFactory.getLogger(UpdateNetworkFromCatalog14.class);
}
