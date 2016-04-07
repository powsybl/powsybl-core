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
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineDb;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class ExportOnlineWorkflowStateTool implements Tool {

	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "export-online-workflow-state";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Export network data of a stored state of an online workflow";
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
			options.addOption(Option.builder().longOpt("folder")
	                .desc("the folder where to export the network data")
	                .hasArg()
	                .argName("FOLDER")
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
		OnlineConfig config = OnlineConfig.load();
		OnlineDb onlinedb = config.getOnlineDbFactoryClass().newInstance().create();
		String workflowId = line.getOptionValue("workflow");
		Integer stateId = Integer.valueOf(line.getOptionValue("state"));
		Path folder =  line.hasOption("folder") ? Paths.get(line.getOptionValue("folder")) : null;
		List<Integer> storedStates = onlinedb.listStoredStates(workflowId);
		if ( storedStates.contains(stateId) ) {
			if ( folder != null )
				System.out.println("Exporting stored state " + stateId + " of workflow " + workflowId + " to folder " + folder);
			else
				System.out.println("Exporting stored state " + stateId + " of workflow " + workflowId + " to current folder");
			onlinedb.exportState(workflowId, stateId, folder);
		} else
			System.out.println("No state " + stateId + " stored for workflow " + workflowId);
		onlinedb.close();
	}
	
}
