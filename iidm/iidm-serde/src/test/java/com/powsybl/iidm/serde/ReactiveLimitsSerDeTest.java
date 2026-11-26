/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.test.ReactiveLimitsTestNetworkFactory;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ReactiveLimitsSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("reactiveLimitsRoundTripRef.xml");

        allFormatsRoundTripTest(ReactiveLimitsTestNetworkFactory.create(), "reactiveLimitsRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void roundTripTestWithShape() throws IOException {
        allFormatsRoundTripFromVersionedXmlTest("reactiveLimitsShapeRoundTripRef.xml", CURRENT_IIDM_VERSION);

        allFormatsRoundTripTest(ReactiveLimitsTestNetworkFactory.createWithShape(), "reactiveLimitsShapeRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void exportShapeShouldFailBeforeIidm119() {
        Network network = ReactiveLimitsTestNetworkFactory.createWithShape();
        testForAllPreviousVersions(IidmVersion.V_1_19, version -> {
            ExportOptions options = new ExportOptions().setVersion(version.toString("."));
            Path path = tmpDir.resolve("fail");
            NotImplementedException e = assertThrows(NotImplementedException.class, () -> NetworkSerDe.write(network, options, path));
            assertTrue(e.getMessage().contains("export of this kind of reactive limits is not supported for IIDM versions older than 1.19"));
        });
    }

    @Test
    void importShapeShouldFailBeforeIidm119() throws IOException {
        String xml119;
        try (InputStream is = getVersionedNetworkAsStream("reactiveLimitsShapeRoundTripRef.xml", IidmVersion.V_1_19)) {
            xml119 = new String(is.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
        String xml118 = xml119.replace(IidmVersion.V_1_19.getNamespaceURI(), IidmVersion.V_1_18.getNamespaceURI());
        InputStream is = new ByteArrayInputStream(xml118.getBytes(StandardCharsets.ISO_8859_1));
        PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.read(is));
        assertEquals("generator.reactiveCapabilityShape is not supported for IIDM version 1.18. IIDM version should be >= 1.19", e.getMessage());
    }

    @Test
    void importShouldRepairInvalidPointsForIidm118OrGreaterWithTheOption() {
        ImportOptions options = new ImportOptions()
                .setRepairInvalidReactiveCurveLimits(true);
        testForAllVersionsSince(IidmVersion.V_1_18, version -> assertImportSuccessWithInvertedPoint(version, options));
    }

    @Test
    void importShouldRepairInvalidPointsForIidm117OrLower() {
        ImportOptions options = new ImportOptions();
        assertFalse(options.isRepairInvalidReactiveCurveLimits()); // The option is not enabled
        testForAllPreviousVersions(IidmVersion.V_1_18, version -> assertImportSuccessWithInvertedPoint(version, options));
    }

    private void assertImportSuccessWithInvertedPoint(IidmVersion version, ImportOptions options) {
        InputStream is = getVersionedNetworkAsStream("reactive-limit-inverted-minq-maxq.xiidm", version);
        Network network = NetworkSerDe.read(is, options, null);

        ReactiveCapabilityCurve curve = network.getGenerator("G1")
                .getReactiveLimits(ReactiveCapabilityCurve.class);

        assertEquals(2.0, curve.getMinQ(100.0));
        assertEquals(10.0, curve.getMaxQ(100.0));
    }

    @Test
    void importShouldNotRepairInvalidPointsForIidm118OrGreaterWithoutTheOption() {
        ImportOptions options = new ImportOptions()
                .setRepairInvalidReactiveCurveLimits(false);
        testForAllVersionsSince(IidmVersion.V_1_18, version -> {
            InputStream is = getVersionedNetworkAsStream("reactive-limit-inverted-minq-maxq.xiidm", version);
            ValidationException e = assertThrows(
                ValidationException.class,
                () -> NetworkSerDe.read(is, options, null));

            assertTrue(e.getMessage().contains(
                "Generator 'G1': maximum reactive power is expected to be greater than or equal to minimum reactive power"));
        });
    }
}
