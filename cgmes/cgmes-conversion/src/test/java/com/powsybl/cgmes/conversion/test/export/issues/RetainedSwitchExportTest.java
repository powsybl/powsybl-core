package com.powsybl.cgmes.conversion.test.export.issues;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class RetainedSwitchExportTest extends AbstractSerDeTest {
    @Test
    void testRetainedSwitchDifferentTN() throws IOException {
        Network network = Network.create("retained-switch-same-TN", "manual");
        VoltageLevel vl = network.newSubstation().setId("S0").add().newVoltageLevel()
                .setId("VL0")
                .setNominalV(100)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection().setId("BBS0").setNode(0).add();
        vl.getNodeBreakerView().newBusbarSection().setId("BBS1").setNode(1).add();
        vl.getNodeBreakerView().newBreaker().setId("COUPLER").setRetained(true).setNode1(0).setNode2(1).add();
        vl.newLoad().setId("LOAD").setNode(2).setP0(1).setQ0(0).add();
        vl.getNodeBreakerView().newBreaker().setId("LOAD_BK").setNode1(2).setNode2(0).add();
        vl.newGenerator().setId("GEN").setNode(3).setTargetP(1).setTargetQ(0).setMinP(0).setMaxP(10).setVoltageRegulatorOn(false).add();
        vl.getNodeBreakerView().newBreaker().setId("GEN_BK").setNode1(3).setNode2(1).add();

        // Check that bus/breaker view buses are different at ends of coupler
        Bus bus1 = vl.getBusBreakerView().getBus1("COUPLER");
        Bus bus2 = vl.getBusBreakerView().getBus2("COUPLER");
        assertNotEquals(bus1, bus2);

        // Export only EQ to CGMES
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, "EQ");

        String basenameRetained = "net";
        network.write("CGMES", exportParams, tmpDir.resolve(basenameRetained));
        String eqRetained = read(basenameRetained, "EQ");

        // Look for the coupler retained attribute
        // Include multiple lines and non-greedy matches
        Pattern couplerRetainedPattern = Pattern.compile("<cim:Breaker rdf:ID=\"_COUPLER\">.*?<cim:Switch.retained>(.*?)</cim:Switch.retained>", Pattern.DOTALL);
        Matcher couplerRetained = couplerRetainedPattern.matcher(eqRetained);
        assertTrue(couplerRetained.find());
        assertTrue(Boolean.parseBoolean(couplerRetained.group(1)));

        // Now add a load that is connected to the two busbars through disconnectors
        vl.newLoad().setId("LOAD_BOTH").setP0(5).setQ0(0).setNode(5).add();
        vl.getNodeBreakerView().newBreaker().setId("LOAD5_BK").setNode1(5).setNode2(4).add();
        vl.getNodeBreakerView().newDisconnector().setId("LOAD5_DIS0").setNode1(4).setNode2(0).add();
        vl.getNodeBreakerView().newDisconnector().setId("LOAD5_DIS1").setNode1(4).setNode2(1).add();

        // Export only EQ to CGMES
        String basenameNonRetained = "netnr";
        network.write("CGMES", exportParams, tmpDir.resolve(basenameNonRetained));
        String eqNonRetained = read(basenameNonRetained, "EQ");

        // Look for the coupler retained attribute
        // Include multiple lines and non-greedy matches
        Matcher couplerNonRetained = couplerRetainedPattern.matcher(eqNonRetained);
        assertTrue(couplerNonRetained.find());
        assertFalse(Boolean.parseBoolean(couplerNonRetained.group(1)));
    }

    private String read(String basename, String profile) throws IOException {
        String instanceFile = String.format("%s_%s.xml", basename, profile);
        return Files.readString(tmpDir.resolve(instanceFile));
    }
}
