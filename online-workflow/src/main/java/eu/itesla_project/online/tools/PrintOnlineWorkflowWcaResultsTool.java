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
import eu.itesla_project.modules.online.OnlineWorkflowWcaResults;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.io.StringWriter;
import java.util.Objects;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class PrintOnlineWorkflowWcaResultsTool implements Tool {

	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "print-online-workflow-wca-results";
		}
	
		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}
	
		@Override
		public String getDescription() {
			return "Print stored results of Worst Case Approach for an online workflow";
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
		OnlineWorkflowWcaResults wfWcaResults = onlinedb.getWcaResults(workflowId);
		if ( wfWcaResults != null ) {
			if ( !wfWcaResults.getContingencies().isEmpty() ) {
				Table table = new Table(7, BorderStyle.CLASSIC_WIDE);
				StringWriter content = new StringWriter();
				CsvWriter cvsWriter = new CsvWriter(content, ',');
				String[] headers = new String[7];
				int i = 0;
		        table.addCell("Contingency", new CellStyle(CellStyle.HorizontalAlign.center));
		        headers[i++] = "Contingency";
		        table.addCell("Cluster 1", new CellStyle(CellStyle.HorizontalAlign.center));
		        headers[i++] = "Cluster 1";
		        table.addCell("Cluster 2", new CellStyle(CellStyle.HorizontalAlign.center));
		        headers[i++] = "Cluster 2";
		        table.addCell("Cluster 3", new CellStyle(CellStyle.HorizontalAlign.center));
		        headers[i++] = "Cluster 3";
		        table.addCell("Cluster 4", new CellStyle(CellStyle.HorizontalAlign.center));
		        headers[i++] = "Cluster 4";
		        table.addCell("Undefined", new CellStyle(CellStyle.HorizontalAlign.center));
		        headers[i++] = "Undefined";
		        table.addCell("Cause", new CellStyle(CellStyle.HorizontalAlign.center));
		        headers[i++] = "Cause";
		        cvsWriter.writeRecord(headers);
				for (String contingencyId : wfWcaResults.getContingencies()) {
					String[] values = new String[7];
					i = 0;
					table.addCell(contingencyId);
					values[i++] = contingencyId;
					int[] clusterIndexes = new int[]{1, 2, 3, 4, -1};
					for (int k = 0; k < clusterIndexes.length; k++) {
						if ( clusterIndexes[k] == wfWcaResults.getClusterIndex(contingencyId) ) {
							table.addCell("X", new CellStyle(CellStyle.HorizontalAlign.center));
							values[i++] = "X";
						} else {
							table.addCell("-", new CellStyle(CellStyle.HorizontalAlign.center));
							values[i++] = "-";
						}
					}
					table.addCell(Objects.toString(wfWcaResults.getCauses(contingencyId), " "), new CellStyle(CellStyle.HorizontalAlign.center));
					values[i++] = Objects.toString(wfWcaResults.getCauses(contingencyId), " ");
					cvsWriter.writeRecord(values);
				}
				cvsWriter.flush();
				if ( line.hasOption("csv"))
					System.out.println(content.toString());
				else
					System.out.println(table.render());
				cvsWriter.close();
			} else
				System.out.println("\nNo results of security rules applications for this workflow");
		} else
			System.out.println("No results for this workflow");
		onlinedb.close();
	}
	
}
