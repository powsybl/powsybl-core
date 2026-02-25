/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.IidmVersion;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CgmesBoundaryNodeSerDeTest extends AbstractCgmesExtensionTest {

    @Test
    void testTieLine() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        network.getTieLine("NHV1_NHV2_1").newExtension(CgmesLineBoundaryNodeAdder.class)
                .setHvdc(true)
                .setLineEnergyIdentificationCodeEic("EIC_CODE")
                .add();
        network.getTieLine("NHV1_NHV2_2").newExtension(CgmesLineBoundaryNodeAdder.class)
                .setHvdc(true)
                .add();
        allFormatsRoundTripTest(network, "/eurostag_cgmes_line_boundary_node.xml");
    }

    @Test
    void testBoundaryLine() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        var tl = network.getTieLine("NHV1_NHV2_1");
        tl.getBoundaryLine1().newExtension(CgmesBoundaryLineBoundaryNodeAdder.class)
                .setHvdc(false)
                .setLineEnergyIdentificationCodeEic("EIC_CODE")
                .add();
        tl.getBoundaryLine2().newExtension(CgmesBoundaryLineBoundaryNodeAdder.class)
                .setHvdc(false)
                .add();
        tl.remove();

        allFormatsRoundTripTest(network, "/eurostag_cgmes_boundary_line_boundary_node.xml");
    }

    @Test
    void testLegacyDanglingLine() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        var tl = network.getTieLine("NHV1_NHV2_1");
        tl.getBoundaryLine1().newExtension(CgmesBoundaryLineBoundaryNodeAdder.class)
                .setHvdc(false)
                .setLineEnergyIdentificationCodeEic("EIC_CODE")
                .add();
        tl.getBoundaryLine2().newExtension(CgmesBoundaryLineBoundaryNodeAdder.class)
                .setHvdc(false)
                .add();
        tl.remove();

        allFormatsRoundTripTestVersioned(network, "/eurostag_cgmes_dangling_line_boundary_node.xml", IidmVersion.V_1_15);
    }

    @Test
    void testAnonymizedCgmesBoundaryNode() throws IOException {
        //Given
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine tieLine = network.getTieLine("NHV1_NHV2_1");
        tieLine.newExtension(CgmesLineBoundaryNodeAdder.class)
                .setHvdc(true)
                .setLineEnergyIdentificationCodeEic("EIC_CODE")
                .add();
        CgmesLineBoundaryNode lineBoundaryNode = tieLine.getExtension(CgmesLineBoundaryNode.class);
        assertNotNull(lineBoundaryNode);

        // When Export (with anonymized option)
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Anonymizer anonymizer = NetworkSerDe.write(network, new ExportOptions().setAnonymized(true), os);
        // Then check anonymized code != original code
        String anonymizedLineEnergyIdentificationCodeEic = anonymizer.anonymizeString("EIC_CODE");
        assertNotEquals("EIC_CODE", anonymizedLineEnergyIdentificationCodeEic);
        // Then check xml content (contain only anonymized code)
        byte[] anonymizedXml = os.toByteArray();
        String xmlContent = new String(anonymizedXml, StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("lineEnergyIdentificationCodeEic=\"" + anonymizedLineEnergyIdentificationCodeEic + "\""));
        // Then import without anonymizer
        try (ByteArrayInputStream is = new ByteArrayInputStream(anonymizedXml)) {
            Network importedNetwork = NetworkSerDe.read(is);
            // get TieLine using anonymized ID.
            TieLine importedTieLine = importedNetwork.getTieLine(anonymizer.anonymizeString("NHV1_NHV2_1"));
            assertNotNull(importedTieLine);
            CgmesLineBoundaryNode importedCgmesLineBoundaryNode = importedTieLine.getExtension(CgmesLineBoundaryNode.class);
            assertNotNull(importedCgmesLineBoundaryNode);
            assertTrue(importedCgmesLineBoundaryNode.getLineEnergyIdentificationCodeEic().isPresent());
            assertEquals(anonymizedLineEnergyIdentificationCodeEic, importedCgmesLineBoundaryNode.getLineEnergyIdentificationCodeEic().get());
        }
    }

    @Test
    void testAnonymizedCgmesBoundaryLineBoundaryNode() throws IOException {
        //Given
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine tieLine = network.getTieLine("NHV1_NHV2_1");
        tieLine.getBoundaryLine1().newExtension(CgmesBoundaryLineBoundaryNodeAdder.class)
                .setHvdc(false)
                .setLineEnergyIdentificationCodeEic("EIC_CODE")
                .add();
        CgmesBoundaryLineBoundaryNode danglingLineBoundaryNode = tieLine.getBoundaryLine1().getExtension(CgmesBoundaryLineBoundaryNode.class);
        assertNotNull(danglingLineBoundaryNode);
        // When Export (with anonymized option)
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Anonymizer anonymizer = NetworkSerDe.write(network, new ExportOptions().setAnonymized(true), os);
        // Then check anonymized id != original ids
        String anonymizedLineEnergyIdentificationCodeEic = anonymizer.anonymizeString("EIC_CODE");
        assertNotEquals("EIC_CODE", anonymizedLineEnergyIdentificationCodeEic);
        // Then check xml content (contain only anonymized code)
        byte[] anonymizedXml = os.toByteArray();
        String xmlContent = new String(anonymizedXml, StandardCharsets.UTF_8);
        assertTrue(xmlContent.contains("lineEnergyIdentificationCodeEic=\"" + anonymizedLineEnergyIdentificationCodeEic + "\""));
        // Then import to check that is anonymized
        try (ByteArrayInputStream is = new ByteArrayInputStream(anonymizedXml)) {
            Network importedNetwork = NetworkSerDe.read(is);
            // get TieLine using her anonymized ID.
            TieLine importedTieLine = importedNetwork.getTieLine(anonymizer.anonymizeString("NHV1_NHV2_1"));
            assertNotNull(importedTieLine);
            CgmesBoundaryLineBoundaryNode importedDanglingLineBoundaryNode = importedTieLine.getBoundaryLine1().getExtension(CgmesBoundaryLineBoundaryNode.class);
            assertNotNull(importedDanglingLineBoundaryNode);
            assertTrue(importedDanglingLineBoundaryNode.getLineEnergyIdentificationCodeEic().isPresent());
            assertEquals(anonymizedLineEnergyIdentificationCodeEic, importedDanglingLineBoundaryNode.getLineEnergyIdentificationCodeEic().get());
        }
    }
}
