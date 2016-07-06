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
import eu.itesla_project.modules.security.LimitViolation;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class PrintOnlineWorkflowPostContingencyViolationsTool implements Tool {

	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "print-online-workflow-postcontingency-violations";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Print post contingency violations in the network data of an online workflow";
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
		if ( line.hasOption("state") && line.hasOption("contingency")) {
			Integer stateId = Integer.parseInt(line.getOptionValue("state"));
			String contingencyId = line.getOptionValue("contingency");
			List<LimitViolation> violations = onlinedb.getPostContingencyViolations(workflowId, stateId, contingencyId);
			if ( violations != null && !violations.isEmpty() ) {
				Table table = new Table(8, BorderStyle.CLASSIC_WIDE);
				StringWriter content = new StringWriter();
				CsvWriter cvsWriter = new CsvWriter(content, ',');
				printHeaders(table, cvsWriter);
				printValues(table, cvsWriter, stateId, contingencyId, violations);
				printOutput(table, content, line.hasOption("csv"));
				cvsWriter.close();
			} else
				System.out.println("\nNo post contingency violations for workflow "+workflowId+", contingency "+contingencyId+" and state "+stateId);			
		} else if ( line.hasOption("state") ) {
			Integer stateId = Integer.parseInt(line.getOptionValue("state"));
			Map<String, List<LimitViolation>> stateViolations = onlinedb.getPostContingencyViolations(workflowId, stateId);
			if ( stateViolations != null && !stateViolations.keySet().isEmpty() ) { 
				Table table = new Table(8, BorderStyle.CLASSIC_WIDE);
				StringWriter content = new StringWriter();
				CsvWriter cvsWriter = new CsvWriter(content, ',');
				printHeaders(table, cvsWriter);
				String[] contingencyIds = stateViolations.keySet().toArray(new String[stateViolations.keySet().size()]);
				Arrays.sort(contingencyIds);
				for(String contingencyId : contingencyIds) {
					List<LimitViolation> violations = stateViolations.get(contingencyId);
					if ( violations != null && !violations.isEmpty() ) {
						printValues(table, cvsWriter, stateId, contingencyId, violations);
					}
				}
				cvsWriter.flush();
				printOutput(table, content, line.hasOption("csv"));
				cvsWriter.close();
			} else
				System.out.println("\nNo post contingency violations for workflow "+workflowId+" and state "+stateId);
		} else if ( line.hasOption("contingency") ) {
			String contingencyId = line.getOptionValue("contingency");
			Map<Integer, List<LimitViolation>> contingencyViolations = onlinedb.getPostContingencyViolations(workflowId, contingencyId);
			if ( contingencyViolations != null && !contingencyViolations.keySet().isEmpty() ) { 
				Table table = new Table(8, BorderStyle.CLASSIC_WIDE);
				StringWriter content = new StringWriter();
				CsvWriter cvsWriter = new CsvWriter(content, ',');
				printHeaders(table, cvsWriter);
				Integer[] stateIds = contingencyViolations.keySet().toArray(new Integer[contingencyViolations.keySet().size()]);
				Arrays.sort(stateIds);
				for(Integer stateId : stateIds) {
					List<LimitViolation> violations = contingencyViolations.get(stateId);
					if ( violations != null && !violations.isEmpty() ) {
						printValues(table, cvsWriter, stateId, contingencyId, violations);
					}
				}
				cvsWriter.flush();
				printOutput(table, content, line.hasOption("csv"));
				cvsWriter.close();
			} else
				System.out.println("\nNo post contingency violations for workflow "+workflowId+" and contingency "+contingencyId);
		} else {		
			Map<Integer, Map<String, List<LimitViolation>>> wfViolations = onlinedb.getPostContingencyViolations(workflowId);
			if ( wfViolations != null && !wfViolations.keySet().isEmpty() ) { 
				Table table = new Table(8, BorderStyle.CLASSIC_WIDE);
				StringWriter content = new StringWriter();
				CsvWriter cvsWriter = new CsvWriter(content, ',');
				printHeaders(table, cvsWriter);
				Integer[] stateIds = wfViolations.keySet().toArray(new Integer[wfViolations.keySet().size()]);
				Arrays.sort(stateIds);
				for(Integer stateId : stateIds) {
					Map<String, List<LimitViolation>> stateViolations = wfViolations.get(stateId);
					if ( stateViolations != null && !stateViolations.keySet().isEmpty() ) {
						String[] contingencyIds = stateViolations.keySet().toArray(new String[stateViolations.keySet().size()]);
						Arrays.sort(contingencyIds);
						for(String contingencyId : contingencyIds) {
							List<LimitViolation> violations = stateViolations.get(contingencyId);
							if ( violations != null && !violations.isEmpty() ) {
								printValues(table, cvsWriter, stateId, contingencyId, violations);
							}
						}
					}
				}
				cvsWriter.flush();
				printOutput(table, content, line.hasOption("csv"));
				cvsWriter.close();
			} else
				System.out.println("\nNo post contingency violations for workflow "+workflowId);
		}
		onlinedb.close();
	}
	
	private void printHeaders(Table table, CsvWriter cvsWriter) throws IOException {
		String[] headers = new String[8];
		int i = 0;
        table.addCell("State", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "State";
        table.addCell("Contingency", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "Contingency";
        table.addCell("Equipment", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "Equipment";
        table.addCell("Type", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "Type";
        table.addCell("Value", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "Value";
        table.addCell("Limit", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "Limit";
        table.addCell("Limit Reduction", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "Limit Reduction";
        table.addCell("Voltage Level", new CellStyle(CellStyle.HorizontalAlign.center));
        headers[i++] = "Voltage Level";
        cvsWriter.writeRecord(headers);
	}
	
	private void printValues(Table table, CsvWriter cvsWriter, Integer stateId, String contingencyId, List<LimitViolation> violations) throws IOException {
		Collections.sort(violations, new Comparator<LimitViolation>() {
		    public int compare(LimitViolation o1, LimitViolation o2) {
		        return o1.getSubject().getId().compareTo(o2.getSubject().getId());
		    }
		});
		for (LimitViolation violation : violations) {
			String[] values = new String[8];
			int i = 0;
			table.addCell(Integer.toString(stateId), new CellStyle(CellStyle.HorizontalAlign.right));
			values[i++] = Integer.toString(stateId);
			table.addCell(contingencyId, new CellStyle(CellStyle.HorizontalAlign.left));
			values[i++] = contingencyId;
			table.addCell(violation.getSubject().getId(), new CellStyle(CellStyle.HorizontalAlign.left));
			values[i++] = violation.getSubject().getId();
			table.addCell(violation.getLimitType().name(), new CellStyle(CellStyle.HorizontalAlign.left));
			values[i++] = violation.getLimitType().name();
			table.addCell(Float.toString(violation.getValue()), new CellStyle(CellStyle.HorizontalAlign.right));
			values[i++] = Float.toString(violation.getValue());
			table.addCell(Float.toString(violation.getLimit()), new CellStyle(CellStyle.HorizontalAlign.right));
			values[i++] = Float.toString(violation.getLimit());
			table.addCell(Float.toString(violation.getLimitReduction()), new CellStyle(CellStyle.HorizontalAlign.right));
			values[i++] = Float.toString(violation.getLimitReduction());
			table.addCell(Float.toString(violation.getBaseVoltage()), new CellStyle(CellStyle.HorizontalAlign.right));
			values[i++] = Float.toString(violation.getBaseVoltage());
			cvsWriter.writeRecord(values);
		}
	}
	
	private void printOutput(Table table, StringWriter content, boolean csv) {
		if ( csv )
			System.out.println(content.toString());
		else
			System.out.println(table.render());
	}
	
}
