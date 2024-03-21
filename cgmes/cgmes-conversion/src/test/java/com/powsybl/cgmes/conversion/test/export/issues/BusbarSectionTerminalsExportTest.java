package com.powsybl.cgmes.conversion.test.export.issues;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class BusbarSectionTerminalsExportTest extends AbstractSerDeTest {
    @Test
    void testMicroGridBEUpdate() throws IOException {
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        network.setName("microGridBE-test-update");

        // Test case is bus/branch, CGMES contains busbar sections, but they are not present in the IIDM model
        assertEquals(0, network.getBusbarSectionCount());

        // Export SSH, TP to CGMES and check that terminals from busbar sections are present in the output
        // Even if the busbar themselves have not been included in the IIDM model
        String ssh = export(network, "SSH");
        String tp = export(network, "TP");
        network.getBusBreakerView().getBusStream().forEach(bus -> {
            String bbsTerminals = bus.getProperty(Conversion.PROPERTY_BUSBAR_SECTION_TERMINALS, "");
            if (bbsTerminals.isEmpty()) {
                // Only this bus is missing a busbar in CGMES input (it is a Topological Node without busbar)
                assertEquals("f96d552a-618d-4d0c-a39a-2dea3c411dee", bus.getId());
            } else {
                for (String bbsTerminal : bbsTerminals.split(",")) {
                    assertTrue(ssh.contains(bbsTerminal));
                    assertTrue(tp.contains(bbsTerminal));
                }
            }
        });
    }

    @Test
    void testMicroGridBEFull() throws IOException {
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        network.setName("microGridBE-test-full");

        // Test case is bus/branch, CGMES contains busbar sections, but they are not present in the IIDM model
        assertEquals(0, network.getBusbarSectionCount());

        // Export EQ, SSH, TP to CGMES and check that terminals from busbar sections are NOT present in the output
        // This is a full export, the EQ will not contain definitions for original busbar sections or its terminals
        Map<String, String> files = export(network);
        String eq = files.get("EQ");
        String ssh = files.get("SSH");
        String tp = files.get("TP");
        assertTrue(eq.contains(CgmesSubset.EQUIPMENT.getProfile()));
        assertTrue(ssh.contains(CgmesSubset.STEADY_STATE_HYPOTHESIS.getProfile()));
        assertTrue(tp.contains(CgmesSubset.TOPOLOGY.getProfile()));
        network.getBusBreakerView().getBusStream().forEach(bus -> {
            String bbsTerminals = bus.getProperty(Conversion.PROPERTY_BUSBAR_SECTION_TERMINALS, "");
            if (bbsTerminals.isEmpty()) {
                // Only this bus is missing a busbar in CGMES input (it is a Topological Node without busbar)
                assertEquals("f96d552a-618d-4d0c-a39a-2dea3c411dee", bus.getId());
            } else {
                for (String bbsTerminal : bbsTerminals.split(",")) {
                    assertFalse(eq.contains(bbsTerminal));
                    assertFalse(ssh.contains(bbsTerminal));
                    assertFalse(tp.contains(bbsTerminal));
                }
            }
        });
    }

    private String export(Network network, String profile) throws IOException {
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, profile);
        String basename = network.getNameOrId();
        network.write("CGMES", exportParams, tmpDir.resolve(basename));
        return read(basename, profile);
    }

    private String read(String basename, String profile) throws IOException {
        String instanceFile = String.format("%s_%s.xml", basename, profile);
        return Files.readString(tmpDir.resolve(instanceFile));
    }

    private Map<String, String> export(Network network) throws IOException {
        Properties exportParams = new Properties();
        String basename = network.getNameOrId();
        network.write("CGMES", exportParams, tmpDir.resolve(basename));

        return Map.of("EQ", read(basename, "EQ"),
                "SSH", read(basename, "SSH"),
                "TP", read(basename, "TP"));
    }
}
