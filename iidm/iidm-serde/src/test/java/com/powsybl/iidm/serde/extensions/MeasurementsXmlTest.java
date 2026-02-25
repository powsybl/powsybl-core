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
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.IidmSerDeConstants;
import com.powsybl.iidm.serde.NetworkSerDe;
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
    void testAnonymizedMeasurementsIds() throws IOException {
        //Given
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
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
        // When Export (with anonymized option)
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Anonymizer anonymizer = NetworkSerDe.write(network, new ExportOptions().setAnonymized(true), os);
        // Then check anonymized id != original id
        String anonymizedMeasurementId = anonymizer.anonymizeString("Measurement_ID");
        assertNotEquals("Measurement_ID", anonymizedMeasurementId);
        // Then check xml content (contain only anonymized id)
        byte[] anonymizedXml = os.toByteArray();
        String xmlContent = new String(anonymizedXml, StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("id=\"" + anonymizedMeasurementId + "\""));
        assertFalse(xmlContent.contains("id=\"Measurement_ID\""));
        //Then import without anonymizer
        try (ByteArrayInputStream is = new ByteArrayInputStream(anonymizedXml)) {
            Network importedNetwork = NetworkSerDe.read(is);
            Load importedLoad = importedNetwork.getLoad(anonymizer.anonymizeString("LOAD"));
            Measurements importedMeasurements = importedLoad.getExtension(Measurements.class);
            assertNotNull(importedMeasurements);
            assertEquals(1, importedMeasurements.getMeasurements().size());
            Measurement importedDiscreteMeasurement = (Measurement) importedMeasurements.getMeasurements().stream().findFirst().get();
            assertEquals(anonymizedMeasurementId, importedDiscreteMeasurement.getId());
        }
    }

}
