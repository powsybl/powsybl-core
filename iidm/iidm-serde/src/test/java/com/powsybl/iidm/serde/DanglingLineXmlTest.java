/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class DanglingLineXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        allFormatsRoundTripAllVersionedXmlTest("danglingLine.xml");
    }

    @Test
    void testWithGeneration() throws IOException {
        Network network = DanglingLineNetworkFactory.createWithGeneration();
        network.setCaseDate(ZonedDateTime.parse("2020-07-16T10:08:48.321+02:00"));
        network.getDanglingLine("DL").setProperty("test", "test");
        allFormatsRoundTripTest(network, "danglingLineWithGeneration.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        // backward compatibility checks from version 1.3
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("danglingLineWithGeneration.xml", IidmVersion.V_1_3);

        // check it fails for all versions < 1.3
        testForAllPreviousVersions(IidmVersion.V_1_3, version -> {
            ExportOptions options = new ExportOptions().setVersion(version.toString("."));
            Path path = tmpDir.resolve("fail");
            try {
                NetworkSerDe.write(network, options, path);
                fail();
            } catch (PowsyblException e) {
                assertEquals("danglingLine.generation is not null and not supported for IIDM version " + version.toString(".") + ". IIDM version should be >= 1.3", e.getMessage());
            }
        });

        // check it doesn't fail for all versions < 1.3 if IidmVersionIncompatibilityBehavior is to log error
        var options = new ExportOptions().setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        testWriteXmlAllPreviousVersions(network, options, "danglingLineWithGeneration.xml", IidmVersion.V_1_3);
    }
}
