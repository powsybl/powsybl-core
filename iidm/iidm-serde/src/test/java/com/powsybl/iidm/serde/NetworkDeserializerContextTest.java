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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NetworkDeserializerContextTest extends AbstractSerDeTest {

    private static NetworkDeserializerContext newContext(Anonymizer anonymizer, TreeDataReader reader, IidmVersion version) {
        return new NetworkDeserializerContext(anonymizer, reader, new ImportOptions(), version, Map.of());
    }

    @Test
    void xmlReaderContextExceptionTest() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> new NetworkDeserializerContext(null, null));
    }

    @Test
    void testDeanonymizeStringOrDefaultShouldSucceed() {
        // Given
        TreeDataReader reader = mock(TreeDataReader.class);
        Anonymizer anonymizer = new SimpleAnonymizer();
        String anonymized = anonymizer.anonymizeString("test");
        when(reader.readStringAttribute(anyString())).thenReturn(anonymized);
        NetworkDeserializerContext context = newContext(anonymizer, reader, IidmVersion.V_1_16);
        // When
        String result = context.deanonymizeStringOrDefault(anonymized, IidmVersion.V_1_15);
        // Then
        assertEquals("test", result);
    }

    @Test
    void testDeanonymizeStringOrDefaultShouldThrowException() {
        // Given
        TreeDataReader reader = mock(TreeDataReader.class);
        when(reader.readStringAttribute(anyString())).thenReturn("test");
        Anonymizer anonymizer = new SimpleAnonymizer(); //empty mapping
        NetworkDeserializerContext context = newContext(anonymizer, reader, IidmVersion.V_1_16);
        // When
        PowsyblException exception = assertThrows(PowsyblException.class,
                () -> context.deanonymizeStringOrDefault("test", IidmVersion.V_1_15));
        // Then
        assertEquals("Mapping not found for anonymized string 'test'", exception.getMessage());
    }

    @Test
    void testDeanonymizeOrDefaultWhenMappingNotFound() {
        // Given
        TreeDataReader reader = mock(TreeDataReader.class);
        when(reader.readStringAttribute(anyString())).thenReturn("test value");
        NetworkDeserializerContext context = newContext(new SimpleAnonymizer(), reader, IidmVersion.V_1_15);
        // When
        String result = context.deanonymizeStringOrDefault("test id", IidmVersion.V_1_15);
        // Then
        assertEquals("test value", result);
    }

    @Test
    void testDeanonymizeContentOrDefaultShouldSucceed() {
        // Given
        TreeDataReader reader = mock(TreeDataReader.class);
        Anonymizer anonymizer = new SimpleAnonymizer();
        String anonymized = anonymizer.anonymizeString("content");
        when(reader.readContent()).thenReturn(anonymized);
        NetworkDeserializerContext context = newContext(anonymizer, reader, IidmVersion.V_1_16);
        // When
        String result = context.deanonymizeContentOrDefault(IidmVersion.V_1_15);
        // Then
        assertEquals("content", result);
    }

}
