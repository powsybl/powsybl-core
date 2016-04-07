/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import com.csvreader.CsvWriter;
import com.google.auto.service.AutoService;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.StateProcessingStatus;
import eu.itesla_project.online.OnlineTaskStatus;
import eu.itesla_project.online.OnlineTaskType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class PrintOnlineWorkflowProcessingStatusTool implements Tool {
	
	private static int COLUMN_LENGTH = 85;

	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "print-online-workflow-processing-status";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Print processing status (failed, success) for the different states of an online workflow";
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
		Map<Integer, ? extends StateProcessingStatus> statesProcessingStatus = onlinedb.getStatesProcessingStatus(workflowId);
		if ( statesProcessingStatus != null ) {			
			Table table = new Table(OnlineTaskType.values().length+2, BorderStyle.CLASSIC_WIDE);
			StringWriter content = new StringWriter();
			CsvWriter cvsWriter = new CsvWriter(content, ',');
			String[] headers = new String[OnlineTaskType.values().length+2];
			int i = 0;
	        table.addCell("State", new CellStyle(CellStyle.HorizontalAlign.center));
	        headers[i++] = "State";
	        for (OnlineTaskType taskType : OnlineTaskType.values()) {
	        	table.addCell(taskTypeLabel(taskType), new CellStyle(CellStyle.HorizontalAlign.center));
	        	headers[i++] = taskTypeLabel(taskType);
	        }
	        table.addCell("Detail", new CellStyle(CellStyle.HorizontalAlign.center));
	        headers[i++] = "Detail";
	        cvsWriter.writeRecord(headers);
			for (Integer stateId : statesProcessingStatus.keySet()) {
				String[] values = new String[OnlineTaskType.values().length+2];
				i = 0;
				table.addCell(stateId.toString(), new CellStyle(CellStyle.HorizontalAlign.right));
				values[i++] = stateId.toString();
				HashMap<String, String> stateProcessingStatus = getProcessingStatus(statesProcessingStatus.get(stateId).getStatus());
				for (OnlineTaskType taskType : OnlineTaskType.values()) {
					table.addCell(stateProcessingStatus.get(taskType.name()), new CellStyle(CellStyle.HorizontalAlign.center));
					values[i++] = stateProcessingStatus.get(taskType.name());
				}
//				table.addCell(statesProcessingStatus.get(stateId).getDetail().isEmpty() ? "-" : statesProcessingStatus.get(stateId).getDetail());
				String value = statesProcessingStatus.get(stateId).getDetail().isEmpty() ? "-" : statesProcessingStatus.get(stateId).getDetail();
				values[i++] = value;
				while ( value.length() > COLUMN_LENGTH ) {
					table.addCell(value.substring(0, COLUMN_LENGTH), new CellStyle(CellStyle.HorizontalAlign.left));
					for (int j = 0; j < OnlineTaskType.values().length+1; j++) {
						table.addCell(" ", new CellStyle(CellStyle.HorizontalAlign.left));
					}
					value = value.substring(COLUMN_LENGTH);
				}
				table.addCell(value, new CellStyle(CellStyle.HorizontalAlign.left));
				cvsWriter.writeRecord(values);				
			}
			cvsWriter.flush();
			if ( line.hasOption("csv"))
				System.out.println(content.toString());
			else
				System.out.println(table.render());
			cvsWriter.close();
		} else
			System.out.println("No status of the processing steps for this workflow");
		onlinedb.close();
	}
	
	private HashMap<String, String> getProcessingStatus(Map<String,String> processingStatus) {
		HashMap<String, String> completeProcessingStatus = new HashMap<String, String>();
		for (OnlineTaskType taskType : OnlineTaskType.values()) {
			if ( processingStatus.containsKey(taskType.name()) )
				switch ( OnlineTaskStatus.valueOf(processingStatus.get(taskType.name())) ) {
				case SUCCESS:
					completeProcessingStatus.put(taskType.name(), "OK");
					break;
				case FAILED:
					completeProcessingStatus.put(taskType.name(), "FAILED");
					break;
				default:
					completeProcessingStatus.put(taskType.name(), "-");
					break;
				}
			else
				completeProcessingStatus.put(taskType.name(), "-");
		}
		return completeProcessingStatus;
	}
	
	private String taskTypeLabel(OnlineTaskType taskType) {
		switch (taskType) {
		case SAMPLING:
			return "Montecarlo Sampling";
		case LOAD_FLOW:
			return "Loadflow";
		case SECURITY_RULES:
			return "Security Rules";
		case OPTIMIZER:
			return "Optimizer";
		case TIME_DOMAIN_SIM:
			return "T-D Simulation";
		default:
			return "-";
		}
	}

}
