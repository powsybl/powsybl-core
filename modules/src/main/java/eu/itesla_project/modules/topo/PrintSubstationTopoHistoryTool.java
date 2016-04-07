/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.network.util.ShortIdDictionary;
import eu.itesla_project.modules.histo.*;
import eu.itesla_project.modules.offline.OfflineConfig;
import eu.itesla_project.modules.topo.parser.TopoBus;
import eu.itesla_project.modules.topo.parser.TopoHistoryHandler;
import eu.itesla_project.modules.topo.parser.TopoHistoryParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.Interval;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class PrintSubstationTopoHistoryTool implements Tool {
    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "print-substation-topo-history";
            }

            @Override
            public String getTheme() {
                return "Histo DB";
            }

            @Override
            public String getDescription() {
                return "Print topology history of a substation";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt("substation-id")
                        .desc("substation id")
                        .hasArg()
                        .required()
                        .argName("ID")
                        .build());
                options.addOption(Option.builder().longOpt("interval")
                        .desc("time interval (example 2013-01-01T00:00:00+01:00/2013-01-31T23:59:00+01:00)")
                        .hasArg()
                        .required()
                        .argName("DATE1/DATE2")
                        .build());
                options.addOption(Option.builder().longOpt("generate-short-ids-dict")
                        .desc("generate a short id dictionary and replace real ids by short ones (A, B, etc)")
                        .hasArg()
                        .argName("DICT_PATH")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    private static Map<Set<TopoBus>, AtomicInteger> translate(Map<Set<TopoBus>, AtomicInteger> topos, ShortIdDictionary dict) {
        Map<Set<TopoBus>, AtomicInteger> topos2 = new LinkedHashMap<>();
        for (Map.Entry<Set<TopoBus>, AtomicInteger> e : topos.entrySet()) {
            Set<TopoBus> topo = e.getKey();
            AtomicInteger count = e.getValue();
            Set<TopoBus> topo2 = new HashSet<>();
            for (TopoBus b : topo) {
                topo2.add(new TopoBus(b.getEquipments().stream().map(s -> dict.getShortId(s)).collect(Collectors.toSet()), b.getSubstation()));
            }
            topos2.put(topo2, count);
        }
        return topos2;
    }

    private static ShortIdDictionary createDict(Set<Set<TopoBus>> topos) {
        Set<String> ids = new TreeSet<>();
        for (Set<TopoBus> topo : topos) {
            for (TopoBus b : topo) {
                ids.addAll(b.getEquipments());
            }
        }
        return new ShortIdDictionary(ids);
    }

    @Override
    public void run(CommandLine line) throws Exception {
        String substationId = line.getOptionValue("substation-id");
        Interval interval = Interval.parse(line.getOptionValue("interval"));
        Path dictFile = null;
        if (line.hasOption("generate-short-ids-dict")) {
            dictFile = Paths.get(line.getOptionValue("generate-short-ids-dict"));
        }
        OfflineConfig config = OfflineConfig.load();
        Map<Set<TopoBus>, AtomicInteger> topos = new LinkedHashMap<>();
        try (HistoDbClient histoDbClient = config.getHistoDbClientFactoryClass().newInstance().create()) {
            int rowCount = histoDbClient.queryCount(interval, HistoDbHorizon.SN);
            Set<HistoDbAttributeId> attributeIds = Collections.singleton(new HistoDbNetworkAttributeId(substationId, HistoDbAttr.TOPO));
            try (InputStream is = histoDbClient.queryCsv(HistoQueryType.data, attributeIds, interval, HistoDbHorizon.SN, false, false)) {
                new TopoHistoryParser().parse(rowCount, is, new TopoHistoryHandler() {
                    @Override
                    public void onHeader(List<String> substationIds, int rowCount) {
                    }

                    @Override
                    public void onTopology(int row, int col, Set<TopoBus> topo) {
                        if (topo != null) {
                            if (topos.containsKey(topo)) {
                                topos.get(topo).incrementAndGet();
                            } else {
                                topos.put(topo, new AtomicInteger(1));
                            }
                        }
                    }
                });
            }
        }

        Map<Set<TopoBus>, AtomicInteger> topos2;
        if (dictFile != null) {
            ShortIdDictionary dict = createDict(topos.keySet());
            dict.write(dictFile);
            topos2 = translate(topos, dict);
        } else {
            topos2 = topos;
        }
        topos2.entrySet().stream().forEach(e -> System.out.println(e.getKey() + " " + e.getValue()));
    }
}
