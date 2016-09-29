/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.OnlineWorkflowResults;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
//@AutoService(Tool.class)
public class PrintOnlineWorkflowResultsTool implements Tool {

	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "print-online-workflow-results";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Print stored online workflow results";
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
		OnlineWorkflowResults wfResults = onlinedb.getResults(workflowId);
		if ( wfResults != null ) {
			if ( !wfResults.getContingenciesWithActions().isEmpty() ) {
				System.out.println("** Contingencies requiring corrective actions **");
				Table table = new Table(3, BorderStyle.CLASSIC_WIDE);
		        table.addCell("Contingency", new CellStyle(CellStyle.HorizontalAlign.center));
		        table.addCell("State", new CellStyle(CellStyle.HorizontalAlign.center));
		        table.addCell("Actions", new CellStyle(CellStyle.HorizontalAlign.center));
				for (String contingencyId : wfResults.getContingenciesWithActions()) {
					for (Integer stateId : wfResults.getUnsafeStatesWithActions(contingencyId).keySet()) {
						table.addCell(contingencyId);
						table.addCell(stateId.toString(), new CellStyle(CellStyle.HorizontalAlign.right));
						String actionList = "";
						for (String actionId : wfResults.getActionsIds(contingencyId, stateId)) {
							actionList += actionId + ";";
						}
						if ( actionList.length() > 0 )
							actionList = actionList.substring(0, actionList.length()-1);
						table.addCell(actionList);
					}
				}
				System.out.println(table.render());
			} else
				System.out.println("\nNo contingencies requiring corrective actions");
			
			if ( !wfResults.getUnsafeContingencies().isEmpty() ) {
//				System.out.println("\n** Contingencies requiring T-D simulation **");
//				Table table = new Table(4, BorderStyle.CLASSIC_WIDE);
//		        table.addCell("Contingency", new CellStyle(CellStyle.HorizontalAlign.center));
//		        table.addCell("State", new CellStyle(CellStyle.HorizontalAlign.center));
//		        table.addCell("Index", new CellStyle(CellStyle.HorizontalAlign.center));
//		        table.addCell("Safe", new CellStyle(CellStyle.HorizontalAlign.center));
//				for (String contingencyId : wfResults.getUnsafeContingencies()) {
//					for (Integer stateId : wfResults.getUnstableStates(contingencyId)) {
//						for (String index : wfResults.getIndexesData(contingencyId, stateId).keySet()) {
//							table.addCell(contingencyId);
//							table.addCell(stateId.toString(), new CellStyle(CellStyle.HorizontalAlign.right));
//							table.addCell(index);
//							table.addCell(wfResults.getIndexesData(contingencyId, stateId).get(index).toString());
//						}
//					}
//				}
//				System.out.println(table.render());
			
				System.out.println("\n** Contingencies requiring T-D simulation **");
				Table table = new Table(SecurityIndexType.values().length+2, BorderStyle.CLASSIC_WIDE);
		        table.addCell("Contingency", new CellStyle(CellStyle.HorizontalAlign.center));
		        table.addCell("State", new CellStyle(CellStyle.HorizontalAlign.center));
		        for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
		        	table.addCell(securityIndexType.getLabel(), new CellStyle(CellStyle.HorizontalAlign.center));
		        }
				for (String contingencyId : wfResults.getUnsafeContingencies()) {
					for (Integer stateId : wfResults.getUnstableStates(contingencyId)) {
						table.addCell(contingencyId);
						table.addCell(stateId.toString(), new CellStyle(CellStyle.HorizontalAlign.right));
						HashMap<String, String> indexesValues = getIndexesValues(wfResults.getIndexesData(contingencyId, stateId));
						for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
							table.addCell(indexesValues.get(securityIndexType.getLabel()), new CellStyle(CellStyle.HorizontalAlign.center));
						}
					}
				}
				System.out.println(table.render());
			} else
				System.out.println("\nNo contingencies requiring T-D simulation");
		} else
			System.out.println("No results for this workflow");
		onlinedb.close();
	}
	
	private HashMap<String, String> getIndexesValues(Map<String,Boolean> securityIndexes) {
		HashMap<String, String> indexesValues = new HashMap<String, String>();
		for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
			if ( securityIndexes.containsKey(securityIndexType.getLabel()) )
				indexesValues.put(securityIndexType.getLabel(), securityIndexes.get(securityIndexType.getLabel()) ? "Safe" : "Unsafe");
			else
				indexesValues.put(securityIndexType.getLabel(), "-");
		}
		return indexesValues;
	}

}
