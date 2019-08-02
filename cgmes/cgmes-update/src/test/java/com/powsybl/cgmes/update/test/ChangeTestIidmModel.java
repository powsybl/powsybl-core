package com.powsybl.cgmes.update.test;

//import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.CgmesUpdater;
import com.powsybl.cgmes.update.IidmChange;
import com.powsybl.cgmes.update.IidmToCgmes;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;

public class ChangeTestIidmModel {

    public ChangeTestIidmModel(Network network) {
        this.network = network;
        changes = new ArrayList<>();
        cgmesUpdater = new CgmesUpdater(network, changes);
    }

    public Network updateImportedTestModel() throws IOException {

        cgmesUpdater.addListenerForUpdates();

        /**
         * Test onCreation
         */
        Substation substation = network.newSubstation()
            .setCountry(Country.FI)
            .setTso("tso")
            .setName("BUS   15_SS")
            .setId("_BUS____15_SS")
            .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .setId("_BUS____15_VL")
            .setName("BUS   15_VL")
            .setHighVoltageLimit(380.0)
            .setNominalV(200.0f)
            .add();
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .setId("_BUS____25_VL")
            .setName("BUS   25_VL")
            .setHighVoltageLimit(385.0)
            .setNominalV(205.0f)
            .add();
        Bus bus = voltageLevel.getBusBreakerView()
            .newBus()
            .setName("bus1Name")
            .setId("bus1")
            .add();
        Bus bus2 = voltageLevel2.getBusBreakerView()
            .newBus()
            .setName("bus2Name")
            .setId("bus2")
            .add();
        TwoWindingsTransformer tWTransformer = substation.newTwoWindingsTransformer()
            .setId("_BUS____4-BUS____15-1_PT")
            .setName("BUS    4-BUS    15-1")
            .setVoltageLevel1("_BUS____15_VL")
            .setVoltageLevel2("_BUS____25_VL")
            .setConnectableBus1("bus1")
            .setConnectableBus2("bus2")
            .setR(2.0)
            .setX(14.745)
            .setG(0.0)
            .setB(3.2E-5)
            .setRatedU1(225.0)
            .setRatedU2(225.0)
            .add();
        tWTransformer.newCurrentLimits1()
            .setPermanentLimit(931.0)
            .add();
        tWTransformer.newCurrentLimits2()
            .setPermanentLimit(931.0)
            .add();
        PhaseTapChanger phaseTapChanger = tWTransformer.newPhaseTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(1)
            .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
            .setRegulationValue(930.6667)
            .setRegulating(false)
            .setRegulationTerminal(tWTransformer.getTerminal(Branch.Side.ONE))
            .beginStep().setR(-28.091503).setX(-28.091503).setG(0.0).setB(0.0).setRho(1.0).setAlpha(5.42).endStep()
            .beginStep().setR(39.78473).setX(39.784725).setG(0.0).setB(0.0).setRho(1.0).setAlpha(-42.8).endStep()
            .add();
//        BusbarSection busbarSection = voltageLevel.getNodeBreakerView().newBusbarSection()
//            .setId("_64901aec-5a8a-4bcb-8ca7-a3ddbfcd0e6c")
//            .setName("BE-Busbar_1")
//            .add();
        Load load = voltageLevel.newLoad()
            .setId("_LOAD___15_EC")
            .setName("LOAD  15")
            .setBus("bus1")
            .setP0(20.0)
            .setQ0(15.0)
            .add();
        Generator generator = voltageLevel.newGenerator()
            .setId("_GEN____15_SM")
            .setName("GEN    15")
            .setBus("bus1")
            .setVoltageRegulatorOn(false)
            // .setRegulatingTerminal(regulatingTerminal)
            .setRatedS(150.0)
            .setTargetP(0.0)
            .setTargetQ(0.0)
            .setMaxP(300.0)
            .setMinP(-300.0)
            .add();
        LccConverterStation lccConverterStation = voltageLevel.newLccConverterStation()
            .setId("lcc")
            .setName("lcc")
            .setBus("bus1")
            .setLossFactor(0.011f)
            .setPowerFactor(0.5f)
            .setConnectableBus("bus1")
            .add();
        Line line = network.newLine()
            .setId("_27086487-56ba-4979-b8de-064025a6b4da")
            .setName("BE-Line_9")
            .setVoltageLevel1("_BUS____15_VL")
            .setVoltageLevel2("_BUS____9_VL")
            .setBus1("bus1")
            .setBus2("_BUS____9_TN")
            .setR(2.2)
            .setX(68.1)
            .setG1(0.01)
            .setG2(0.02)
            .setB1(0.03)
            .setB2(0.04)
            .add();

//        assertTrue(changes.size() == 1);
//
//        /**
//         * Test onUpdate
//         */
//        double p1 = 1.0;
//        double q1 = 2.0;
//        lccConverterStation.getTerminal().setP(p1);
//        lccConverterStation.getTerminal().setQ(q1);

        // assertTrue(changes.size() == 6);
//        network.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").setB(0.001);
//        network.getGenerator("_GEN____3_SM").setRatedS(100).setMaxP(200.0).setMinP(-200.0);
//        network.getGenerator("_GEN____2_SM").setTargetP(1.0);
//        network.getSubstation("_BUS___10_SS").setCountry(Country.RU);
//        network.getLoad("_LOAD__10_EC").setP0(100.0).setQ0(0.0);
//        network.getVoltageLevel("_BUS___10_VL").setHighVoltageLimit(1.2 * 380.0).setNominalV(10.50);
        Bus checkBusBreakerView = network.getLoad("_LOAD__10_EC").getTerminal().getBusBreakerView().getBus();
        Bus checkBusView = network.getLoad("_LOAD__10_EC").getTerminal().getBusView().getBus();

//        assertTrue(changes.size() == 9);
        LOGGER.info("IidmChange list size is {}", changes.size());

        return network;
    }

    public CgmesModel updateTester() {
        try {
            return cgmesUpdater.update();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private Network network;
    List<IidmChange> changes;
    IidmToCgmes iidmToCgmes;
    CgmesUpdater cgmesUpdater;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeTestIidmModel.class);
}
