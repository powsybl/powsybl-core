/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.UcteNetwork;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Jérémy LABOUS <jlabous@silicom.fr>
 */
@AutoService(UcteExportPostProcessor.class)
public class UcteExportScriptPostProcessor implements UcteExportPostProcessor {

    public static final String NAME = "ucteExportScript";

    public static final String DEFAULT_SCRIPT_NAME = "ucte-export-post-processor.groovy";

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteExportScriptPostProcessor.class);

    private final Path script;

    public UcteExportScriptPostProcessor() {
        this(PlatformConfig.defaultConfig());
    }

    public UcteExportScriptPostProcessor(PlatformConfig platformConfig) {
        this(getConfiguredScript(platformConfig));
    }

    public UcteExportScriptPostProcessor(Path script) {
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
    public void process(Network network, UcteNetwork ucteNetwork) throws Exception {
        if (Files.exists(script)) {
            LOGGER.debug("Execute ucte export post processor {}", script);
            try (Reader reader = Files.newBufferedReader(script, StandardCharsets.UTF_8)) {
                CompilerConfiguration conf = new CompilerConfiguration();

                Binding binding = new Binding();
                binding.setVariable("network", network);
                binding.setVariable("ucteNetwork", ucteNetwork);

                GroovyShell shell = new GroovyShell(binding, conf);
                shell.evaluate(reader);
            }
        }
    }
}
