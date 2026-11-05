/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.getUniqueMatches;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Export of the CGMES 3 attribute {@code Equipment.inService}: it is always written {@code true}, the out-of-service
 * state being carried by the terminals and the switches.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
class EquipmentInServiceExportTest extends AbstractSerDeTest {

    private static final Pattern SSH_OBJECT = Pattern.compile("<cim:(\\w+) rdf:about=\"#_?([^\"]+)\">(.*?)</cim:\\w+>", Pattern.DOTALL);
    private static final Pattern IN_SERVICE = Pattern.compile("<cim:Equipment.inService>(\\w+)</cim:Equipment.inService>");

    @Test
    @DisplayName("CGMES 3 SSH export writes Equipment.inService=true once for every exported equipment")
    void cim100ExportTest() throws IOException {
        // The ENTSO-E test models are the oracle: every equipment carries inService in their SSH
        assertInServiceWrittenOnceForEveryEquipment(Cgmes3Catalog.microGrid(), "microGrid");
        assertInServiceWrittenOnceForEveryEquipment(Cgmes3Catalog.miniGrid(), "miniGrid");
        assertInServiceWrittenOnceForEveryEquipment(Cgmes3Catalog.smallGrid(), "smallGrid");
    }

    @Test
    @DisplayName("CGMES 2.4.15 SSH export does not write Equipment.inService")
    void cim16ExportTest() throws IOException {
        Network network = Network.read(Cgmes3Catalog.microGrid().dataSource());
        Properties properties = new Properties();
        properties.put(CgmesExport.CIM_VERSION, "16");
        properties.put(CgmesExport.PROFILES, List.of("SSH"));
        network.write("CGMES", properties, tmpDir.resolve("cim16"));
        assertFalse(Files.readString(tmpDir.resolve("cim16_SSH.xml")).contains("Equipment.inService"));
    }

    @Test
    @DisplayName("Out-of-service equipment stays disconnected after a CGMES 3 round trip")
    void roundTripTest() throws IOException {
        // In the ENTSO-E MiniGrid, a load and two lines to the boundary are out of service
        ReadOnlyDataSource dataSource = Cgmes3Catalog.miniGrid().dataSource();
        Network network = Network.read(dataSource);
        Properties properties = new Properties();
        properties.put(CgmesExport.CIM_VERSION, "100");
        network.write("CGMES", properties, tmpDir.resolve("roundTrip"));
        // The boundary set is needed for the re-import
        for (String boundary : dataSource.listNames(".*_BD.*")) {
            try (InputStream is = dataSource.newInputStream(boundary)) {
                Files.copy(is, tmpDir.resolve("roundTrip_" + boundary));
            }
        }
        for (String profile : List.of("EQ", "TP", "SSH", "SV")) {
            assertFalse(Files.readString(tmpDir.resolve("roundTrip_" + profile + ".xml")).contains("_SW_fict"));
        }

        // The out-of-service equipment is exported in service with disconnected terminals: it stays disconnected
        Network reimported = Network.read(new GenericReadOnlyDataSource(tmpDir.toAbsolutePath(), "roundTrip"));
        assertFalse(reimported.getLoad("d711629e-7b35-4b01-867a-f8fd41362a88").getTerminal().isConnected());
        assertFalse(reimported.getBoundaryLine("5150a037-e241-421f-98b2-fe60e5c90303").getTerminal().isConnected());
        assertFalse(reimported.getBoundaryLine("f32baf36-7ea3-4b6a-9452-71e7f18779f8").getTerminal().isConnected());
        assertEquals(network.getLoadStream().filter(l -> l.getTerminal().isConnected()).count(),
                reimported.getLoadStream().filter(l -> l.getTerminal().isConnected()).count());
    }

    /**
     * Export a CGMES 3 test model and assert that every equipment that carries {@code Equipment.inService} in the SSH of
     * the model, and that is exported, has {@code inService=true} written exactly once in the exported SSH.
     *
     * @param model the CGMES 3 test model
     * @param baseName the base name of the exported files
     * @throws IOException if the files cannot be read or written
     */
    private void assertInServiceWrittenOnceForEveryEquipment(GridModelReferenceResources model, String baseName) throws IOException {
        Map<String, List<String>> expected = inServiceValuesBySubject(model.dataSource(), ".*SSH.*");

        Network network = Network.read(model.dataSource());
        Properties properties = new Properties();
        properties.put(CgmesExport.CIM_VERSION, "100");
        network.write("CGMES", properties, tmpDir.resolve(baseName));
        Set<String> exportedEquipment = getUniqueMatches(Files.readString(tmpDir.resolve(baseName + "_EQ.xml")),
                Pattern.compile("rdf:ID=\"_?([^\"]+)\""));
        Map<String, List<String>> exported = inServiceValuesBySubject(Files.readString(tmpDir.resolve(baseName + "_SSH.xml")));

        List<String> wrong = expected.keySet().stream()
                .filter(exportedEquipment::contains)
                .filter(id -> !List.of("true").equals(exported.get(id)))
                .map(id -> id + "=" + exported.get(id))
                .sorted()
                .toList();
        assertEquals(List.of(), wrong);
    }

    /**
     * Read the {@code Equipment.inService} values of the files of a data source.
     *
     * @param dataSource the data source
     * @param regex the regular expression on the names of the files to read
     * @return the {@code inService} values written for each equipment id
     * @throws IOException if a file cannot be read
     */
    private static Map<String, List<String>> inServiceValuesBySubject(ReadOnlyDataSource dataSource, String regex) throws IOException {
        Map<String, List<String>> values = new HashMap<>();
        for (String name : dataSource.listNames(regex)) {
            try (InputStream is = dataSource.newInputStream(name)) {
                inServiceValuesBySubject(new String(is.readAllBytes(), StandardCharsets.UTF_8))
                        .forEach((id, v) -> values.computeIfAbsent(id, k -> new ArrayList<>()).addAll(v));
            }
        }
        return values;
    }

    /**
     * Read the {@code Equipment.inService} values of an SSH file.
     *
     * @param ssh the content of the SSH file
     * @return the {@code inService} values written for each equipment id
     */
    private static Map<String, List<String>> inServiceValuesBySubject(String ssh) {
        Map<String, List<String>> values = new HashMap<>();
        Matcher object = SSH_OBJECT.matcher(ssh);
        while (object.find()) {
            Matcher inService = IN_SERVICE.matcher(object.group(3));
            while (inService.find()) {
                values.computeIfAbsent(object.group(2), k -> new ArrayList<>()).add(inService.group(1));
            }
        }
        return values;
    }
}
