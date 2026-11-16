/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.json.JsonUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class DataSchemeTest {

    @Test
    void test() throws IOException {
        DataScheme scheme = new DataScheme();
        scheme.addClass(DataClass.init("ElmFoo")
                .addAttribute(new DataAttribute("i", DataAttributeType.INTEGER)));
        scheme.addClass(DataClass.init("ElmBar")
                .addAttribute(new DataAttribute("d", DataAttributeType.DOUBLE)));
        assertTrue(scheme.classExists("ElmFoo"));
        assertFalse(scheme.classExists("ElmBaz"));
        assertEquals("ElmFoo", scheme.getClassByName("ElmFoo").getName());

        try (StringWriter writer = new StringWriter()) {
            JsonUtil.writeJson(writer, generator -> {
                try {
                    generator.writeStartObject();
                    scheme.writeJson(generator);
                    generator.writeEndObject();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            assertEquals(new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/scheme.json")), StandardCharsets.UTF_8),
                         writer.toString());
        }
    }
}
