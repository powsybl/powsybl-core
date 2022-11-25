/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.model;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class NamespaceReaderTest {

    @Test
    public void base() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/empty_cim16_EQ_with_explicitBase.xml")) {
            assertEquals("http://example.com", NamespaceReader.base(is));
        }
    }

    @Test
    public void baseNull() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/empty_cim16_EQ.xml")) {
            assertNull(NamespaceReader.base(is));
        }
    }
}
