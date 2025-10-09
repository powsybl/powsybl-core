/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tools;

import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.NetworkFactoryMock;
import com.powsybl.tools.Tool;
import com.powsybl.tools.test.AbstractToolTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ConversionToolTest extends AbstractToolTest {

    private PlatformConfig platformConfig;

    private ConversionTool createConversionTool() {
        return new ConversionTool() {

            @Override
            protected ImportConfig createImportConfig() {
                return ImportConfig.load(platformConfig);
            }

            @Override
            protected NetworkFactory createNetworkFactory() {
                return new NetworkFactoryMock();
            }
        };
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/import-parameters.xml")), fileSystem.getPath("/import-parameters.xml"));
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/export-parameters.properties")), fileSystem.getPath("/export-parameters.properties"));
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
        assertCommand(tool.getCommand(), "convert-network", 7, 3);
        assertOption(tool.getCommand().getOptions(), "input-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-format", true, true);
        assertOption(tool.getCommand().getOptions(), "import-parameters", false, true);
        assertOption(tool.getCommand().getOptions(), "I", false, true);
        assertOption(tool.getCommand().getOptions(), "import-parameters", false, true);
        assertOption(tool.getCommand().getOptions(), "E", false, true);
    }

    @Test
    void testConversion() {
        String[] commandLine = new String[] {
            "convert-network", "--input-file", "/input.txt",
            "--import-parameters", "/import-parameters.xml", "-Iparam1=value1",
            "--output-format", "OUT", "--output-file", "/output.txt",
            "--export-parameters", "/export-parameters.properties", "-Eparam2=value2"
        };
        assertCommandSuccessful(commandLine, "");
    }
}
