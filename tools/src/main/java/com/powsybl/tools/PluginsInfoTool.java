/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.plugins.PluginInfo;
import com.powsybl.commons.plugins.Plugins;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.it>}
 */
@AutoService(Tool.class)
public class PluginsInfoTool implements Tool {

    private static final int MAX_IDS_LENGTH = 100;

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
            return "List the available plugins";
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

    @Override
    public Command getCommand() {
        return COMMAND;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Collection<PluginInfo> pluginInfos = Plugins.getPluginInfos();
        Writer writer = new OutputStreamWriter(context.getOutputStream());
        AsciiTableFormatterFactory asciiTableFormatterFactory = new AsciiTableFormatterFactory();

        try (TableFormatter formatter = asciiTableFormatterFactory.create(writer, "Plugins", new TableFormatterConfig(),
                new Column("Plugin type name"),
                new Column("Available plugin IDs"))) {

            for (PluginInfo p : pluginInfos) {
                List<String> ids = Plugins.getPluginImplementationsIds(p);
                String strIds = String.join(", ", ids);
                if (strIds.length() > MAX_IDS_LENGTH) {
                    formatter.writeCell(p.getPluginName());
                    formatter.writeCell(ids.get(0));
                    for (int i = 1; i < ids.size(); ++i) {
                        formatter.writeEmptyCell();
                        formatter.writeCell(ids.get(i));
                    }
                } else {
                    formatter.writeCell(p.getPluginName());
                    formatter.writeCell(strIds);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
