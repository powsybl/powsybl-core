/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools.autocompletion;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.EscapeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
 */
public class BashCompletionGenerator {

    private static Logger LOGGER = LoggerFactory.getLogger(BashCompletionGenerator.class);

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final VelocityEngine ve;

    public BashCompletionGenerator() {
        ve = new VelocityEngine();
        ve.setProperty(VelocityEngine.RESOURCE_LOADER, "class");
        ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.init();
    }

    private static VelocityContext createContext(String toolName) {
        VelocityContext context = new VelocityContext();
        context.put("toolName", toolName);
        context.put("optionPrefix", "--");
        context.put("esc", new EscapeTool());
        context.put("util", new TemplateUtil());
        return context;
    }

    public void generateCommands(String toolName, List<BashCommand> commands, Writer writer) {
        Template t = ve.getTemplate("/com/powsybl/tools/autocompletion/multiple_commands_completion.sh.vm", CHARSET.toString());
        VelocityContext context = createContext(toolName);
        context.put("commands", commands);
        t.merge(context, writer);
    }

    public void generateCommands(String toolName, List<BashCommand> commands, Path outputFile) throws IOException {
        LOGGER.info("Generating {}", outputFile);
        try (Writer writer = Files.newBufferedWriter(outputFile, CHARSET)) {
            generateCommands(toolName, commands, writer);
        }
    }

    public void generateOptions(String toolName, List<BashOption> options, Writer writer) {
        Template t = ve.getTemplate("/com/powsybl/tools/autocompletion/completion.sh.vm", CHARSET.toString());
        VelocityContext context = createContext(toolName);
        context.put("options", options);
        t.merge(context, writer);
    }

    public void generateOptions(String toolName, List<BashOption> options, Path outputFile) throws IOException {
        LOGGER.info("Generating {}", outputFile);
        try (Writer writer = Files.newBufferedWriter(outputFile, CHARSET)) {
            generateOptions(toolName, options, writer);
        }
    }

}
