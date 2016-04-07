/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.csvreader.CsvWriter;
import com.google.auto.service.AutoService;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.OnlineWorkflowDetails;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.online.OnlineWorkflowResults;
import eu.itesla_project.modules.online.OnlineWorkflowRulesResults;
import eu.itesla_project.modules.online.OnlineWorkflowWcaResults;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class PrintOnlineWorkflowPerformances implements Tool {

	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "print-online-workflow-performances";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Print the performances ofan online workflow";
		}

		@Override
		public Options getOptions() {
			Options options = new Options();
			options.addOption(Option.builder().longOpt("workflow")
	                .desc("the workflow id")
	                .hasArg()
	                .argName("ID")
	                .build());
			options.addOption(Option.builder().longOpt("workflows")
	                .desc("the workflow ids, separated by ,")
	                .hasArg()
	                .argName("IDS")
	                .build());
			options.addOption(Option.builder().longOpt("basecase")
					.desc("the basecase")
					.hasArg()
					.argName("BASECASE")
					.build());
			options.addOption(Option.builder().longOpt("basecases-interval")
					.desc("the base cases interval")
					.hasArg()
					.argName("BASECASE")
					.build());
			options.addOption(Option.builder().longOpt("csv-file")
                    .desc("the ouptput CSV file")
                    .hasArg()
                    .required()
                    .argName("FILE")
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
		Path outputCsvFile = Paths.get(line.getOptionValue("csv-file"));
		OnlineConfig config = OnlineConfig.load();
		OnlineDb onlinedb = config.getOnlineDbFactoryClass().newInstance().create();
		List<String> workflowsIds = new ArrayList<String>();
        if ( line.hasOption("workflow") )
        	workflowsIds.add(line.getOptionValue("workflow"));
        else if ( line.hasOption("workflows") )
        	workflowsIds = Arrays.asList(line.getOptionValue("workflows").split(","));
        else if ( line.hasOption("basecase") ) {
        	DateTime basecaseDate = DateTime.parse(line.getOptionValue("basecase"));
        	workflowsIds = onlinedb.listWorkflows(basecaseDate).stream().map(OnlineWorkflowDetails::getWorkflowId).collect(Collectors.toList());
        } else if ( line.hasOption("basecases-interval") ) {
        	Interval basecasesInterval = Interval.parse(line.getOptionValue("basecases-interval"));
        	workflowsIds = onlinedb.listWorkflows(basecasesInterval).stream().map(OnlineWorkflowDetails::getWorkflowId).collect(Collectors.toList());
        } else {
        	System.out.println("You must specify workflow(s) or basecase(s)");
        	return;
        }
        Collections.sort(workflowsIds, new Comparator<String>() {
		    public int compare(String o1, String o2) {
		        return o1.compareTo(o2);
		    }
		});
        System.out.println("Printing performances of workflows " + workflowsIds);
        CsvWriter cvsWriter = null;
        try (FileWriter content = new FileWriter(outputCsvFile.toFile())) {
			cvsWriter = new CsvWriter(content, ',');
			String[] headers = new String[25];
			int i = 0;
			headers[i++] = "workflow_id";
			headers[i++] = "basecase";
			headers[i++] = "secure_contingencies";
			headers[i++] = "unsecure_contingencies";
			headers[i++] = "unsecure_contingencies_ratio";
			headers[i++] = "secure_contingencies_ratio";
			headers[i++] = "unsecure_secure_contingencies_ratio";
			headers[i++] = "wca_missed_alarms";
			headers[i++] = "wca_missed_alarms_lst";
			headers[i++] = "wca_false_alarms";
			headers[i++] = "wca_false_alarms_lst";
			headers[i++] = "wca_accuracy";
			headers[i++] = "wca_efficiency";
			headers[i++] = "mcla_missed_alarms";
			headers[i++] = "mcla_missed_alarms_lst";
			headers[i++] = "mcla_false_alarms";
			headers[i++] = "mcla_false_alarms_lst";
			headers[i++] = "mcla_accuracy";
			headers[i++] = "mcla_efficiency";
			headers[i++] = "wf_missed_alarms";
			headers[i++] = "wf_missed_alarms_lst";
			headers[i++] = "wf_false_alarms";
			headers[i++] = "wf_false_alarms_lst";
			headers[i++] = "wf_accuracy";
			headers[i++] = "wf_efficiency";
			cvsWriter.writeRecord(headers);
			// cycle over the workflows
			for (String workflowId : workflowsIds) {
				try {
					System.out.println("\nPrinting performances of workflow " + workflowId);
					OnlineWorkflowParameters parameters = onlinedb.getWorkflowParameters(workflowId);
					if ( parameters != null && parameters.validation() ) {
						OnlineWorkflowResults wfResults = onlinedb.getResults(workflowId);
						Map<String, Boolean> contigencySecure = new HashMap<String, Boolean>();
						Map<String, Boolean> contigencyWCASecure = new HashMap<String, Boolean>();
						Map<String, Boolean> contigencyMCLASecure = new HashMap<String, Boolean>();
						Map<String, Boolean> contigencyWfSecure = new HashMap<String, Boolean>();
						Map<String, Map<SecurityIndexType, Boolean>> contigencyPhenomenaSecure = new HashMap<String, Map<SecurityIndexType,Boolean>>();
						Map<String, Map<SecurityIndexType, Boolean>> contigencyPhenomenaMCLASecure = new HashMap<String, Map<SecurityIndexType,Boolean>>();
						int unsecureContingencies = 0;
						int secureContingencies = 0;
						int wcaFalseAlarms = 0;
						List<String> wcaFalseAlarmsList= new ArrayList<String>();
						int wcaMissedAlarms = 0;
						List<String> wcaMissedAlarmsList= new ArrayList<String>();
						int mclaFalseAlarms = 0;
						List<String> mclaFalseAlarmsList= new ArrayList<String>();
						int mclaMissedAlarms = 0;
						List<String> mclaMissedAlarmsList= new ArrayList<String>();
						int wfFalseAlarms = 0;
						List<String> wfFalseAlarmsList= new ArrayList<String>();
						int wfMissedAlarms = 0;
						List<String> wfMissedAlarmsList= new ArrayList<String>();
						if ( wfResults != null ) {		
							if ( !wfResults.getUnsafeContingencies().isEmpty() ) {
								Network basecase = onlinedb.getState(workflowId, 0);
								String basecaseId = basecase.getId();
								OnlineWorkflowWcaResults wfWcaResults = onlinedb.getWcaResults(workflowId);
								OnlineWorkflowRulesResults wfRulesResults = onlinedb.getRulesResults(workflowId);
								SecurityIndexType[] securityIndexTypes = parameters.getSecurityIndexes() == null ? SecurityIndexType.values()
						                    : parameters.getSecurityIndexes().toArray(new SecurityIndexType[parameters.getSecurityIndexes().size()]);
								for (String contingencyId : wfResults.getUnsafeContingencies()) {
									// initialize values
									contigencySecure.put(contingencyId, true);
									if ( wfWcaResults.getClusterIndex(contingencyId) == 1 )
										contigencyWCASecure.put(contingencyId, true);
									else
										contigencyWCASecure.put(contingencyId, false);
									contigencyMCLASecure.put(contingencyId, true);
									Map<SecurityIndexType, Boolean> phenomenaSecure = new HashMap<SecurityIndexType, Boolean>();
									Map<SecurityIndexType, Boolean> phenomenaMCLASecure = new HashMap<SecurityIndexType, Boolean>();
									for (SecurityIndexType securityIndexType : securityIndexTypes) {
										phenomenaSecure.put(securityIndexType, true);
										phenomenaMCLASecure.put(securityIndexType, true);
									}
									contigencyPhenomenaSecure.put(contingencyId, phenomenaSecure);
									contigencyPhenomenaMCLASecure.put(contingencyId, phenomenaMCLASecure);
									// compute values
									for (Integer stateId : wfResults.getUnstableStates(contingencyId)) {
										Map<String,Boolean> securityIndexes = wfResults.getIndexesData(contingencyId, stateId);
										Map<String,Boolean> securityRules = wfRulesResults.getStateResults(contingencyId, stateId);
										for (SecurityIndexType securityIndexType : securityIndexTypes) {
											if ( securityIndexes.containsKey(securityIndexType.getLabel()) && !securityIndexes.get(securityIndexType.getLabel()) ) {
												contigencySecure.put(contingencyId, false);
												contigencyPhenomenaSecure.get(contingencyId).put(securityIndexType, false);
											}
											if ( securityRules.containsKey(securityIndexType.getLabel()) && !securityRules.get(securityIndexType.getLabel()) ) {
												contigencyMCLASecure.put(contingencyId, false);
												contigencyPhenomenaMCLASecure.get(contingencyId).put(securityIndexType, false);
											}
										}
									}
									if ( contigencyWCASecure.get(contingencyId) 
										 || ( !contigencyWCASecure.get(contingencyId) && contigencyMCLASecure.get(contingencyId) ) )
										contigencyWfSecure.put(contingencyId, true);
									else
										contigencyWfSecure.put(contingencyId, false);
									// compute data for performances
									if ( contigencySecure.get(contingencyId) )
										secureContingencies++;
									else
										unsecureContingencies++;
									if ( !contigencySecure.get(contingencyId)  && contigencyWCASecure.get(contingencyId) ) {
										wcaMissedAlarms++;
										wcaMissedAlarmsList.add(contingencyId);
									}
									if ( contigencySecure.get(contingencyId)  && !contigencyWCASecure.get(contingencyId) ) {
										wcaFalseAlarms++;
										wcaFalseAlarmsList.add(contingencyId);
									}
									if ( !contigencySecure.get(contingencyId)  && contigencyMCLASecure.get(contingencyId) ) {
										mclaMissedAlarms++;
										mclaMissedAlarmsList.add(contingencyId);
									}
									if ( contigencySecure.get(contingencyId)  && !contigencyMCLASecure.get(contingencyId) ) {
										mclaFalseAlarms++;
										mclaFalseAlarmsList.add(contingencyId);
									}
									if ( !contigencySecure.get(contingencyId)  && contigencyWfSecure.get(contingencyId) ) {
										wfMissedAlarms++;
										wfMissedAlarmsList.add(contingencyId);
									}
									if ( contigencySecure.get(contingencyId)  && !contigencyWfSecure.get(contingencyId) ) {
										wfFalseAlarms++;
										wfFalseAlarmsList.add(contingencyId);
									}
								}
								// compute performances
								float wcaAccuracy = ( unsecureContingencies == 0 ) ? 100 : (1f - ((float)wcaMissedAlarms / (float)unsecureContingencies)) * 100f;
								float wcaEfficiency = ( secureContingencies == 0 ) ? 100 : (1f - ((float)wcaFalseAlarms / (float)secureContingencies)) * 100f;
								float mclaAccuracy = ( unsecureContingencies == 0 ) ? 100 : (1f - ((float)mclaMissedAlarms / (float)unsecureContingencies)) * 100f;
								float mclaEfficiency = ( secureContingencies == 0 ) ? 100 : (1f - ((float)mclaFalseAlarms / (float)secureContingencies)) * 100f;
								float wfAccuracy = ( unsecureContingencies == 0 ) ? 100 : (1f - ((float)wfMissedAlarms / (float)unsecureContingencies)) * 100f;
								float wfEfficiency = ( secureContingencies == 0 ) ? 100 : (1f - ((float)wfFalseAlarms / (float)secureContingencies)) * 100f;
								float unsecureRatio =  (float)unsecureContingencies / (float)(secureContingencies+unsecureContingencies);
								float secureRatio =  (float)secureContingencies / (float)(secureContingencies+unsecureContingencies);
								float secureUnsecureRatio = ( secureContingencies == 0 ) ? Float.NaN : (float)unsecureContingencies / (float)secureContingencies;
	//							System.out.println("contigencySecure: " + contigencySecure);
	//							System.out.println("contigencyWCASecure: " + contigencyWCASecure);
	//							System.out.println("contigencyMCLASecure: " + contigencyMCLASecure);
	//							System.out.println("contigencyWfSecure: " + contigencyWfSecure);
	//							System.out.println("contigencyPhenomenaSecure: " + contigencyPhenomenaSecure);
	//							System.out.println("contigencyPhenomenaMCLASecure: " + contigencyPhenomenaMCLASecure);
								// print performances
								String[] values = new String[25];
								i = 0;
								values[i++] = workflowId;
								values[i++] = basecaseId;
								values[i++] = Integer.toString(secureContingencies);
								values[i++] = Integer.toString(unsecureContingencies);
								values[i++] = Float.toString(unsecureRatio);
								values[i++] = Float.toString(secureRatio);
								values[i++] = Float.toString(secureUnsecureRatio);
								values[i++] = Integer.toString(wcaMissedAlarms);
								values[i++] = wcaMissedAlarmsList.toString();
								values[i++] = Integer.toString(wcaFalseAlarms);
								values[i++] = wcaFalseAlarmsList.toString();
								values[i++] = Float.toString(wcaAccuracy);
								values[i++] = Float.toString(wcaEfficiency);
								values[i++] = Integer.toString(mclaMissedAlarms);
								values[i++] = mclaMissedAlarmsList.toString();
								values[i++] = Integer.toString(mclaFalseAlarms);
								values[i++] = mclaFalseAlarmsList.toString();
								values[i++] = Float.toString(mclaAccuracy);
								values[i++] = Float.toString(mclaEfficiency);
								values[i++] = Integer.toString(wfMissedAlarms);
								values[i++] = wfMissedAlarmsList.toString();
								values[i++] = Integer.toString(wfFalseAlarms);
								values[i++] = wfFalseAlarmsList.toString();
								values[i++] = Float.toString(wfAccuracy);
								values[i++] = Float.toString(wfEfficiency);
								cvsWriter.writeRecord(values);
								cvsWriter.flush();
							} else
								System.out.println("No data for benchmark: skipping wf " + workflowId);
						} else {
							System.out.println("No results: skipping wf " + workflowId);
						}
					} else
						System.out.println("No data for validation: skipping wf " + workflowId);
				} catch (IOException e1) {
				}
			}
		} finally {
			if ( cvsWriter!=null )
				cvsWriter.close();
			onlinedb.close();
		}
	}
	

}
