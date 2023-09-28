/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.tool;

import com.google.auto.service.AutoService;
import com.powsybl.dynamicsimulation.DynamicSimulation;
import com.powsybl.dynamicsimulation.groovy.DynamicModelGroovyExtension;
import com.powsybl.dynamicsimulation.groovy.EventModelGroovyExtension;
import com.powsybl.dynamicsimulation.groovy.GroovyExtension;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;

/**
 * @author Laurent Issertial <laurent.issertial at rte-france.com>
 */
@AutoService(Tool.class)
public class ListDynamicSimulationModelsTool implements Tool {

    private static final String DYNAMIC_MODELS = "dynamic-models";
    private static final String EVENT_MODELS = "event-models";

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "list-dynamic-simulation-models";
            }

            @Override
            public String getTheme() {
                return "Misc";
            }

            @Override
            public String getDescription() {
                return "Display dynamic simulation models";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(DYNAMIC_MODELS)
                        .desc("display implemented dynamic models")
                        .build());
                options.addOption(Option.builder().longOpt(EVENT_MODELS)
                        .desc("display implemented event models")
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
    public void run(CommandLine line, ToolRunningContext context) {
        boolean noOption = !line.hasOption(DYNAMIC_MODELS) && !line.hasOption(EVENT_MODELS);
        boolean dynamicList = line.hasOption(DYNAMIC_MODELS) || noOption;
        boolean eventList = line.hasOption(EVENT_MODELS) || noOption;

        try (Writer writer = new OutputStreamWriter(context.getOutputStream())) {
            if (dynamicList) {
                printModelsList("Dynamic models", createDynamicModelNamesList(DynamicSimulation.find().getName()), writer);
            }
            if (eventList) {
                printModelsList("Event models", createEventModelNamesList(DynamicSimulation.find().getName()), writer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> createDynamicModelNamesList(String providerName) {
        return GroovyExtension.find(DynamicModelGroovyExtension.class, providerName)
                .stream()
                .flatMap(ex -> ex.getModelNames().stream())
                .toList();
    }

    private List<String> createEventModelNamesList(String providerName) {
        return GroovyExtension.find(EventModelGroovyExtension.class, providerName)
                .stream()
                .flatMap(ex -> ex.getModelNames().stream())
                .toList();
    }

    private void printModelsList(String title, List<String> models, Writer writer) throws IOException {
        if (!StringUtils.isEmpty(title)) {
            writer.write(title + ":" + System.lineSeparator());
        }
        for (String model : models) {
            writer.write("\t" + model + System.lineSeparator());
        }
    }
}
