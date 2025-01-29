package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static com.powsybl.ucte.converter.UcteExporterTest.testExporter;

class UcteExporterGeneratorTest {

    @Test
    void testMultipleGeneratorsAndLoads() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        // Splits generation on two generators
        network.getVoltageLevel("VLGEN").newGenerator()
            .setId("GEN2")
            .setBus("NGEN")
            .setConnectableBus("NGEN")
            .setMinP(-9999)
            .setMaxP(9999)
            .setVoltageRegulatorOn(true)
            .setTargetV(24.5)
            .setTargetP(607.0)
            .setTargetQ(301.0)
            .add();
        Properties p = new Properties();
        p.put(UcteExporter.NAMING_STRATEGY, "Counter");
        testExporter(network, "/eurostagMultipleGeneratorAndLoad.uct", p);
    }
}
