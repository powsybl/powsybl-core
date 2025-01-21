/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.iidm.serde.LoadMockSerDe;
import com.powsybl.iidm.serde.extensions.util.NetworkSourceExtensionSerDe;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class SerializationNamesTest {

    public static final String LOAD_MOCK = "loadMock";
    public static final String LOAD_ELEMENT_MOCK = "loadElementMock";

    @Test
    void unversionedExtension() {
        AbstractExtensionSerDe<?, ?> serde = new NetworkSourceExtensionSerDe();
        assertEquals(serde.getExtensionName(), serde.getSerializationName(serde.getVersion()));
        Set<String> serializationNames = serde.getSerializationNames();
        assertEquals(1, serializationNames.size());
        assertEquals(serde.getExtensionName(), serializationNames.iterator().next());
    }

    @Test
    void getSerializationName() {
        LoadMockSerDe serde = new LoadMockSerDe();
        assertEquals(LOAD_MOCK, serde.getExtensionName());
        assertEquals(LOAD_MOCK, serde.getSerializationName(serde.getVersion()));
        assertEquals(LOAD_ELEMENT_MOCK, serde.getSerializationName("0.1"));
        Set<String> serializationNames = serde.getSerializationNames();
        assertEquals(2, serializationNames.size());
        assertTrue(serializationNames.containsAll(Set.of(LOAD_MOCK, LOAD_ELEMENT_MOCK)));
    }

    //TODO namespacePrefixes by serializationName or version
}
