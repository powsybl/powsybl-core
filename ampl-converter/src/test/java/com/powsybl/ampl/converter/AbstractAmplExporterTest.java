/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Pierre ARVY {@literal <pierre.arvy at artelys.com>}
 */
abstract class AbstractAmplExporterTest extends AbstractSerDeTest {

    MemDataSource dataSource;
    AmplExporter exporter;
    Properties properties;

    protected void assertEqualsToRef(MemDataSource dataSource, String suffix, String refFileName) throws IOException {
        try (InputStream actual = new ByteArrayInputStream(dataSource.getData(suffix, "txt"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + refFileName), actual);
        }
    }

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        dataSource = new MemDataSource();
        exporter = new AmplExporter();
        properties = new Properties();
    }

}
