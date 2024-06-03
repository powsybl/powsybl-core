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

class ExportNumberMaxValueTest extends AbstractSerDeTest {
    @Test
    void testTemporaryLimitWithoutValue() throws IOException {
        // XIIDM may not define a value for a temporary limit,
        // In that case it is assumed its value is Double.MAX_VALUE

        // CIMXML uses xsd:float for floating point numbers,
        // xsd:float is an IEEE single-precision 32-bit floating point type
        // CGMES export writes Double.MAX_VALUEs as Float.MAX_VALUE

        Network network = Network.create("temporary-limit-without-value", "manual");
        network.newSubstation().setId("S0").add()
                .newVoltageLevel().setId("VL0").setNominalV(100).setTopologyKind(TopologyKind.BUS_BREAKER).add()
                .getBusBreakerView().newBus().setId("B0").add();
        network.newSubstation().setId("S1").add()
                .newVoltageLevel().setId("VL1").setNominalV(100).setTopologyKind(TopologyKind.BUS_BREAKER).add()
                .getBusBreakerView().newBus().setId("B1").add();
        Line line = network.newLine().setId("L01").setR(1.0).setX(10.0)
                .setBus1("B0").setBus2("B1").add();
        line.newCurrentLimits1()
                .setPermanentLimit(2000)
                .beginTemporaryLimit()
                .setName("L300").setAcceptableDuration(300).setValue(3000)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("L120").setAcceptableDuration(120).setValue(6000)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("L0").setAcceptableDuration(60).setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();

        // Check that the value of the temporary limit for duration 60 has been kept as max value
        assertEquals(Double.MAX_VALUE, line.getCurrentLimits1().orElseThrow().getTemporaryLimit(60).getValue());

        // Export only EQ to CGMES and read the exported file
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, "EQ");
        String basename = "net";
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        String eq = Files.readString(tmpDir.resolve(String.format("%s_EQ.xml", basename)));

        // Look for the value of the temporary limit with acceptable duration 60
        // We know exactly which identifier to look for
        // Include multiple lines and non-greedy matches
        Pattern tatl60Pattern = Pattern.compile("<cim:CurrentLimit rdf:ID=\"_L01_ACLS_T_1_CurrentLimit_OLV_TATL_60\">.*?<cim:CurrentLimit.value>(.*?)</cim:CurrentLimit.value>", Pattern.DOTALL);
        Matcher tatl60Value = tatl60Pattern.matcher(eq);
        assertTrue(tatl60Value.find());
        // Read the value as a double
        double value = Double.parseDouble(tatl60Value.group(1));

        // Check that we have read a very big number,
        // we can not compare exactly with Float.MAX_VALUE because it has not been written with enough precision
        assertTrue(value > Float.MAX_VALUE / 10);
        // And check that we have not written Double.MAX_VALUE
        assertTrue(value < Double.MAX_VALUE / 10);
    }
}
