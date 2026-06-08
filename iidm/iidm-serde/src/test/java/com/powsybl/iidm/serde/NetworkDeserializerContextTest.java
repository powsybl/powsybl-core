/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;
import com.powsybl.iidm.serde.anonymizer.SimpleAnonymizer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NetworkDeserializerContextTest extends AbstractSerDeTest {

    private static NetworkDeserializerContext newContext(Anonymizer anonymizer, IidmVersion version) {
        return new NetworkDeserializerContext(anonymizer, mock(TreeDataReader.class), new ImportOptions(), version, Map.of());
    }

    @Test
    void xmlReaderContextExceptionTest() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> new NetworkDeserializerContext(null, null));
    }

    @Test
    void testDeanonymizeFromMinimumVersionShouldDeanonymize() {
        // Given
        Anonymizer anonymizer = new SimpleAnonymizer();
        String anonymized = anonymizer.anonymizeString("test");
        NetworkDeserializerContext context = newContext(anonymizer, IidmVersion.V_1_16);
        // When
        String result = context.deanonymizeFromMinimumVersion(anonymized, IidmVersion.V_1_15);
        // Then
        assertEquals("test", result);
    }

    @Test
    void testDeanonymizeFromMinimumVersionShouldNoDeanonymize() {
        // Given
        NetworkDeserializerContext context = newContext(new SimpleAnonymizer(), IidmVersion.V_1_15);
        // When
        String result = context.deanonymizeFromMinimumVersion("test id", IidmVersion.V_1_16);
        // Then
        assertEquals("test id", result);
    }

    @Test
    void testDeanonymizeFromMinimumVersionWhenNoMappingExist() {
        Anonymizer anonymizer = new SimpleAnonymizer(); //empty mapping
        NetworkDeserializerContext context = newContext(anonymizer, IidmVersion.V_1_16);
        PowsyblException exception = assertThrows(PowsyblException.class,
                () -> context.deanonymizeFromMinimumVersion("test", IidmVersion.V_1_15));
        assertEquals("Mapping not found for anonymized string 'test'", exception.getMessage());
    }

}
