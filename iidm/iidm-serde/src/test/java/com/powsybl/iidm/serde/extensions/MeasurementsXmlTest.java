/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class MeasurementsXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));

        Load load = network.getLoad("LOAD");
        load.newExtension(MeasurementsAdder.class).add();
        load.getExtension(Measurements.class)
                .newMeasurement()
                .setId("MEAS_LOAD_P")
                .setType(Measurement.Type.ACTIVE_POWER)
                .setValue(580.0)
                .setStandardDeviation(5.0)
                .setValid(false)
                .putProperty("source", "test")
                .add();

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NGEN_NHV1");
        twt.newExtension(MeasurementsAdder.class).add();
        twt.getExtension(Measurements.class)
                .newMeasurement()
                .setId("MEAS_TWT_Q_2")
                .setType(Measurement.Type.REACTIVE_POWER)
                .setSide(ThreeSides.TWO)
                .setValue(-600.07)
                .setStandardDeviation(10.2)
                .setValid(true)
                .add();
        twt.getExtension(Measurements.class)
                .newMeasurement()
                .setId("MEAS_TWT_Q_1")
                .setType(Measurement.Type.REACTIVE_POWER)
                .setSide(ThreeSides.ONE)
                .setValue(605.2)
                .setStandardDeviation(9.7)
                .setValid(true)
                .putProperty("source", "test2")
                .add();

        allFormatsRoundTripTest(network, "measRef.xiidm", IidmSerDeConstants.CURRENT_IIDM_VERSION);
    }

    @Test
    void testAnonymizedMeasurementsIdsWhenExported() {
        //Given
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        load.newExtension(MeasurementsAdder.class).add();
        load.getExtension(Measurements.class)
                .newMeasurement()
                .setId("Measurement_ID")
                .setType(Measurement.Type.OTHER)
                .setValue(0)
                .setValid(false)
                .putProperty("source", "test")
                .add();
        Measurements measurements = load.getExtension(Measurements.class);
        assertNotNull(measurements);

        testForAllVersionsSince(IidmVersion.V_1_16, version -> {
            ExportOptions exportOptions = new ExportOptions().setVersion(version.toString(".")).setAnonymized(true);
            // When Export (with anonymized option)
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Anonymizer anonymizer = NetworkSerDe.write(network, exportOptions, os);
            // Then check anonymized id != original id
            String anonymizedMeasurementId = anonymizer.anonymizeString("Measurement_ID");
            assertNotEquals("Measurement_ID", anonymizedMeasurementId);
            // Then check xml content (contain only anonymized id)
            String xmlContent = os.toString(StandardCharsets.UTF_8);
            assertTrue(xmlContent.contains("id=\"" + anonymizedMeasurementId + "\""));
            assertFalse(xmlContent.contains("id=\"Measurement_ID\""));
            // Then check import without anonymizer
            Network importedNetwork1 = NetworkSerDe.read(new ByteArrayInputStream(os.toByteArray()));
            assertWhenImport(importedNetwork1, anonymizer.anonymizeString("LOAD"), anonymizedMeasurementId);
            // Then check import with anonymizer
            Network importedNetwork2 = NetworkSerDe.read(new ByteArrayInputStream(os.toByteArray()), new ImportOptions(), anonymizer);
            assertWhenImport(importedNetwork2, "LOAD", "Measurement_ID");
        });
    }

    private void assertWhenImport(Network importedNetwork, String loadID, String expectedMeasurementId) {
        Load importedLoad = importedNetwork.getLoad(loadID);
        Measurements importedMeasurements = importedLoad.getExtension(Measurements.class);
        assertNotNull(importedMeasurements);
        assertEquals(1, importedMeasurements.getMeasurements().size());
        Measurement importedDiscreteMeasurement = (Measurement) importedMeasurements.getMeasurements().stream().findFirst().get();
        assertEquals(expectedMeasurementId, importedDiscreteMeasurement.getId());
    }

    @Test
    void testOldIIdmNoAnonymizedMeasurementsWhenExported() {
        //Given
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        load.newExtension(MeasurementsAdder.class).add();
        load.getExtension(Measurements.class)
                .newMeasurement()
                .setId("Measurement_ID")
                .setType(Measurement.Type.OTHER)
                .setValue(0)
                .setValid(false)
                .putProperty("source", "test")
                .add();
        Measurements measurements = load.getExtension(Measurements.class);
        assertNotNull(measurements);

        testForAllPreviousVersions(IidmVersion.V_1_16, version -> {
            ExportOptions exportOptions = new ExportOptions().setVersion(version.toString(".")).setAnonymized(true);
            // When Export (with anonymized option)
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            NetworkSerDe.write(network, exportOptions, os);
            String xmlContent = os.toString(StandardCharsets.UTF_8);
            assertTrue(xmlContent.contains("id=\"Measurement_ID\""));
        });
    }

}
