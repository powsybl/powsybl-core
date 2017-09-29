/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.auto.service.AutoService;
import com.google.common.io.CharStreams;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.Networks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ImportPostProcessor.class)
public class JavaScriptPostProcessor implements ImportPostProcessor {

    public static final String NAME = "javaScript";

    public static final String SCRIPT_NAME = "import-post-processor.js";

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptPostProcessor.class);

    private static final boolean DEFAULT_PRINT_TO_STD_OUT = true;

    private boolean printToStdOut;

    private Path script;

    @Override
    public String getName() {
        return NAME;
    }

    public JavaScriptPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public JavaScriptPostProcessor(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        Path defaultScript = platformConfig.getConfigDir().resolve(SCRIPT_NAME);

        ModuleConfig config = platformConfig.getModuleConfigIfExists("javaScriptPostProcessor");
        if (config != null) {
            printToStdOut = config.getBooleanProperty("printToStdOut", DEFAULT_PRINT_TO_STD_OUT);
            script = config.getPathProperty("script", defaultScript);
        } else {
            printToStdOut = DEFAULT_PRINT_TO_STD_OUT;
            script = defaultScript;
        }
    }

    @Override
    public void process(Network network, ComputationManager computationManager) throws Exception {
        if (Files.exists(script)) {
            LOGGER.debug("Execute JS post processor {}", script);
            try (Reader reader = Files.newBufferedReader(script)) {
                Networks.runScript(network, reader, printToStdOut ? null : CharStreams.nullWriter());
            }
        }
    }
}
