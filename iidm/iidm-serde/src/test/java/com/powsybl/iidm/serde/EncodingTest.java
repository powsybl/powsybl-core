/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author imagrid team {@literal <rte-mco-imagrid at rte-france.com>}
 */
class EncodingTest {

    @Test
    void testEncodingISO88591ToISO88591() {
        try {
            Network network = NetworkSerDe.read(getClass().getResourceAsStream("/encoding/network.xml"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ExportOptions options = new ExportOptions();
            options.setCharset(StandardCharsets.ISO_8859_1);
            NetworkSerDe.write(network, options, baos);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            ComparisonUtils.compareXml(getClass().getResourceAsStream("/encoding/network.xml"), is);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testEncodingISO88591ToUTF8() {
        try {
            Network network = NetworkSerDe.read(getClass().getResourceAsStream("/encoding/network.xml"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ExportOptions options = new ExportOptions();
            options.setCharset(StandardCharsets.UTF_8);
            NetworkSerDe.write(network, options, baos);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            ComparisonUtils.compareXml(getClass().getResourceAsStream("/encoding/network.xml"), is);
        } catch (Exception e) {
            fail();
        }
    }
}
