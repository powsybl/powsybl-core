/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.securityindexes.SecurityIndex;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;
import eu.itesla_project.modules.simulation.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.util.*;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class RunImpactAnalysisOnStateTool implements Tool {

	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "run-impact-analysis-on-state";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Run impact analysis on a stored state of an online workflow";
		}

		@Override
		public Options getOptions() {
			Options options = new Options();
			options.addOption(Option.builder().longOpt("workflow")
	                .desc("the workflow id")
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
			options.addOption(Option.builder().longOpt("contingencies")
                    .desc("contingencies to test separated by , (all the db if not specified)")
                    .hasArg()
                    .argName("LIST")
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
		Set<String> contingencyIds = null;
        if (line.hasOption("contingencies")) {
            contingencyIds = Sets.newHashSet(line.getOptionValue("contingencies").split(","));
        }
        System.out.println("loading state " + stateId + " of workflow " + workflowId + " from the online db ...");
        OnlineConfig config = OnlineConfig.load();
		OnlineDb onlinedb = config.getOnlineDbFactoryClass().newInstance().create();
        // load the network
        Network network = onlinedb.getState(workflowId, stateId);
        if ( network != null ) {
	        ComputationManager computationManager = new LocalComputationManager();
	        DynamicDatabaseClientFactory ddbFactory = config.getDynamicDbClientFactoryClass().newInstance();
	        ContingenciesAndActionsDatabaseClient contingencyDb = config.getContingencyDbClientFactoryClass().newInstance().create();
	        SimulatorFactory simulatorFactory = config.getSimulatorFactoryClass().newInstance();
	        Stabilization stabilization = simulatorFactory.createStabilization(network, computationManager, 0, ddbFactory);
			ImpactAnalysis impactAnalysis = simulatorFactory.createImpactAnalysis(network, computationManager, 0, contingencyDb);
			Map<String, Object> initContext = new HashMap<>();
			SimulationParameters simulationParameters = SimulationParameters.load();
			stabilization.init(simulationParameters, initContext);
			impactAnalysis.init(simulationParameters, initContext);
			System.out.println("running stabilization simulation...");
			StabilizationResult sr = stabilization.run();
			System.out.println("stabilization status: " + sr.getStatus());
			if (sr.getStatus() == StabilizationStatus.COMPLETED) {
				System.out.println("running impact analysis...");
				ImpactAnalysisResult iar = impactAnalysis.run(sr.getState(), contingencyIds);

				Table table = new Table(1 + SecurityIndexType.values().length, BorderStyle.CLASSIC_WIDE);
				table.addCell("Contingency");
				for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
					table.addCell(securityIndexType.toString());
				}

				Multimap<String, SecurityIndex> securityIndexesPerContingency = Multimaps.index(iar.getSecurityIndexes(), new Function<SecurityIndex, String>() {
					@Override
					public String apply(SecurityIndex securityIndex) {
						return securityIndex.getId().getContingencyId();
					}
				});
				for (Map.Entry<String, Collection<SecurityIndex>> entry : securityIndexesPerContingency.asMap().entrySet()) {
					String contingencyId = entry.getKey();

					table.addCell(contingencyId);

					Map<SecurityIndexType, Boolean> ok = new EnumMap<>(SecurityIndexType.class);
					for (SecurityIndex securityIndex : entry.getValue()) {
						ok.put(securityIndex.getId().getSecurityIndexType(), securityIndex.isOk());
					}

					for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
						Boolean b = ok.get(securityIndexType);
						String str;
						if (b == null) {
							str = "NA";
						} else {
							str = b ? "OK" : "NOK";
						}
						table.addCell(str);
					}
				}
				System.out.println(table.render());
			} else {
	            	System.out.println("Error running stabilization -  metrics = " + sr.getMetrics());
			}
        } else {
        	System.out.println("no state " + stateId + " of workflow " + workflowId + " stored in the online db");
        }
		onlinedb.close();
	}
	
}
