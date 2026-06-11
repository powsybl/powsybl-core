/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.IidmVersion;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class CgmesTapChangersSerDeTest extends AbstractCgmesExtensionTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWith3wTransformer();
        network.setCaseDate(ZonedDateTime.parse("2024-09-17T12:01:34.831Z"));
        TwoWindingsTransformer twoWT = network.getTwoWindingsTransformer("NGEN_NHV1");
        CgmesTapChangers<TwoWindingsTransformer> ctc2wt = ((CgmesTapChangersAdder<TwoWindingsTransformer>) twoWT.newExtension(CgmesTapChangersAdder.class)).add();
        ctc2wt.newTapChanger()
                .setId("tc1")
                .setControlId("control1")
                .setStep(1)
                .setType("type1")
                .setHiddenStatus(false)
                .add();
        ThreeWindingsTransformer threeWT = network.getThreeWindingsTransformer("NGEN_V2_NHV1");
        CgmesTapChangers<ThreeWindingsTransformer> ctc3wt = ((CgmesTapChangersAdder<ThreeWindingsTransformer>) threeWT.newExtension(CgmesTapChangersAdder.class)).add();
        ctc3wt.newTapChanger()
                .setId("tc2")
                .setCombinedTapChangerId("ctc2")
                .setStep(1)
                .setControlId("control2")
                .setType("type2")
                .setHiddenStatus(true)
                .add();
        allFormatsRoundTripTest(network, "/eurostag_cgmes_tap_changers.xml");
    }

    @Test
    void testAnonymizedCgmesTapChangersIdAndControlIdWhenExported() {
        //Given
        Network network = EurostagTutorialExample1Factory.createWith3wTransformer();
        network.setCaseDate(ZonedDateTime.parse("2024-09-17T12:01:34.831Z"));
        TwoWindingsTransformer twoWT = network.getTwoWindingsTransformer("NGEN_NHV1");
        CgmesTapChangers<TwoWindingsTransformer> ctc2wt = ((CgmesTapChangersAdder<TwoWindingsTransformer>) twoWT.newExtension(CgmesTapChangersAdder.class)).add();
        ctc2wt.newTapChanger()
                .setId("tc1")
                .setControlId("control1")
                .setStep(1)
                .setType("type1")
                .setHiddenStatus(false)
                .add();
        CgmesTapChangers cgmesTapChangers = twoWT.getExtension(CgmesTapChangers.class);
        assertNotNull(cgmesTapChangers);

        testForAllVersionsSince(IidmVersion.V_1_16, version -> {
            ExportOptions exportOptions = new ExportOptions().setVersion(version.toString(".")).setAnonymized(true);
            // When Export (with anonymized option)
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Anonymizer anonymizer = NetworkSerDe.write(network, exportOptions, os);
            // Then check anonymized id, controlId != original values
            String anonymizedId = anonymizer.anonymizeString("tc1");
            String anonymizedControlId = anonymizer.anonymizeString("control1");
            assertNotEquals("tc1", anonymizedId);
            assertNotEquals("control1", anonymizedControlId);
            // Then check xml content (contain only anonymized id and controlId)
            String xmlContent = os.toString(StandardCharsets.UTF_8);
            assertTrue(xmlContent.contains("id=\"" + anonymizedId + "\""));
            assertTrue(xmlContent.contains("controlId=\"" + anonymizedControlId + "\""));
            // Then check import without anonymizer
            Network importedNetwork1 = NetworkSerDe.read(new ByteArrayInputStream(os.toByteArray()));
            assertWhenImport(importedNetwork1, anonymizer.anonymizeString("NGEN_NHV1"), anonymizedId, anonymizedControlId);
            // Then check import with anonymizer
            Network importedNetwork2 = NetworkSerDe.read(new ByteArrayInputStream(os.toByteArray()), new ImportOptions(), anonymizer);
            assertWhenImport(importedNetwork2, "NGEN_NHV1", "tc1", "control1");
        });
    }

    private void assertWhenImport(Network importedNetwork, String twtName, String expectedId, String expectedControlId) {
        TwoWindingsTransformer importedTWT = importedNetwork.getTwoWindingsTransformer(twtName);
        assertNotNull(importedTWT);
        CgmesTapChangers importedCgmesTapChangers = importedTWT.getExtension(CgmesTapChangers.class);
        assertNotNull(importedCgmesTapChangers);
        CgmesTapChanger importedCgmesTapChanger = (CgmesTapChanger) importedCgmesTapChangers.getTapChangers().iterator().next();
        assertEquals(expectedId, importedCgmesTapChanger.getId());
        assertEquals(expectedControlId, importedCgmesTapChanger.getControlId());
    }
}
