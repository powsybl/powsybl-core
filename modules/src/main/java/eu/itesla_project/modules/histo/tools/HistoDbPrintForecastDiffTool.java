/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo.tools;

import com.google.auto.service.AutoService;
import com.google.common.io.CharStreams;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.modules.histo.*;
import eu.itesla_project.modules.offline.OfflineConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.Interval;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.EnumSet;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class HistoDbPrintForecastDiffTool implements Tool {

    private static final Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "histodb-print-forecastdiff";
        }

        @Override
        public String getTheme() {
            return "Histo DB";
        }

        @Override
        public String getDescription() {
            return "print forecast diff for active power of all loads and generators of the Histo DB";
        }

        @Override
        @SuppressWarnings("static-access")
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder().longOpt("interval")
                    .desc("time interval (example 2013-01-01T00:00:00+01:00/2013-01-31T23:59:00+01:00)")
                    .hasArg()
                    .required()
                    .argName("DATE1/DATE2")
                    .build());
            return options;
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
    public void run(CommandLine line) throws Exception {
        OfflineConfig config = OfflineConfig.load();
        try (HistoDbClient histoDbClient = config.getHistoDbClientFactoryClass().newInstance().create()) {
            Interval interval = Interval.parse(line.getOptionValue("interval"));
            try (Reader reader = new InputStreamReader(histoDbClient.queryCsv(
                    HistoQueryType.forecastDiff,
                    EnumSet.allOf(Country.class),
                    EnumSet.of(HistoDbEquip.loads, HistoDbEquip.gen),
                    EnumSet.of(HistoDbAttr.P),
                    interval,
                    HistoDbHorizon.DACF,
                    false,
                    false))) {
                CharStreams.copy(reader, System.out);
            }
        }
    }

}
