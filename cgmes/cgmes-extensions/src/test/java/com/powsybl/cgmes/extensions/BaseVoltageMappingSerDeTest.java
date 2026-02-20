/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.ImportOptions;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class BaseVoltageMappingSerDeTest extends AbstractCgmesExtensionTest {

    @Test
    void test() throws IOException {
        Network network = NoEquipmentNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2024-09-17T13:36:37.831Z"));
        network.newExtension(BaseVoltageMappingAdder.class)
                .addBaseVoltage("id_400", 400, Source.IGM)
                .addBaseVoltage("id_380", 380, Source.BOUNDARY)
                .add();
        allFormatsRoundTripTest(network, "/no_equipment_base_voltage_mapping.xml");
    }

    @Test
    void testAnonymizedBaseVoltageIds() throws IOException {
        //Given
        Network network = NoEquipmentNetworkFactory.create();
        ExportOptions exportOptions = new ExportOptions().setAnonymized(true);
        ImportOptions importOptions = new ImportOptions();

        network.newExtension(BaseVoltageMappingAdder.class)
                .addBaseVoltage("id_400", 400, Source.IGM)
                .addBaseVoltage("id_380", 380, Source.BOUNDARY)
                .add();
        byte[] anonymizedXml;
        Anonymizer anonymizer;
        // When Export (with anonymized option)
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            anonymizer = NetworkSerDe.write(network, exportOptions, os);
            anonymizedXml = os.toByteArray();
        }
        // Then check anonymized id != original ids
        String anonymizedId400 = anonymizer.anonymizeString("id_400");
        String anonymizedId380 = anonymizer.anonymizeString("id_380");
        assertNotEquals("id_400", anonymizedId400);
        assertNotEquals("id_380", anonymizedId380);
        // Then check xml content (contain only anonymized id)
        String xmlContent = new String(anonymizedXml, StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("id=\"" + anonymizedId400 + "\""));
        assertTrue(xmlContent.contains("id=\"" + anonymizedId380 + "\""));
        assertFalse(xmlContent.contains("id=\"id_400\""));
        assertFalse(xmlContent.contains("id=\"id_380\""));

        //Then import with anonymizer to restores original ids
        try (ByteArrayInputStream is = new ByteArrayInputStream(anonymizedXml)) {
            Network importedNetwork = NetworkSerDe.read(is, importOptions, anonymizer);
            BaseVoltageMapping importedBaseVoltageMapping = importedNetwork.getExtension(BaseVoltageMapping.class);
            assertEquals("id_400", importedBaseVoltageMapping.getBaseVoltage(400).getId());
            assertEquals("id_380", importedBaseVoltageMapping.getBaseVoltage(380).getId());
        }

        //Then import without anonymizer to check that is still anonymized
        try (ByteArrayInputStream is = new ByteArrayInputStream(anonymizedXml)) {
            Network importedNetwork = NetworkSerDe.read(is);
            BaseVoltageMapping importedBaseVoltageMapping = importedNetwork.getExtension(BaseVoltageMapping.class);
            assertEquals(anonymizedId400, importedBaseVoltageMapping.getBaseVoltage(400).getId());
            assertEquals(anonymizedId380, importedBaseVoltageMapping.getBaseVoltage(380).getId());
        }
    }
}
