
package com.powsybl.cgmes.update;

import com.powsybl.iidm.network.*;
//import org.junit.Test;
//import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangesTester {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangesTester.class);

    public ChangesTester() {
        LOGGER.info("calling testChanges method");
    }

    public static Network createTestNetwork() {

        // Network network = Network.create("test", "test");
        Network network = NetworkFactory.create("TestCase", "code");
        network.setCaseDate(DateTime.parse("2016-06-29T14:54:03.427+02:00"));
        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
            .setId("VL1")
            .setNominalV(380)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vl1.getBusBreakerView().newBus()
            .setId("B1")
            .add();
        vl1.newGenerator()
            .setId("G1")
            .setConnectableBus("B1")
            .setBus("B1")
            .setVoltageRegulatorOn(true)
            .setTargetP(100.0)
            .setTargetV(400.0)
            .setMinP(50.0)
            .setMaxP(150.0)
            .add();
        Substation s2 = network.newSubstation()
            .setId("S2")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
            .setId("VL2")
            .setNominalV(380)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vl2.getBusBreakerView().newBus()
            .setId("B2")
            .add();
        vl2.newLoad()
            .setId("L2")
            .setConnectableBus("B2")
            .setBus("B2")
            .setP0(100.0)
            .setQ0(50.0)
            .add();
        vl2.newStaticVarCompensator()
            .setId("SVC2")
            .setConnectableBus("B2")
            .setBus("B2")
            .setBmin(0.0002)
            .setBmax(0.0008)
            .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
            .setVoltageSetPoint(390)
            .add();
        network.newLine()
            .setId("L1")
            .setVoltageLevel1("VL1")
            .setConnectableBus1("B1")
            .setBus1("B1")
            .setVoltageLevel2("VL2")
            .setConnectableBus2("B2")
            .setBus2("B2")
            .setR(4.0)
            .setX(200.0)
            .setG1(0.0)
            .setB1(0.0)
            .setG2(0.0)
            .setB2(0.0)
            .add();
        return network;
    }

    public static Network modifyTestNetwork() {
        Network network = createTestNetwork();

        network.getVoltageLevel("VL2").newStaticVarCompensator()
            .setId("SVC3")
            .setConnectableBus("B2")
            .setBus("B2")
            .setBmin(0.0002)
            .setBmax(0.0008)
            .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
            .setVoltageSetPoint(390)
            .setReactivePowerSetPoint(350)
            .add();
        
        return network;
    }

    public static void main(String[] args) {
        Network network = createTestNetwork();
        Substation substation = network.getSubstation("S1");
        VoltageLevel voltageLevel = network.getVoltageLevel("VL2");
        LOGGER.info("County is "+ network.getCountries() + " substation ID is "+ substation.getId() + " busView " + voltageLevel.getBusView() );
        
        Network newNetwork = modifyTestNetwork();
        Substation substation1 = network.getSubstation("S1");
        VoltageLevel voltageLevel1 = network.getVoltageLevel("VL2");
        LOGGER.info("County is "+ newNetwork.getCountries() + " substation ID is "+ substation1.getId() + " busView " + voltageLevel1.getBusView());
    }

}
