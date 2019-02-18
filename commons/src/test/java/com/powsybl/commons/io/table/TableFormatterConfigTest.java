/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class TableFormatterConfigTest {

    private void testConfig(TableFormatterConfig config, Locale locale, char separator, String invalidString, boolean printHeader, boolean printTitle) {
        assertEquals(locale, config.getLocale());
        assertEquals(separator, config.getCsvSeparator());
        assertEquals(invalidString, config.getInvalidString());
        assertEquals(printHeader, config.getPrintHeader());
        assertEquals(printTitle, config.getPrintTitle());
    }

    @Test
    public void testConfig() throws IOException {
        TableFormatterConfig config = new TableFormatterConfig();

        testConfig(config, Locale.getDefault(), ';', "inv", true, true);

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
            MapModuleConfig moduleConfig = platformConfig.createModuleConfig("table-formatter");
            moduleConfig.setStringProperty("language", "en-US");
            moduleConfig.setStringProperty("separator", "\t");
            moduleConfig.setStringProperty("invalid-string", "NaN");
            moduleConfig.setStringProperty("print-header", "false");
            moduleConfig.setStringProperty("print-title", "false");

            config = TableFormatterConfig.load(platformConfig);
            testConfig(config, Locale.US, '\t', "NaN", false, false);
        }
    }
}
