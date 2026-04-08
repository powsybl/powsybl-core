/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools.autocompletion;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
@AutoService(Tool.class)
public class BashCompletionTool implements Tool {

    private static final String OUTPUT_FILE = "output-file";

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "generate-completion-script";
            }

            @Override
            public String getTheme() {
                return "Misc";
            }

            @Override
            public String getDescription() {
                return "Generates a bash autocompletion script";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(OUTPUT_FILE)
                        .desc("the generated autocompletion script")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        ToolOptions options = new ToolOptions(line, context);
        Path outputPath = options.getPath(OUTPUT_FILE).orElseThrow(IllegalStateException::new);

        List<Tool> tools = new ServiceLoaderCache<>(Tool.class).getServices();
        generateCompletionScript(tools, outputPath);
    }

    public void generateCompletionScript(List<Tool> tools, Path outputPath) throws IOException {
        Map<String, Options> itoolsCommands = tools.stream()
                .map(Tool::getCommand)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Command::getName))
                .collect(ImmutableMap.toImmutableMap(Command::getName, Command::getOptions));
        List<BashCommand> commands = BashCommand.convert(itoolsCommands);

        //Try to identify file, dir, and host options, and defaults to files
        new OptionTypeMapper()
                .setDefaultType(OptionType.FILE)
                .addOptionNameMapping(".*file", OptionType.FILE)
                .addArgNameMapping("FILE", OptionType.FILE)
                .addOptionNameMapping(".*dir", OptionType.DIRECTORY)
                .addArgNameMapping("DIR", OptionType.DIRECTORY)
                .addArgNameMapping("HOST", OptionType.HOSTNAME)
                .map(commands);
        BashCompletionGenerator generator = new StringTemplateBashCompletionGenerator();
        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            generator.generateCommands("itools", commands, writer);
        }
    }

}
