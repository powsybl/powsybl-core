/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey@rte-france.com>}
 */
class TieLineSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testTieLineWithAliases() throws IOException {
        allFormatsRoundTripFromVersionedXmlTest("tielineWithAliases.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        // Tests for backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("tielineWithAliases.xml", IidmVersion.V_1_3);
    }

    @Test
    void importNetworkWithTiLineAndAnonymizeShouldSucceed() {
        Network network = EurostagTutorialExample1Factory.createWithTieLinesAndAreas();
        testForAllVersionsSince(IidmVersion.V_1_16, version -> {
            // Export (with Anonymize option)
            ExportOptions exportOptions = new ExportOptions().setVersion(version.toString(".")).setAnonymized(true);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Anonymizer anonymizer = NetworkSerDe.write(network, exportOptions, os);
            // Import (with Anonymize)
            assertDoesNotThrow(() -> NetworkSerDe.read(new ByteArrayInputStream(os.toByteArray()), new ImportOptions(), anonymizer));
        });

        testForAllVersionsBetween(IidmVersion.V_1_10, IidmVersion.V_1_15, version -> {
            // Export (with Anonymize option)
            ExportOptions exportOptions = new ExportOptions().setVersion(version.toString(".")).setAnonymized(true);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Anonymizer anonymizer = NetworkSerDe.write(network, exportOptions, os);
            // Import (with Anonymize)
            assertDoesNotThrow(() -> NetworkSerDe.read(new ByteArrayInputStream(os.toByteArray()), new ImportOptions(), anonymizer));
        });
    }

}
