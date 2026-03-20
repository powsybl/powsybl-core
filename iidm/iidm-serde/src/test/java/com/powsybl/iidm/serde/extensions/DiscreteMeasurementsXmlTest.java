/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.serde.*;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class DiscreteMeasurementsXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));

        Switch sw = network.getSwitch("S1VL1_BBS_LD1_DISCONNECTOR");
        sw.newExtension(DiscreteMeasurementsAdder.class).add();
        sw.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("DIS_MEAS_SW_POS")
                .setType(DiscreteMeasurement.Type.SWITCH_POSITION)
                .setValue("CLOSED")
                .setValid(false)
                .putProperty("source", "test")
                .add();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TWT");
        twt.newExtension(DiscreteMeasurementsAdder.class).add();
        twt.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("DIS_MEAS_TAP_POS")
                .setType(DiscreteMeasurement.Type.TAP_POSITION)
                .setTapChanger(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER)
                .setValue(15)
                .setValid(true)
                .putProperty("source", "test2")
                .add();

        allFormatsRoundTripTest(network, "disMeasRef.xiidm", IidmSerDeConstants.CURRENT_IIDM_VERSION);
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("disMeasRef.xiidm", IidmVersion.V_1_5);
    }

    @Test
    void testAnonymizedDiscreteMeasurementsIdsWhenExported() {
        //Given
        Network network = Network.create("test", "test");
        network.newExtension(DiscreteMeasurementsAdder.class).add();
        network.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("discreteMeasurementId")
                .setType(DiscreteMeasurement.Type.OTHER)
                .setValue("Test")
                .setValid(false)
                .putProperty("source", "test")
                .add();
        DiscreteMeasurements discreteMeasurements = network.getExtension(DiscreteMeasurements.class);
        assertNotNull(discreteMeasurements);

        testForAllVersionsSince(IidmVersion.V_1_16, version -> {
            ExportOptions exportOptions = new ExportOptions().setVersion(version.toString(".")).setAnonymized(true);
            // When Export (with anonymized option)
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Anonymizer anonymizer = NetworkSerDe.write(network, exportOptions, os);
            // Then check anonymized id != original ids
            String anonymizedDiscreteMeasurementId = anonymizer.anonymizeString("discreteMeasurementId");
            assertNotEquals("discreteMeasurementId", anonymizedDiscreteMeasurementId);
            // Then check xml content (contain only anonymized id)
            String xmlContent = os.toString(StandardCharsets.UTF_8);
            assertTrue(xmlContent.contains("id=\"" + anonymizedDiscreteMeasurementId + "\""));
            assertFalse(xmlContent.contains("id=\"discreteMeasurementId\""));
            // Then check import without anonymizer
            Network importedNetwork1 = NetworkSerDe.read(new ByteArrayInputStream(os.toByteArray()));
            assertWhenImport(importedNetwork1, anonymizedDiscreteMeasurementId);
            // Then check import with anonymizer
            Network importedNetwork2 = NetworkSerDe.read(new ByteArrayInputStream(os.toByteArray()), new ImportOptions(), anonymizer);
            assertWhenImport(importedNetwork2, "discreteMeasurementId");
        });
    }

    private void assertWhenImport(Network importedNetwork, String anonymizedDiscreteMeasurementId) {
        DiscreteMeasurements importedDiscreteMeasurements = importedNetwork.getExtension(DiscreteMeasurements.class);
        assertNotNull(importedDiscreteMeasurements);
        assertEquals(1, importedDiscreteMeasurements.getDiscreteMeasurements().size());
        DiscreteMeasurement importedDiscreteMeasurement = (DiscreteMeasurement) importedDiscreteMeasurements.getDiscreteMeasurements().stream().findFirst().get();
        assertEquals(anonymizedDiscreteMeasurementId, importedDiscreteMeasurement.getId());
    }

    @Test
    void testOldIIdmNoAnonymizedDiscreteMeasurementsWhenExported() {
        //Given
        Network network = Network.create("test", "test");
        network.newExtension(DiscreteMeasurementsAdder.class).add();
        network.getExtension(DiscreteMeasurements.class)
                .newDiscreteMeasurement()
                .setId("discreteMeasurementId")
                .setType(DiscreteMeasurement.Type.OTHER)
                .setValue("Test")
                .setValid(false)
                .putProperty("source", "test")
                .add();
        DiscreteMeasurements discreteMeasurements = network.getExtension(DiscreteMeasurements.class);
        assertNotNull(discreteMeasurements);

        testForAllPreviousVersions(IidmVersion.V_1_16, version -> {
            ExportOptions exportOptions = new ExportOptions().setVersion(version.toString(".")).setAnonymized(true);
            // When Export (with anonymized option)
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            NetworkSerDe.write(network, exportOptions, os);
            String xmlContent = os.toString(StandardCharsets.UTF_8);
            assertTrue(xmlContent.contains("id=\"discreteMeasurementId\""));
        });
    }
}
