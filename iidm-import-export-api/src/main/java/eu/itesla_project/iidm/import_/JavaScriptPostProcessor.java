/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.import_;

import com.google.auto.service.AutoService;
import com.google.common.io.CharStreams;
import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.util.Networks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ImportPostProcessor.class)
public class JavaScriptPostProcessor implements ImportPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptPostProcessor.class);

    private static final boolean DEFAULT_PRINT_TO_STD_OUT = true;

    private boolean printToStdOut = DEFAULT_PRINT_TO_STD_OUT;

    @Override
    public String getName() {
        return "javaScript";
    }

    public JavaScriptPostProcessor() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfigIfExists("javaScriptPostProcessor");
        if (config != null) {
            printToStdOut = config.getBooleanProperty("printToStdOut", DEFAULT_PRINT_TO_STD_OUT);
        }
    }

    @Override
    public void process(Network network, ComputationManager computationManager) throws Exception {
        Path js = PlatformConfig.CONFIG_DIR.resolve("import-post-processor.js");
        if (Files.exists(js)) {
            LOGGER.debug("Execute JS post processor {}", js);
            try (Reader reader = Files.newBufferedReader(js)) {
                Networks.runScript(network, reader, printToStdOut ? null : CharStreams.nullWriter());
            }
        }
    }
}
