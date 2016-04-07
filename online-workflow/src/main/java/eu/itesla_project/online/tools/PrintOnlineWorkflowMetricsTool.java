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
import eu.itesla_project.modules.online.OnlineStep;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class PrintOnlineWorkflowMetricsTool implements Tool {
	
	private static int COLUMN_LENGTH = 100;

	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "print-online-workflow-metrics";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Print stored metrics of a step of an online workflow";
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
			options.addOption(Option.builder().longOpt("step")
	                .desc("the online step (FORECAST_ERRORS_ANALYSIS,MERGING,WORST_CASE_APPROACH,MONTE_CARLO_SAMPLING,LOAD_FLOW,SECURITY_RULES_ASSESSMENT,CONTROL_ACTION_OPTIMIZATION,TIME_DOMAIN_SIMULATION,STABILIZATION,IMPACT_ANALYSIS)")
	                .hasArg()
	                .required()
	                .argName("STEP")
	                .build());
			options.addOption(Option.builder().longOpt("state")
	                .desc("the state id (if not specified all the metrics of all the states are printed, in CSV format)")
	                .hasArg()
	                .argName("STATE")
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
		OnlineStep step = OnlineStep.valueOf(line.getOptionValue("step"));
		if ( line.hasOption("state") && line.hasOption("step")) {
			Integer stateId = Integer.parseInt(line.getOptionValue("state"));
			Map<String,String> metrics = onlinedb.getMetrics(workflowId, stateId, step);
			if ( metrics != null && !metrics.keySet().isEmpty() ) {
				Table table = new Table(3, BorderStyle.CLASSIC_WIDE);
				table.addCell("State", new CellStyle(CellStyle.HorizontalAlign.center));
				table.addCell("Parameter", new CellStyle(CellStyle.HorizontalAlign.center));
				table.addCell("Value", new CellStyle(CellStyle.HorizontalAlign.center));
				for(String key : metrics.keySet()) {
					table.addCell(Integer.toString(stateId), new CellStyle(CellStyle.HorizontalAlign.right));
					table.addCell(key, new CellStyle(CellStyle.HorizontalAlign.left));
					String value = metrics.get(key);
					while ( value.length() > COLUMN_LENGTH ) {
						table.addCell(value.substring(0, COLUMN_LENGTH), new CellStyle(CellStyle.HorizontalAlign.left));
						table.addCell(" ", new CellStyle(CellStyle.HorizontalAlign.left));
						table.addCell(" ", new CellStyle(CellStyle.HorizontalAlign.left));
						value = value.substring(COLUMN_LENGTH);
					}
					table.addCell(value, new CellStyle(CellStyle.HorizontalAlign.left));
//					table.addCell(" ", new CellStyle(CellStyle.HorizontalAlign.left));
//					table.addCell(" ", new CellStyle(CellStyle.HorizontalAlign.left));
//					table.addCell(metrics.get(key), new CellStyle(CellStyle.HorizontalAlign.left));
				}
				System.out.println(table.render());
			} else
				System.out.println("\nNo metrics for workflow "+workflowId+", step "+step.name()+" and state "+stateId);			
		} else {
			String csvMetrics = onlinedb.getCsvMetrics(workflowId, step);
			if ( csvMetrics!=null && !csvMetrics.isEmpty() ) {
				System.out.println(csvMetrics);
			} else
				System.out.println("\nNo metrics for workflow "+workflowId+" and step "+step.name());
		}
		onlinedb.close();
	}
	
	
}
