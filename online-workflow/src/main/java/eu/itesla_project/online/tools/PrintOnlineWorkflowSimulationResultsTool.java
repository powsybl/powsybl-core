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
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.online.OnlineWorkflowResults;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class PrintOnlineWorkflowSimulationResultsTool implements Tool {

	public static final String SECURITY_INDEXES ="security-indexes";

	private static Command COMMAND = new Command() {

		@Override
		public String getName() {
			return "print-online-workflow-simulation-results";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Print stored results of T-D simulation for an online workflow";
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
			options.addOption(Option.builder().longOpt(SECURITY_INDEXES)
					.desc("sub list of security index types to use, use ALL for using all of them")
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
		OnlineConfig config = OnlineConfig.load();
		OnlineDb onlinedb = config.getOnlineDbFactoryClass().newInstance().create();
		String workflowId = line.getOptionValue("workflow");
		OnlineWorkflowResults wfResults = onlinedb.getResults(workflowId);
		if ( wfResults != null ) {			
			if ( !wfResults.getUnsafeContingencies().isEmpty() ) {
				OnlineWorkflowParameters parameters = onlinedb.getWorkflowParameters(workflowId);
				SecurityIndexType[] securityIndexTypes =null;
				if (line.hasOption(SECURITY_INDEXES)) {
					Set<SecurityIndexType> securityIndexesTypeSet = Arrays.stream(line.getOptionValue(SECURITY_INDEXES).split(","))
							.map(SecurityIndexType::valueOf)
							.collect(Collectors.toSet());
					securityIndexTypes = securityIndexesTypeSet.toArray(new SecurityIndexType[securityIndexesTypeSet.size()]);
				} else {
					securityIndexTypes = parameters.getSecurityIndexes() == null ? SecurityIndexType.values()
							: parameters.getSecurityIndexes().toArray(new SecurityIndexType[parameters.getSecurityIndexes().size()]);
				}
				Table table = new Table(securityIndexTypes.length+2, BorderStyle.CLASSIC_WIDE);
				StringWriter content = new StringWriter();
				CsvWriter cvsWriter = new CsvWriter(content, ',');
				String[] headers = new String[securityIndexTypes.length+2];
				int i = 0;
		        table.addCell("Contingency", new CellStyle(CellStyle.HorizontalAlign.center));
		        headers[i++] = "Contingency";
		        table.addCell("State", new CellStyle(CellStyle.HorizontalAlign.center));
		        headers[i++] = "State";
		        for (SecurityIndexType securityIndexType : securityIndexTypes) {
		        	table.addCell(securityIndexType.getLabel(), new CellStyle(CellStyle.HorizontalAlign.center));
		        	headers[i++] = securityIndexType.getLabel();
		        }
		        cvsWriter.writeRecord(headers);
				for (String contingencyId : wfResults.getUnsafeContingencies()) {
					for (Integer stateId : wfResults.getUnstableStates(contingencyId)) {
						String[] values = new String[securityIndexTypes.length+2];
						i = 0;
						table.addCell(contingencyId);
						values[i++] = contingencyId;
						table.addCell(stateId.toString(), new CellStyle(CellStyle.HorizontalAlign.right));
						values[i++] = stateId.toString();
						HashMap<String, String> indexesValues = getIndexesValues(wfResults.getIndexesData(contingencyId, stateId), securityIndexTypes);
						for (SecurityIndexType securityIndexType : securityIndexTypes) {
							table.addCell(indexesValues.get(securityIndexType.getLabel()), new CellStyle(CellStyle.HorizontalAlign.center));
							values[i++] = indexesValues.get(securityIndexType.getLabel());
						}
						cvsWriter.writeRecord(values);
					}
				}
				cvsWriter.flush();
				if ( line.hasOption("csv"))
					System.out.println(content.toString());
				else
					System.out.println(table.render());
				cvsWriter.close();
			} else
				System.out.println("\nNo contingencies requiring T-D simulation");
		} else
			System.out.println("No results for this workflow");
		onlinedb.close();
	}
	
	private HashMap<String, String> getIndexesValues(Map<String,Boolean> securityIndexes, SecurityIndexType[] securityIndexTypes) {
		HashMap<String, String> indexesValues = new HashMap<String, String>();
		for (SecurityIndexType securityIndexType : securityIndexTypes) {
			if ( securityIndexes.containsKey(securityIndexType.getLabel()) )
				indexesValues.put(securityIndexType.getLabel(), securityIndexes.get(securityIndexType.getLabel()) ? "Safe" : "Unsafe");
			else
				indexesValues.put(securityIndexType.getLabel(), "-");
		}
		return indexesValues;
	}

}
