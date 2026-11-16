/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;
import com.powsybl.iidm.serde.anonymizer.SimpleAnonymizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class NetworkSerializerContextTest extends AbstractSerDeTest {

    private static NetworkSerializerContext newContext(Anonymizer anonymizer, IidmVersion version) {
        return new NetworkSerializerContext(anonymizer, mock(TreeDataWriter.class), new ExportOptions(), null, version, true);
    }

    @Test
    void testAnonymizeFromMinimumVersionShouldAnonymize() {
        // Given
        NetworkSerializerContext context = newContext(new SimpleAnonymizer(), IidmVersion.V_1_16);
        // When
        String result = context.anonymizeFromMinimumVersion("test", IidmVersion.V_1_16);
        // Then
        assertNotEquals("test", result);
    }

    @Test
    void testAnonymizeFromMinimumVersionShouldNoAnonymize() {
        // Given
        NetworkSerializerContext context = newContext(new SimpleAnonymizer(), IidmVersion.V_1_15);
        // When
        String result = context.anonymizeFromMinimumVersion("test", IidmVersion.V_1_16);
        // Then
        assertEquals("test", result);
    }
}
