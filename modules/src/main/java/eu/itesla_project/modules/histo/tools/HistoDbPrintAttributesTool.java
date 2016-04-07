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
import eu.itesla_project.modules.histo.*;
import eu.itesla_project.modules.offline.OfflineConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class HistoDbPrintAttributesTool implements Tool {

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "histodb-print-attributes";
            }

            @Override
            public String getTheme() {
                return "Histo DB";
            }

            @Override
            public String getDescription() {
                return "print a list of attributes of the Histo DB";
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
                options.addOption(Option.builder().longOpt("attributes")
                        .desc("attribute list separated by a coma")
                        .hasArg()
                        .required()
                        .argName("ATTR1,ATTR2,...")
                        .build());
                options.addOption(Option.builder().longOpt("statistics")
                        .desc("print basic statistics")
                        .build());
                options.addOption(Option.builder().longOpt("add-datetime")
                        .desc("add date & time atrribute")
                        .build());
                options.addOption(Option.builder().longOpt("format")
                        .desc("format output in a table")
                        .build());
                options.addOption(Option.builder().longOpt("horizon")
                        .desc("SN/DACF (default SN)")
                        .hasArg()
                        .argName("HORIZON")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return "Where HORIZON is one of " + Arrays.toString(HistoDbHorizon.values());
            }

        };
    }

    private static Reader createReader(InputStream is, boolean zipped) throws IOException {
        return zipped ? new InputStreamReader(new GZIPInputStream(is), StandardCharsets.UTF_8)
                      : new InputStreamReader(is, StandardCharsets.UTF_8);
    }

    private static void format(InputStream is, boolean zipped) throws IOException {
        Table table;
        CsvPreference prefs = new CsvPreference.Builder('"', ',', "\r\n").build();
        try (ICsvListReader reader = new CsvListReader(createReader(is, zipped), prefs)) {
            String[] header = reader.getHeader(true);
            table = new Table(header.length, BorderStyle.CLASSIC_WIDE);
            for (String cell : header) {
                table.addCell(cell);
            }
            List<String> row;
            while( (row = reader.read()) != null ) {
                for (int i = 0; i < row.size(); i++) {
                    String cell = row.get(i);
                    if (header[i].equals(HistoDbMetaAttributeId.datetime.toString())) {
                        DateTime datetime = new DateTime(Long.parseLong(cell) * 1000L);
                        table.addCell(datetime.toString());
                    } else {
                        table.addCell(cell);
                    }
                }
            }
        }
        System.out.println(table.render());
    }

    @Override
    public void run(CommandLine line) throws Exception {
        OfflineConfig config = OfflineConfig.load();
        try (HistoDbClient histoDbClient = config.getHistoDbClientFactoryClass().newInstance().create()) {
            boolean statistics = line.hasOption("statistics");
            Set<HistoDbAttributeId> attrs = new LinkedHashSet<>();
            if (!statistics && line.hasOption("add-datetime")) {
                attrs.add(HistoDbMetaAttributeId.datetime);
            }
            for (String str : line.getOptionValue("attributes").split(",")) {
                attrs.add(HistoDbAttributeIdParser.parse(str));
            }
            Interval interval = Interval.parse(line.getOptionValue("interval"));
            boolean format = line.hasOption("format");
            HistoDbHorizon horizon = HistoDbHorizon.SN;
            if (line.hasOption("horizon")) {
                horizon = HistoDbHorizon.valueOf(line.getOptionValue("horizon"));
            }
            boolean async = false;
            boolean zipped = false;
            InputStream is = histoDbClient.queryCsv(statistics ? HistoQueryType.stats : HistoQueryType.data, attrs, interval, horizon, zipped, async);
            if (format) {
                format(is, zipped);
            } else {
                try (Reader reader = createReader(is, zipped)) {
                    CharStreams.copy(reader, System.out);
                }
            }
        }
    }

}
