/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.wca;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.histo.*;
import eu.itesla_project.modules.offline.OfflineConfig;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.rules.RulesDbClient;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.Interval;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class WCATool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(WCATool.class);

    private static final double DEFAULT_PURITY_THRESHOLD = 0.95;

    private static final char CSV_SEPARATOR = ';';

    private static final boolean DEFAULT_STOP_WCA_ON_VIOLATIONS = true;

    private static Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "run-wca";
        }

        @Override
        public String getTheme() {
            return "Worst Case";
        }

        @Override
        public String getDescription() {
            return "run WCA and print clusters";
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
            options.addOption(Option.builder().longOpt("offline-workflow-id")
                    .desc("the offline workflow id (to get security rules)")
                    .hasArg()
                    .argName("ID")
                    .build());
            options.addOption(Option.builder().longOpt("history-interval")
                    .desc("history time interval (example 2013-01-01T00:00:00+01:00/2013-01-31T23:59:00+01:00)")
                    .hasArg()
                    .argName("DATE1/DATE2")
                    .required()
                    .build());
            options.addOption(Option.builder().longOpt("rules-db-name")
                    .desc("the rules db name (default is " + OfflineConfig.DEFAULT_RULES_DB_NAME + ")")
                    .hasArg()
                    .argName("NAME")
                    .build());
            options.addOption(Option.builder().longOpt("purity-threshold")
                    .desc("the purity threshold (related to decision tree), default is " + DEFAULT_PURITY_THRESHOLD)
                    .hasArg()
                    .argName("THRESHOLD")
                    .build());
            options.addOption(Option.builder().longOpt("security-index-types")
                    .desc("sub list of security index types to use, all of them if the option if not specified")
                    .hasArg()
                    .argName("INDEX_TYPE,INDEX_TYPE,...")
                    .build());
            options.addOption(Option.builder().longOpt("output-csv-file")
                    .desc("output CSV file path, only needed in case of multiple run")
                    .hasArg()
                    .argName("FILE")
                    .build());
            options.addOption(Option.builder().longOpt("stop-on-violations")
                    .desc("stop WCA if there are violations, default is " + DEFAULT_STOP_WCA_ON_VIOLATIONS)
                    .hasArg()
                    .argName("true/false")
                    .build());
            return options;
        }

        @Override
        public String getUsageFooter() {
            return "Where INDEX_TYPE is one of " + Arrays.toString(SecurityIndexType.values());
        }

    };

    @Override
    public Command getCommand() {
        return COMMAND;
    }

    private static class SynchronizedHistoDbClient extends ForwardingHistoDbClient {

        private final Lock lock = new ReentrantLock();

        private SynchronizedHistoDbClient(HistoDbClient delegate) {
            super(delegate);
        }

        @Override
        public HistoDbStats queryStats(Set<HistoDbAttributeId> attrIds, Interval interval, HistoDbHorizon horizon, boolean async) throws IOException, InterruptedException {
            lock.lock();
            try {
                return super.queryStats(attrIds, interval, horizon, async);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public InputStream queryCsv(HistoQueryType queryType, Set<HistoDbAttributeId> attrIds, Interval interval, HistoDbHorizon horizon, boolean zipped, boolean async) throws IOException, InterruptedException {
            lock.lock();
            try {
                return super.queryCsv(queryType, attrIds, interval, horizon, zipped, async);
            } finally {
                lock.unlock();
            }
        }
    }

    private static void writeClustersCsv(Map<String, Map<String, WCACluster>> clusterPerContingencyPerBaseCase,
                                         Set<String> contingencyIds, Path outputCsvFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputCsvFile, StandardCharsets.UTF_8)) {
            writer.write("base case");
            for (String contingencyId : contingencyIds) {
                writer.write(CSV_SEPARATOR);
                writer.write("cluster num " + contingencyId);
            }
            for (String contingencyId : contingencyIds) {
                writer.write(CSV_SEPARATOR);
                writer.write("cluster cause " + contingencyId);
            }
            writer.newLine();
            for (Map.Entry<String, Map<String, WCACluster>> entry : clusterPerContingencyPerBaseCase.entrySet()) {
                String baseCaseName = entry.getKey();
                Map<String, WCACluster> clusterNumPerContingency = entry.getValue();
                writer.write(baseCaseName);
                for (String contingencyId : contingencyIds) {
                    WCACluster cluster = clusterNumPerContingency.get(contingencyId);
                    writer.write(CSV_SEPARATOR);
                    if (cluster != null) {
                        writer.write(cluster.getNum().toIntValue() + " (" + cluster.getOrigin() + ")");
                    } else {
                        writer.write("");
                    }
                }
                for (String contingencyId : contingencyIds) {
                    WCACluster cluster = clusterNumPerContingency.get(contingencyId);
                    writer.write(CSV_SEPARATOR);
                    if (cluster != null) {
                        writer.write(cluster.getCauses().stream().collect(Collectors.joining("|")));
                    } else {
                        writer.write("");
                    }
                }
                writer.newLine();
            }
        }
    }

    @Override
    public void run(CommandLine line) throws Exception {
        Path caseFile = Paths.get(line.getOptionValue("case-file"));
        String offlineWorkflowId = line.getOptionValue("offline-workflow-id"); // can be null meaning use no offline security rules
        Interval histoInterval = Interval.parse(line.getOptionValue("history-interval"));
        String rulesDbName = line.hasOption("rules-db-name") ? line.getOptionValue("rules-db-name") : OfflineConfig.DEFAULT_RULES_DB_NAME;
        double purityThreshold = DEFAULT_PURITY_THRESHOLD;
        if (line.hasOption("purity-threshold")) {
            purityThreshold = Double.parseDouble(line.getOptionValue("purity-threshold"));
        }
        Set<SecurityIndexType> securityIndexTypes = null;
        if (line.hasOption("security-index-types")) {
            securityIndexTypes = Arrays.stream(line.getOptionValue("security-index-types").split(","))
                    .map(SecurityIndexType::valueOf)
                    .collect(Collectors.toSet());
        }
        Path outputCsvFile = null;
        if (line.hasOption("output-csv-file")) {
            outputCsvFile = Paths.get(line.getOptionValue("output-csv-file"));
        }
        boolean stopWcaOnViolations = DEFAULT_STOP_WCA_ON_VIOLATIONS;
        if (line.hasOption("stop-on-violations")) {
            stopWcaOnViolations = Boolean.parseBoolean(line.getOptionValue("stop-on-violations"));
        }

        try (ComputationManager computationManager = new LocalComputationManager()) {
            WCAParameters parameters = new WCAParameters(histoInterval, offlineWorkflowId, securityIndexTypes, purityThreshold, stopWcaOnViolations);
            OnlineConfig config = OnlineConfig.load();
            ContingenciesAndActionsDatabaseClient contingenciesDb = config.getContingencyDbClientFactoryClass().newInstance().create();
            LoadFlowFactory loadFlowFactory = config.getLoadFlowFactoryClass().newInstance();
            WCAFactory wcaFactory = config.getWcaFactoryClass().newInstance();
            try (HistoDbClient histoDbClient = new SynchronizedHistoDbClient(config.getHistoDbClientFactoryClass().newInstance().create());
                 RulesDbClient rulesDbClient = config.getRulesDbClientFactoryClass().newInstance().create(rulesDbName)) {

                UncertaintiesAnalyserFactory uncertaintiesAnalyserFactory = config.getUncertaintiesAnalyserFactoryClass().newInstance();

                if (Files.isRegularFile(caseFile)) {
                    if (outputCsvFile != null) {
                        throw new RuntimeException("In case of single wca, only standard output pretty print is supported");
                    }
                    System.out.println("loading case...");
                    // load the network
                    Network network = Importers.loadNetwork(caseFile);
                    if (network == null) {
                        throw new RuntimeException("Case '" + caseFile + "' not found");
                    }
                    network.getStateManager().allowStateMultiThreadAccess(true);

                    WCA wca = wcaFactory.create(network, computationManager, histoDbClient, rulesDbClient, uncertaintiesAnalyserFactory, contingenciesDb, loadFlowFactory);
                    WCAAsyncResult result = wca.runAsync(StateManager.INITIAL_STATE_ID, parameters).join();

                    Table table = new Table(3, BorderStyle.CLASSIC_WIDE);
                    table.addCell("Contingency");
                    table.addCell("Cluster");
                    table.addCell("Causes");

                    List<CompletableFuture<WCACluster>> futureClusters = new LinkedList<>(result.getClusters());
                    while (futureClusters.size() > 0) {
                        CompletableFuture.anyOf(futureClusters.toArray(new CompletableFuture[futureClusters.size()])).join();
                        for (Iterator<CompletableFuture<WCACluster>> it = futureClusters.iterator(); it.hasNext(); ) {
                            CompletableFuture<WCACluster> futureCluster = it.next();
                            if (futureCluster.isDone()) {
                                it.remove();
                                WCACluster cluster = futureCluster.get();

                                if (cluster != null) {
                                    System.out.println("contingency " + cluster.getContingency().getId() + " done: "
                                            + cluster.getNum() + " (" + cluster.getOrigin() + ")");

                                    table.addCell(cluster.getContingency().getId());
                                    table.addCell(cluster.getNum() + " (" + cluster.getOrigin() + ")");
                                    List<String> sortedCauses = cluster.getCauses().stream().sorted().collect(Collectors.toList());
                                    if (sortedCauses != null && sortedCauses.size() > 0) {
                                        table.addCell(sortedCauses.get(0));
                                        for (int i = 1; i < sortedCauses.size(); i++) {
                                            table.addCell("");
                                            table.addCell("");
                                            table.addCell(sortedCauses.get(i));
                                        }
                                    } else {
                                        table.addCell("");
                                    }
                                }
                            }
                        }
                    }

                    System.out.println(table.render());
                } else if (Files.isDirectory(caseFile)){
                    if (outputCsvFile == null) {
                        throw new RuntimeException("In case of multiple wca, you have to specify and ouput to csv file");
                    }

                    Map<String, Map<String, WCACluster>> clusterPerContingencyPerBaseCase = Collections.synchronizedMap(new TreeMap<>());
                    Set<String> contingencyIds = Collections.synchronizedSet(new TreeSet<>());

                    Importers.loadNetworks(caseFile, true, network -> {
                        try {
                            network.getStateManager().allowStateMultiThreadAccess(true);
                            String baseStateId = network.getId();
                            network.getStateManager().cloneState(StateManager.INITIAL_STATE_ID, baseStateId);
                            network.getStateManager().setWorkingState(baseStateId);

                            WCA wca = wcaFactory.create(network, computationManager, histoDbClient, rulesDbClient, uncertaintiesAnalyserFactory, contingenciesDb, loadFlowFactory);
                            WCAAsyncResult result = wca.runAsync(baseStateId, parameters).join();

                            Map<String, WCACluster> clusterPerContingency = new HashMap<>();

                            List<CompletableFuture<WCACluster>> futureClusters = new LinkedList<>(result.getClusters());
                            while (futureClusters.size() > 0) {
                                CompletableFuture.anyOf(futureClusters.toArray(new CompletableFuture[futureClusters.size()])).join();
                                for (Iterator<CompletableFuture<WCACluster>> it = futureClusters.iterator(); it.hasNext(); ) {
                                    CompletableFuture<WCACluster> futureCluster = it.next();
                                    if (futureCluster.isDone()) {
                                        it.remove();
                                        WCACluster cluster = futureCluster.get();
                                        if (cluster != null) {
                                            System.out.println("case " + network.getId() + ", contingency " + cluster.getContingency().getId() + " done: "
                                                    + cluster.getNum() + " (" + cluster.getOrigin() + ")");

                                            clusterPerContingency.put(cluster.getContingency().getId(), cluster);
                                            contingencyIds.add(cluster.getContingency().getId());
                                        }
                                    }
                                }
                            }

                            clusterPerContingencyPerBaseCase.put(network.getId(), clusterPerContingency);
                        } catch (Exception e) {
                            LOGGER.error(e.toString(), e);
                        }
                    }, dataSource -> System.out.println("loading case " + dataSource.getBaseName() + "..."));

                    writeClustersCsv(clusterPerContingencyPerBaseCase, contingencyIds, outputCsvFile);
                }
            }
        }
    }

}
