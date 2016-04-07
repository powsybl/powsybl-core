/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import com.csvreader.CsvWriter;
import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineDb;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class PrintOnlineWorkflowPostContingencyLoadflowTool implements Tool {

	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "print-online-workflow-postcontingency-loadflow";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Print convergence of post contingencies loadflow of an online workflow";
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
	                .argName("STATE")
	                .build());
			options.addOption(Option.builder().longOpt("contingency")
	                .desc("the contingency id")
	                .hasArg()
	                .argName("CONTINGENCY")
	                .build());
			options.addOption(Option.builder().longOpt("csv")
	                .desc("export in csv format")
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
		if ( line.hasOption("state") ) {
			Integer stateId = Integer.parseInt(line.getOptionValue("state"));
			Map<String, Boolean> loadflowConvergence = onlinedb.getPostContingencyLoadflowConvergence(workflowId, stateId);
			if ( loadflowConvergence != null && !loadflowConvergence.keySet().isEmpty() ) { 
				Table table = new Table(3, BorderStyle.CLASSIC_WIDE);
				StringWriter content = new StringWriter();
				CsvWriter cvsWriter = new CsvWriter(content, ',');
				printHeaders(table, cvsWriter);
				String[] contingencyIds = loadflowConvergence.keySet().toArray(new String[loadflowConvergence.keySet().size()]);
				Arrays.sort(contingencyIds);
				for(String contingencyId : contingencyIds) {
					Boolean loadflowConverge = loadflowConvergence.get(contingencyId);
					printValues(table, cvsWriter, stateId, contingencyId, loadflowConverge);
				}
				cvsWriter.flush();
				printOutput(table, content, line.hasOption("csv"));
				cvsWriter.close();
			} else
				System.out.println("\nNo post contingency loadflow data for workflow "+workflowId+" and state "+stateId);
		} else if ( line.hasOption("contingency") ) {
			String contingencyId = line.getOptionValue("contingency");
			Map<Integer, Boolean> loadflowConvergence = onlinedb.getPostContingencyLoadflowConvergence(workflowId, contingencyId);
			if ( loadflowConvergence != null && !loadflowConvergence.keySet().isEmpty() ) { 
				Table table = new Table(3, BorderStyle.CLASSIC_WIDE);
				StringWriter content = new StringWriter();
				CsvWriter cvsWriter = new CsvWriter(content, ',');
				printHeaders(table, cvsWriter);
				Integer[] stateIds = loadflowConvergence.keySet().toArray(new Integer[loadflowConvergence.keySet().size()]);
				Arrays.sort(stateIds);
				for(Integer stateId : stateIds) {
					Boolean loadflowConverge = loadflowConvergence.get(stateId);
					printValues(table, cvsWriter, stateId, contingencyId, loadflowConverge);
				}
				cvsWriter.flush();
				printOutput(table, content, line.hasOption("csv"));
				cvsWriter.close();
			} else
				System.out.println("\nNo post contingency loadflow data for workflow "+workflowId+" and contingency "+contingencyId);
		} else {		
			Map<Integer, Map<String, Boolean>> loadflowConvergence = onlinedb.getPostContingencyLoadflowConvergence(workflowId);
			if ( loadflowConvergence != null && !loadflowConvergence.keySet().isEmpty() ) { 
				Table table = new Table(3, BorderStyle.CLASSIC_WIDE);
				StringWriter content = new StringWriter();
				CsvWriter cvsWriter = new CsvWriter(content, ',');
				printHeaders(table, cvsWriter);
				Integer[] stateIds = loadflowConvergence.keySet().toArray(new Integer[loadflowConvergence.keySet().size()]);
				Arrays.sort(stateIds);
				for(Integer stateId : stateIds) {
					Map<String, Boolean> stateLoadflowConvergence = loadflowConvergence.get(stateId);
					if ( stateLoadflowConvergence != null && !stateLoadflowConvergence.keySet().isEmpty() ) {
						String[] contingencyIds = stateLoadflowConvergence.keySet().toArray(new String[stateLoadflowConvergence.keySet().size()]);
						Arrays.sort(contingencyIds);
						for(String contingencyId : contingencyIds) {
							Boolean loadflowConverge = stateLoadflowConvergence.get(contingencyId);
							printValues(table, cvsWriter, stateId, contingencyId, loadflowConverge);
						}
					}
				}
				cvsWriter.flush();
				printOutput(table, content, line.hasOption("csv"));
				cvsWriter.close();
			} else
				System.out.println("\nNo post contingency loadflow data for workflow "+workflowId);
		}
		onlinedb.close();
	}
	
	private void printHeaders(Table table, CsvWriter cvsWriter) throws IOException {
		String[] headers = new String[3];
		int i = 0;
        table.addCell("State", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "State";
        table.addCell("Contingency", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "Contingency";
        table.addCell("Loadflow Convergence", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "Loadflow Convergence";
        cvsWriter.writeRecord(headers);
	}
	
	private void printValues(Table table, CsvWriter cvsWriter, Integer stateId, String contingencyId, Boolean loadflowConverge) throws IOException {
			String[] values = new String[3];
			int i = 0;
			table.addCell(Integer.toString(stateId), new CellStyle(CellStyle.HorizontalAlign.right));
			values[i++] = Integer.toString(stateId);
			table.addCell(contingencyId, new CellStyle(CellStyle.HorizontalAlign.left));
			values[i++] = contingencyId;
			table.addCell(loadflowConverge.toString(), new CellStyle(CellStyle.HorizontalAlign.left));
			values[i++] = loadflowConverge.toString();
			cvsWriter.writeRecord(values);
	}
	
	private void printOutput(Table table, StringWriter content, boolean csv) {
		if ( csv )
			System.out.println(content.toString());
		else
			System.out.println(table.render());
	}
	
}
