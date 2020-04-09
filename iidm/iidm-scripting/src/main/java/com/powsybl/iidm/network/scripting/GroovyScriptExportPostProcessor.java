/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.export.ExportPostProcessor;
import com.powsybl.iidm.network.Network;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Jérémy LABOUS <jlabous@silicom.fr>
 */
@AutoService(ExportPostProcessor.class)
public class GroovyScriptExportPostProcessor implements ExportPostProcessor {

    public static final String NAME = "exportScript";

    public static final String DEFAULT_SCRIPT_NAME = "export-post-processor.groovy";

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyScriptExportPostProcessor.class);

    private final Path script;

    public GroovyScriptExportPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public GroovyScriptExportPostProcessor(PlatformConfig platformConfig) {
        this(getConfiguredScript(platformConfig));
    }

    public GroovyScriptExportPostProcessor(Path script) {
        this.script = Objects.requireNonNull(script);
    }

    private static Path getConfiguredScript(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        Path defaultScript = platformConfig.getConfigDir().resolve(DEFAULT_SCRIPT_NAME);

        return platformConfig.getOptionalModuleConfig("ucte-export-post-processor")
            .map(config -> config.getPathProperty("script", defaultScript))
            .orElse(defaultScript);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void process(Network network, String format, Object nativeModel, ComputationManager computationManager) {
        if (Files.exists(script)) {
            LOGGER.debug("Execute export post processor {}", script);
            try (Reader reader = Files.newBufferedReader(script, StandardCharsets.UTF_8)) {
                CompilerConfiguration conf = new CompilerConfiguration();

                Binding binding = new Binding();
                binding.setVariable("network", network);
                binding.setVariable("format", format);
                binding.setVariable("nativeModel", nativeModel);
                binding.setVariable("computationManager", computationManager);

                GroovyShell shell = new GroovyShell(binding, conf);
                shell.evaluate(reader);
            } catch (IOException ioe) {
                throw new PowsyblException("Error during post processing", ioe);
            }
        }
    }
}
