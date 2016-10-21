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
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.online.*;
import eu.itesla_project.security.LimitViolation;
import eu.itesla_project.online.OnlineTaskStatus;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class PrintOnlineWorkflowSummaryTable implements Tool {

	private static final String EMPTY_CONTINGENCY_ID = "Empty-Contingency";
	
	private static Command COMMAND = new Command() {
		
		@Override
		public String getName() {
			return "print-online-workflow-summary";
		}

		@Override
		public String getTheme() {
			return Themes.ONLINE_WORKFLOW;
		}

		@Override
		public String getDescription() {
			return "Print a summary table containing the data of an online workflow";
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
			options.addOption(Option.builder().longOpt("output-file")
                    .desc("the ouptput file")
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
        Path outputCsvFile = Paths.get(line.getOptionValue("output-file"));
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
        System.out.println("Printing violations and failues of workflows " + workflowsIds);
        try (FileWriter content = new FileWriter(outputCsvFile.toFile())) {
			CsvWriter cvsWriter = null;
			try {
				// print headers
				cvsWriter = new CsvWriter(content, ',');
				String[] headers = new String[]{"WorkflowId", 
												"Basecase", 
												"Contingency",
												"State", 
												"FailureStep", 
												"FailureDescription",
												"ViolationType",
												"Violation",
												"ViolationStep",
												"Equipment", 
												"Value", 
												"Limit"};
				cvsWriter.writeRecord(headers);
				// cycle over the workflows
				for (String workflowId : workflowsIds) {
					System.out.println("Printing violations and failures of workflow " + workflowId);
					Network basecase = onlinedb.getState(workflowId, 0);
					String basecaseId = basecase.getId(); 
					// print pre-contingency violations
					printPrecontingencyViolations(workflowId, basecaseId, onlinedb, cvsWriter);
					// print contingencies violations
					printContingenciesViolations(workflowId, basecaseId, onlinedb, basecase, cvsWriter);
				}
				cvsWriter.flush();
			} catch (IOException e) {
				throw e;
			} finally {
				if ( cvsWriter!=null )
					cvsWriter.close();
				onlinedb.close();
			}
		} catch (IOException e1) {
			throw e1;
		}
	}
	
	private void printPrecontingencyViolations(String workflowId, String basecaseId, OnlineDb onlinedb, CsvWriter cvsWriter) throws IOException {
		Map<Integer, Map<OnlineStep, List<LimitViolation>>> wfViolations = onlinedb.getViolations(workflowId);
		Map<Integer, ? extends StateProcessingStatus> statesProcessingStatus = onlinedb.getStatesProcessingStatus(workflowId);
		if ( wfViolations != null && !wfViolations.keySet().isEmpty() ) { 
			Integer[] stateIds = wfViolations.keySet().toArray(new Integer[wfViolations.keySet().size()]);
			Arrays.sort(stateIds);
			for(Integer stateId : stateIds) {
				StateProcessingStatus stateprocessingStatus = statesProcessingStatus.get(stateId);
				if ( stateprocessingStatus != null 
					 && stateprocessingStatus.getStatus() != null
					 && !stateprocessingStatus.getStatus().isEmpty() ) {
					for(String step : stateprocessingStatus.getStatus().keySet()) {
						if ( OnlineTaskStatus.valueOf(stateprocessingStatus.getStatus().get(step)) == OnlineTaskStatus.FAILED ) {
							String[] values = new String[12];
							int i = 0;
							values[i++] = workflowId;
							values[i++] = basecaseId;
							values[i++] = EMPTY_CONTINGENCY_ID;
							values[i++] = Integer.toString(stateId);
							values[i++] = step;
							values[i++] = stateprocessingStatus.getDetail();
							values[i++] = "";
							values[i++] = "";
							values[i++] = "";
							values[i++] = "";
							values[i++] = "";
							values[i++] = "";
							cvsWriter.writeRecord(values);
							break;
						}
					}
				}
				Map<OnlineStep, List<LimitViolation>> stateViolations = wfViolations.get(stateId);
				if ( stateViolations != null && !stateViolations.keySet().isEmpty() ) {
					OnlineStep[] steps = stateViolations.keySet().toArray(new OnlineStep[stateViolations.keySet().size()]);
					Arrays.sort(steps);
					for(OnlineStep step : steps) {
						List<LimitViolation> violations = stateViolations.get(step);
						printViolations(workflowId, basecaseId, EMPTY_CONTINGENCY_ID, stateId, step, violations, cvsWriter);
					}
				}
			}
		}
	}
	
	private void printViolations(String workflowId, String basecaseId, String contingencyId, Integer stateId, OnlineStep step,
								 List<LimitViolation> violations, CsvWriter cvsWriter) throws IOException {
		if (violations != null && !violations.isEmpty()) {
			Collections.sort(violations, new Comparator<LimitViolation>() {
				public int compare(LimitViolation o1, LimitViolation o2) {
					return o1.getLimitType().compareTo(o2.getLimitType());
				}
			});
			for (LimitViolation violation : violations) {
				if (violations != null && !violations.isEmpty()) {
					String[] values = new String[12];
					int i = 0;
					values[i++] = workflowId;
					values[i++] = basecaseId;
					values[i++] = contingencyId;
					values[i++] = Integer.toString(stateId);
					values[i++] = "";
					values[i++] = "";
					values[i++] = ViolationType.STEADY_STATE.name();
					values[i++] = violation.getLimitType().name();
					values[i++] = step.name();
					values[i++] = violation.getSubject().getId();
					values[i++] = Float.toString(violation.getValue());
					values[i++] = Float.toString(violation.getLimit());
					cvsWriter.writeRecord(values);
				}
			}
		}
	}
	
	private void printContingenciesViolations(String workflowId, String basecaseId, OnlineDb onlinedb, 
											  Network basecase, CsvWriter cvsWriter) throws IOException {
		int states = onlinedb.getWorkflowParameters(workflowId).getStates();
		OnlineWorkflowRulesResults wfWcaRulesResults = onlinedb.getWcaRulesResults(workflowId);
		OnlineWorkflowRulesResults wfMclaRulesResults = onlinedb.getRulesResults(workflowId);
		OnlineWorkflowResults wfResults = onlinedb.getResults(workflowId);
		Map<Integer, Map<String, List<LimitViolation>>> wfViolations = onlinedb.getPostContingencyViolations(workflowId);
		Map<Integer, Map<String, Boolean>> loadflowConvergence = onlinedb.getPostContingencyLoadflowConvergence(workflowId);
		List<String> contingencies = new ArrayList<String>();
		OnlineWorkflowWcaResults wcaResults = onlinedb.getWcaResults(workflowId); 
		if ( wcaResults != null && wcaResults.getContingencies() != null )
			contingencies.addAll(wcaResults.getContingencies());
		Collections.sort(contingencies, new Comparator<String>() {
		    public int compare(String o1, String o2) {
		        return o1.compareTo(o2);
		    }
		});
		for (String contingency : contingencies) {
			for (int stateId = 0; stateId < states; stateId++) {
				printFailures(loadflowConvergence, workflowId, basecaseId, contingency, stateId, cvsWriter);
				printRulesResults(wfWcaRulesResults, workflowId, basecaseId, contingency, stateId, ViolationType.WCA_RULE, cvsWriter);
				printRulesResults(wfMclaRulesResults, workflowId, basecaseId, contingency, stateId, ViolationType.MCLA_RULE, cvsWriter);
				printSimulationResults(wfResults, workflowId, basecaseId, contingency, stateId, ViolationType.SECURITY_INDEX, cvsWriter);
				printPostcontingencyViolations(workflowId, basecaseId, contingency, stateId, wfViolations, cvsWriter);
			}
		}
	}

	
	private void printFailures(Map<Integer, Map<String, Boolean>> loadflowConvergence, String workflowId, String basecaseId, 
								   String contingencyId, Integer stateId, CsvWriter cvsWriter) throws IOException {
		if ( loadflowConvergence != null
			 && !loadflowConvergence.isEmpty()
			 && loadflowConvergence.containsKey(stateId)
			 && !loadflowConvergence.get(stateId).isEmpty()
			 && loadflowConvergence.get(stateId).containsKey(contingencyId)) {
			if ( !loadflowConvergence.get(stateId).get(contingencyId) ) {
				String[] values = new String[12];
				int i = 0;
				values[i++] = workflowId;
				values[i++] = basecaseId;
				values[i++] = contingencyId;
				values[i++] = Integer.toString(stateId);
				values[i++] = OnlineStep.POSTCONTINGENCY_LOAD_FLOW.name();
				values[i++] = "Post contingency load flow does not converge";
				values[i++] = "";
				values[i++] = "";
				values[i++] = "";
				values[i++] = "";
				values[i++] = "";
				values[i++] = "";
				cvsWriter.writeRecord(values);
			}
		}
	}
	
	private void printRulesResults(OnlineWorkflowRulesResults wfMclaRulesResults, String workflowId, String basecaseId, String contingencyId, 
								   Integer stateId, ViolationType violationType, CsvWriter cvsWriter) throws IOException {
		if ( wfMclaRulesResults != null 
			 && wfMclaRulesResults.getContingenciesWithSecurityRulesResults() != null
			 && !wfMclaRulesResults.getContingenciesWithSecurityRulesResults().isEmpty() 
			 && wfMclaRulesResults.getContingenciesWithSecurityRulesResults().contains(contingencyId)
			 && wfMclaRulesResults.getStateResults(contingencyId, stateId) != null
			 && !wfMclaRulesResults.getStateResults(contingencyId, stateId).isEmpty()) {
			for (String index : wfMclaRulesResults.getStateResults(contingencyId, stateId).keySet()) {
				if ( !wfMclaRulesResults.getStateResults(contingencyId, stateId).get(index) ) {
					String[] values = new String[12];
					int i=0;
					values[i++] = workflowId;
					values[i++] = basecaseId;
					values[i++] = contingencyId;
					values[i++] = Integer.toString(stateId);
					values[i++] = "";
					values[i++] = "";
					values[i++] = violationType.name();
					values[i++] = index;
					values[i++] = OnlineStep.MONTE_CARLO_LIKE_APPROACH.name();
					values[i++] = "";
					values[i++] = "";
					values[i++] = "";
					cvsWriter.writeRecord(values);
				}
			}
		}
	}
	
	private void printSimulationResults(OnlineWorkflowResults wfResults, String workflowId, String basecaseId, String contingencyId, 
										Integer stateId, ViolationType violationType, CsvWriter cvsWriter) throws IOException {
		if (wfResults != null
			&& wfResults.getUnsafeContingencies() != null 
			&& !wfResults.getUnsafeContingencies().isEmpty()
			&& wfResults.getUnsafeContingencies().contains(contingencyId)
			&& wfResults.getIndexesData(contingencyId, stateId) != null
			&& !wfResults.getIndexesData(contingencyId, stateId).isEmpty()) {
			for (String index : wfResults.getIndexesData(contingencyId, stateId).keySet()) {
				if ( !wfResults.getIndexesData(contingencyId, stateId).get(index) ) {
					String[] values = new String[12];
					int i = 0;
					values[i++] = workflowId;
					values[i++] = basecaseId;
					values[i++] = contingencyId;
					values[i++] = Integer.toString(stateId);
					values[i++] = "";
					values[i++] = "";
					values[i++] = violationType.name();
					values[i++] = index;
					values[i++] = OnlineStep.TIME_DOMAIN_SIMULATION.name();
					values[i++] = "";
					values[i++] = "";
					values[i++] = "";
					cvsWriter.writeRecord(values);
				}
			}
		}
	}

	private void printPostcontingencyViolations(String workflowId, String basecaseId, String contingencyId, Integer stateId,
												Map<Integer, Map<String, List<LimitViolation>>> wfViolations, CsvWriter cvsWriter) throws IOException {
		if ( wfViolations!= null 
			 && !wfViolations.isEmpty()
			 && wfViolations.containsKey(stateId)
			 && wfViolations.get(stateId) != null 
			 && !wfViolations.get(stateId).isEmpty()
			 && wfViolations.get(stateId).containsKey(contingencyId)) {
			List<LimitViolation> violations = wfViolations.get(stateId).get(contingencyId);
			printViolations(workflowId, basecaseId, contingencyId, stateId, OnlineStep.POSTCONTINGENCY_LOAD_FLOW, violations, cvsWriter);
		}
	}
}

enum ViolationType {
	STEADY_STATE,
	WCA_RULE,
	MCLA_RULE,
	SECURITY_INDEX
}
