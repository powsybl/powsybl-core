/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.tools;

import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.Tool;
import com.powsybl.iidm.import_.ImportConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class ConversionToolTest extends AbstractToolTest {

    private PlatformConfig platformConfig;

    private ConversionTool createConversionTool() {
        return new ConversionTool() {

            @Override
            protected ImportConfig createImportConfig() {
                return ImportConfig.load(platformConfig);
            }
        };
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Files.copy(getClass().getResourceAsStream("/import-parameters.xml"), fileSystem.getPath("/import-parameters.xml"));
        Files.copy(getClass().getResourceAsStream("/export-parameters.properties"), fileSystem.getPath("/export-parameters.properties"));
        createFile("/input.txt", "");

        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("import");
        moduleConfig.setStringListProperty("postProcessors", Collections.emptyList());

        this.platformConfig = platformConfig;
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singletonList(createConversionTool());
    }

    @Override
    public void assertCommand() {
        Tool tool = createConversionTool();
        assertCommand(tool.getCommand(), "convert-network", 8, 3);
        assertOption(tool.getCommand().getOptions(), "input-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-format", true, true);
        assertOption(tool.getCommand().getOptions(), "groovy-script", false, true);
        assertOption(tool.getCommand().getOptions(), "import-parameters", false, true);
        assertOption(tool.getCommand().getOptions(), "I", false, true);
        assertOption(tool.getCommand().getOptions(), "import-parameters", false, true);
        assertOption(tool.getCommand().getOptions(), "E", false, true);
    }

    @Test
    public void testConversion() throws IOException {
        String[] commandLine = new String[] {
            "convert-network", "--input-file", "/input.txt",
            "--import-parameters", "/import-parameters.xml", "-Iparam1=value1",
            "--output-format", "OUT", "--output-file", "/output.txt",
            "--export-parameters", "/export-parameters.properties", "-Eparam2=value2"
        };
        assertCommand(commandLine, CommandLineTools.COMMAND_OK_STATUS, "", "");
    }
}
