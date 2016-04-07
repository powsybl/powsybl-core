/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.histo.HistoDbClient;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.rules.RulesDbClient;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;
import eu.itesla_project.modules.wca.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.Interval;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class RunWcaOnStateTool implements Tool {

    private static Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "run-wca-on-state";
        }

        @Override
        public String getTheme() {
            return Themes.ONLINE_WORKFLOW;
        }

        @Override
        public String getDescription() {
            return "Run worst case approach on a stored state of an online workflow";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder().longOpt("workflow")
	                .desc("the online workflow id")
	                .hasArg()
	                .required()
	                .argName("ID")
	                .build());
			options.addOption(Option.builder().longOpt("state")
	                .desc("the state id")
	                .hasArg()
	                .required()
	                .argName("STATE")
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
                    .build());
            options.addOption(Option.builder().longOpt("purity-threshold")
                    .desc("the purity threshold (related to decision tree)")
                    .hasArg()
                    .argName("THRESHOLD")
                    .build());
            options.addOption(Option.builder().longOpt("security-index-types")
                    .desc("sub list of security index types to use, all the ones used in the online workflow if the option if not specified")
                    .hasArg()
                    .argName("INDEX_TYPE,INDEX_TYPE,...")
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
		String workflowId = line.getOptionValue("workflow");
		Integer stateId = Integer.valueOf(line.getOptionValue("state"));
		System.out.println("loading state " + stateId + " of workflow " + workflowId + " from the online db ...");
        OnlineConfig config = OnlineConfig.load();
		OnlineDb onlinedb = config.getOnlineDbFactoryClass().newInstance().create();
        // load the network
        Network network = onlinedb.getState(workflowId, stateId);
        if ( network != null ) {
        	OnlineWorkflowParameters parameters = onlinedb.getWorkflowParameters(workflowId);
        	String offlineWorkflowId = parameters.getOfflineWorkflowId();
        	if (line.hasOption("offline-workflow"))
        		offlineWorkflowId = line.getOptionValue("offline-workflow");
        	Interval histoInterval = parameters.getHistoInterval();
        	if (line.hasOption("history-interval"))
        		histoInterval = Interval.parse(line.getOptionValue("history-interval"));
        	double purityThreshold = parameters.getRulesPurityThreshold();
		    if (line.hasOption("purity-threshold"))
		        purityThreshold = Double.parseDouble(line.getOptionValue("purity-threshold"));
		    Set<SecurityIndexType> securityIndexTypes = parameters.getSecurityIndexes();
		    if (line.hasOption("security-index-types")) {
		        securityIndexTypes = Arrays.stream(line.getOptionValue("security-index-types").split(","))
		                .map(SecurityIndexType::valueOf)
		                .collect(Collectors.toSet());
		    }
		    ComputationManager computationManager = new LocalComputationManager();
		    network.getStateManager().allowStateMultiThreadAccess(true);
		    WCAParameters wcaParameters = new WCAParameters(histoInterval, offlineWorkflowId, securityIndexTypes, purityThreshold);
	        ContingenciesAndActionsDatabaseClient contingenciesDb = config.getContingencyDbClientFactoryClass().newInstance().create();
	        LoadFlowFactory loadFlowFactory = config.getLoadFlowFactoryClass().newInstance();
	        try (HistoDbClient histoDbClient = config.getHistoDbClientFactoryClass().newInstance().create();
	             RulesDbClient rulesDbClient = config.getRulesDbClientFactoryClass().newInstance().create("rulesdb")) {
	            UncertaintiesAnalyserFactory uncertaintiesAnalyserFactory = config.getUncertaintiesAnalyserFactoryClass().newInstance();
	            WCA wca = config.getWcaFactoryClass().newInstance().create(network, computationManager, histoDbClient, rulesDbClient, uncertaintiesAnalyserFactory, contingenciesDb, loadFlowFactory);
	            WCAResult result = wca.run(wcaParameters);
	            Table table = new Table(7, BorderStyle.CLASSIC_WIDE);
	            table.addCell("Contingency", new CellStyle(CellStyle.HorizontalAlign.center));
		        table.addCell("Cluster 1", new CellStyle(CellStyle.HorizontalAlign.center));
		        table.addCell("Cluster 2", new CellStyle(CellStyle.HorizontalAlign.center));
		        table.addCell("Cluster 3", new CellStyle(CellStyle.HorizontalAlign.center));
		        table.addCell("Cluster 4", new CellStyle(CellStyle.HorizontalAlign.center));
		        table.addCell("Undefined", new CellStyle(CellStyle.HorizontalAlign.center));
		        table.addCell("Cause", new CellStyle(CellStyle.HorizontalAlign.center));
		        for (WCACluster cluster : result.getClusters()) {
		        	table.addCell(cluster.getContingency().getId());
		        	int[] clusterIndexes = new int[]{1, 2, 3, 4, -1};
					for (int k = 0; k < clusterIndexes.length; k++) {
						if ( clusterIndexes[k] == cluster.getNum().toIntValue() ) {
							table.addCell("X", new CellStyle(CellStyle.HorizontalAlign.center));
						} else {
							table.addCell("-", new CellStyle(CellStyle.HorizontalAlign.center));
						}
					}
					table.addCell(Objects.toString(cluster.getCauses(), ""), new CellStyle(CellStyle.HorizontalAlign.center));
		        }
	            System.out.println(table.render());
	        }
	    }
    }

}
