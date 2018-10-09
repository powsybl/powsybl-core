/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.plugins.Plugin;
import com.powsybl.commons.plugins.Plugins;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
@AutoService(Tool.class)
public class PluginsInfoTool implements Tool {

    private static final Command COMMAND = new Command() {
        @Override
        public String getName() {
            return "plugins-info";
        }

        @Override
        public String getTheme() {
            return "Misc";
        }

        @Override
        public String getDescription() {
            return "list the available plugins";
        }

        @Override
        public Options getOptions() {
            return new Options();
        }

        @Override
        public String getUsageFooter() {
            return null;
        }
    };

    private final PlatformConfig platformConfig;

    public PluginsInfoTool() {
        this(PlatformConfig.defaultConfig());
    }

    public PluginsInfoTool(PlatformConfig platformConfig) {
        this.platformConfig = Objects.requireNonNull(platformConfig);
    }

    @Override
    public Command getCommand() {
        return COMMAND;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Collection<Plugin> plugins = Plugins.getPlugins();
        Writer writer = new OutputStreamWriter(context.getOutputStream());
        AsciiTableFormatterFactory asciiTableFormatterFactory = new AsciiTableFormatterFactory();

        try (TableFormatter formatter = asciiTableFormatterFactory.create(writer, "Plugins", new TableFormatterConfig(),
                new Column("plugin type name"), new Column("available plugins IDs"))) {
            plugins.stream().forEach(p -> {
                try {
                    formatter.writeCell(p.getPluginInfo().getPluginName());
                    formatter.writeCell(Plugins.getPluginImplementationsIds(p).stream().collect(Collectors.joining(", ")).toString());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

}
