package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static com.powsybl.ucte.converter.UcteExporterTest.testExporter;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UcteExporterGeneratorTest {

    Network network = EurostagTutorialExample1Factory.create();

    @Test
    void testGenerator() throws IOException {
        Properties p = new Properties();
        p.put(UcteExporter.NAMING_STRATEGY, "Counter");
        testExporter(network, "/eurostag.uct", p);
    }

    @Test
    void testMultipleGeneratorsAndLoads() throws IOException {
        createGen2AndSplitGeneration();

        Properties p = new Properties();
        p.put(UcteExporter.NAMING_STRATEGY, "Counter");
        testExporter(network, "/eurostag.uct", p);
    }

    @Test
    void testMultipleGeneratorDifferentTargetV() throws IOException {
        createGen2AndSplitGeneration();

        network.getGenerator("GEN2").setTargetV(30);

        assertNotEquals(network.getGenerator("GEN").getTargetV(), network.getGenerator("GEN2").getTargetV());

        Properties p = new Properties();
        p.put(UcteExporter.NAMING_STRATEGY, "Counter");
        // TargetV kept is the one of GEN, so the export doesn't change
        testExporter(network, "/eurostag.uct", p);
    }

    @Test
    void testGeneratorNotRegulatingVoltage() throws IOException {
        network.getGenerator("GEN").setVoltageRegulatorOn(false);

        Properties p = new Properties();
        p.put(UcteExporter.NAMING_STRATEGY, "Counter");
        // The UCTE node associated to NGEN is now PQ and not PV
        testExporter(network, "/eurostagGeneratorNotRegulating.uct", p);
    }

    @Test
    void testHydroGenerator() throws IOException {
        network.getGenerator("GEN").setEnergySource(EnergySource.HYDRO);

        Properties p = new Properties();
        p.put(UcteExporter.NAMING_STRATEGY, "Counter");
        // The UCTE node associated to NGEN is now H and not F
        testExporter(network, "/eurostagHydroGen.uct", p);
    }

    @Test
    void testMultipleGeneratorWithDifferentEnergySource() throws IOException {
        createGen2AndSplitGeneration();
        network.getGenerator("GEN2").setEnergySource(EnergySource.HYDRO);

        Properties p = new Properties();
        p.put(UcteExporter.NAMING_STRATEGY, "Counter");
        // The UCTE node associated to NGEN is F as GEN and GEN2 do not have the same EnergySource
        testExporter(network, "/eurostag.uct", p);
    }

    @Test
    void testRemoteRegulatingGenerator() throws IOException {
        // Set regulation to remote, the generator GEN now regulates after the transformer with an equivalent targetV
        network.getGenerator("GEN").setRegulatingTerminal(network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2()).setTargetV(24.5 * 380 / 24);
        Properties p = new Properties();
        p.put(UcteExporter.NAMING_STRATEGY, "Counter");
        testExporter(network, "/eurostag.uct", p);
    }

    @Test
    void testRemoteAndLocalRegulatingGenerator() throws IOException {
        // Create a second generator and make it regulate remotely after the transformer with a different targetV
        createGen2AndSplitGeneration();
        network.getGenerator("GEN2").setRegulatingTerminal(network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2()).setTargetV(390);
        Properties p = new Properties();
        p.put(UcteExporter.NAMING_STRATEGY, "Counter");
        // TargetV exported will be the one of the local generator GEN and not the targetV of the remote generator
        testExporter(network, "/eurostag.uct", p);
    }

    @Test
    void testActiveRemoteAndInactiveLocalRegulatingGenerator() throws IOException {
        // Create a second generator and make it regulate remotely after the transformer with a different targetV
        createGen2AndSplitGeneration();
        network.getGenerator("GEN2").setRegulatingTerminal(network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2()).setTargetV(24.5 * 380 / 24);
        // The local generator has inactive regulation
        network.getGenerator("GEN").setVoltageRegulatorOn(false).setTargetV(25);
        Properties p = new Properties();
        p.put(UcteExporter.NAMING_STRATEGY, "Counter");
        // TargetV exported will be the one of the local generator GEN and not the targetV of the remote generator
        testExporter(network, "/eurostag.uct", p);
    }

    private void createGen2AndSplitGeneration() {
        // Splits generation on two generators
        network.getVoltageLevel("VLGEN").newGenerator()
            .setId("GEN2")
            .setBus("NGEN")
            .setConnectableBus("NGEN")
            .setMinP(-9999.99 / 2)
            .setMaxP(9999.99 / 2)
            .setVoltageRegulatorOn(true)
            .setTargetV(24.5)
            .setTargetP(303.5)
            .setTargetQ(150.5)
            .add();

        network.getGenerator("GEN").setTargetP(303.5).setTargetQ(150.5).setMinP(-9999.99 / 2).setMaxP(9999.99 / 2);

        network.getGenerator("GEN").newMinMaxReactiveLimits()
            .setMinQ(-9999.99 / 2)
            .setMaxQ(9999.99 / 2)
            .add();

        network.getGenerator("GEN2").newMinMaxReactiveLimits()
            .setMinQ(-9999.99 / 2)
            .setMaxQ(9999.99 / 2)
            .add();
    }

}
