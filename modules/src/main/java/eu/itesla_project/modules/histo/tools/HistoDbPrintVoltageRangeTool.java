/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo.tools;

import com.google.auto.service.AutoService;
import com.google.common.collect.Range;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.modules.histo.*;
import eu.itesla_project.modules.offline.OfflineConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.Interval;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class HistoDbPrintVoltageRangeTool implements Tool {

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "print-voltage-range";
            }

            @Override
            public String getTheme() {
                return "Histo DB";
            }

            @Override
            public String getDescription() {
                return "print substations historical min/max voltage";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt("case-file")
                        .desc("the case path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
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
    }

    private static class VoltageStats {
        private final Range<Float> range;
        private final int count;
        private final float vnom;
        private float pmax = 0;
        private VoltageStats(Range<Float> range, int count, float vnom) {
            this.range = range;
            this.count = count;
            this.vnom = vnom;
        }
    }

    @Override
    public void run(CommandLine line) throws Exception {
        Interval interval = Interval.parse(line.getOptionValue("interval"));
        Path caseFile = Paths.get(line.getOptionValue("case-file"));
        Map<String, VoltageStats> ranges = new HashMap<>();

        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new RuntimeException("Case '" + caseFile + "' not found");
        }
        network.getStateManager().allowStateMultiThreadAccess(true);

        OfflineConfig config = OfflineConfig.load();
        try (HistoDbClient histoDbClient = config.getHistoDbClientFactoryClass().newInstance().create()) {
            Set<HistoDbAttributeId> attrIds = new LinkedHashSet<>();
            for (VoltageLevel vl : network.getVoltageLevels()) {
                attrIds.add(new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.V));
            }
            HistoDbStats stats = histoDbClient.queryStats(attrIds, interval, HistoDbHorizon.SN, false);
            for (VoltageLevel vl : network.getVoltageLevels()) {
                HistoDbNetworkAttributeId attrId = new HistoDbNetworkAttributeId(vl.getId(), HistoDbAttr.V);
                float min = stats.getValue(HistoDbStatsType.MIN, attrId, Float.NaN) / vl.getNominalV();
                float max = stats.getValue(HistoDbStatsType.MAX, attrId, Float.NaN) / vl.getNominalV();
                int count = (int) stats.getValue(HistoDbStatsType.COUNT, attrId, 0);
                VoltageStats vstats = new VoltageStats(Range.closed(min, max), count, vl.getNominalV());
                for (Generator g : vl.getGenerators()) {
                    vstats.pmax += g.getMaxP();
                }
                ranges.put(vl.getId(), vstats);
            }
        }
        Table table = new Table(7, BorderStyle.CLASSIC_WIDE);
        table.addCell("ID");
        table.addCell("vnom");
        table.addCell("range");
        table.addCell("min");
        table.addCell("max");
        table.addCell("count");
        table.addCell("pmax");
        ranges.entrySet().stream().sorted((e1, e2) -> {
            VoltageStats stats1 = e1.getValue();
            VoltageStats stats2 = e2.getValue();
            Range<Float> r1 = stats1.range;
            Range<Float> r2 = stats2.range;
            float s1 = r1.upperEndpoint() - r1.lowerEndpoint();
            float s2 = r2.upperEndpoint() - r2.lowerEndpoint();
            return Float.compare(s1, s2);
        }).forEach(e -> {
            String vlId = e.getKey();
            VoltageStats stats = e.getValue();
            Range<Float> r = stats.range;
            float s = r.upperEndpoint() - r.lowerEndpoint();
            table.addCell(vlId);
            table.addCell(Float.toString(stats.vnom));
            table.addCell(Float.toString(s));
            table.addCell(Float.toString(r.lowerEndpoint()));
            table.addCell(Float.toString(r.upperEndpoint()));
            table.addCell(Integer.toString(stats.count));
            table.addCell(Float.toString(stats.pmax));
        });
        System.out.println(table.render());
    }

}
