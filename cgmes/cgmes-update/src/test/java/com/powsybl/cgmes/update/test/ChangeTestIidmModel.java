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
import com.powsybl.iidm.network.*;

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
            .setCountry(Country.AF)
            .setTso("tso")
            .setName("BUS   9_SS")
            .setId("_BUS___9_SS")
            .add();
//        VoltageLevel voltageLevel = substation.newVoltageLevel()
//            .setTopologyKind(TopologyKind.BUS_BREAKER)
//            .setId("bbVL")
//            .setName("bbVL_name")
//            .setNominalV(200.0f)
//            .add();
//        Bus bus = voltageLevel.getBusBreakerView()
//            .newBus()
//            .setName("bus1Name")
//            .setId("bus1")
//            .add();
//        LccConverterStation lccConverterStation = voltageLevel.newLccConverterStation()
//            .setId("lcc")
//            .setName("lcc")
//            .setBus("bus1")
//            .setLossFactor(0.011f)
//            .setPowerFactor(0.5f)
//            .setConnectableBus("bus1")
//            .add();

//        assertTrue(changes.size() == 1);
//        LOGGER.info("IidmChange list size is {}", changes.size());
//
//        /**
//         * Test onUpdate
//         */
//        double p1 = 1.0;
//        double q1 = 2.0;
//        lccConverterStation.getTerminal().setP(p1);
//        lccConverterStation.getTerminal().setQ(q1);

        // assertTrue(changes.size() == 6);
        // network.getTwoWindingsTransformer("_GEN_____-GRID____-1_PT").setB(0.001);
//        network.getGenerator("_GEN____3_SM").setRatedS(100).setMaxP(200.0).setMinP(-200.0);
//        network.getGenerator("_GEN____2_SM").setTargetP(1.0);
//        network.getSubstation("_BUS___10_SS").setCountry(Country.RU);
//        network.getLoad("_LOAD__10_EC").setP0(100.0).setQ0(0.0);
//        network.getVoltageLevel("_BUS___10_VL").setHighVoltageLimit(1.2 * 380.0).setNominalV(10.50);
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
