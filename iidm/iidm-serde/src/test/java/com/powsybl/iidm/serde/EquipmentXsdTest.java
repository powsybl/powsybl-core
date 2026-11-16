/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class EquipmentXsdTest extends AbstractIidmSerDeTest {

    @Test
    void xsdConsistenceTest() {
        testForAllVersionsSince(IidmVersion.V_1_12, this::checkXsdConsistence);
    }

    private void checkXsdConsistence(IidmVersion iidmVersion) {
        try (InputStream is = getClass().getResourceAsStream(getXsdPath("iidm_equipment", iidmVersion))) {
            // Load the "iidm_equipment" xsd
            String equipmentXsd = new String(ByteStreams.toByteArray(is));
            // Change the namespaces to match the ones in the "iidm" xsd
            String modifiedEquipmentXsd = equipmentXsd.replaceAll("schema/iidm/equipment/", "schema/iidm/");
            // Compare the modified "iidm_equipment" xsd with the "iidm" xsd
            try (InputStream equipmentIs = IOUtils.toInputStream(modifiedEquipmentXsd, "UTF-8");
                 InputStream iidmIs = getClass().getResourceAsStream(getXsdPath("iidm", iidmVersion))) {
                Diff myDiffs = DiffBuilder.compare(iidmIs).withTest(equipmentIs).ignoreWhitespace().ignoreComments().build();
                // For each difference, check that it is a "use" attribute change from "required" to "optional" or the reverse.
                for (Difference diff : myDiffs.getDifferences()) {
                    Assertions.assertTrue(isAcceptable(diff), diff.toString());
                }
            }
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        }
    }

    private static boolean isAcceptable(Difference diff) {
        // Check that the difference is a "use" attribute change from "required" to "optional" or the reverse.
        var c = diff.getComparison();
        Node controlNode = c.getControlDetails().getTarget();
        Node targetNode = c.getTestDetails().getTarget();
        return c.getType() == ComparisonType.ATTR_VALUE
                && "use".equals(controlNode.getLocalName())
                && "use".equals(targetNode.getLocalName())
                && ("required".equals(controlNode.getNodeValue()) && "optional".equals(targetNode.getNodeValue())
                    || "optional".equals(controlNode.getNodeValue()) && "required".equals(targetNode.getNodeValue()));
    }

    private static String getXsdPath(String prefix, IidmVersion iidmVersion) {
        return String.format("/xsd/%s_V%s.xsd", prefix, iidmVersion.toString("_"));
    }
}
