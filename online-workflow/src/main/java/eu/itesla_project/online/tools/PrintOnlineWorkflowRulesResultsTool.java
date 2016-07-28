/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
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
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.online.OnlineWorkflowRulesResults;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class PrintOnlineWorkflowRulesResultsTool implements Tool {
	
	private static String NO_RULES_AVAILABLE = "NO_RULES_AVAILABLE";
	private static String INVALID_RULE = "Invalid Rule";

    private static Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "print-online-workflow-rules-results";
        }

        @Override
        public String getTheme() {
            return Themes.ONLINE_WORKFLOW;
        }

        @Override
        public String getDescription() {
            return "Print stored results of security rules application for an online workflow";
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
            options.addOption(Option.builder().longOpt("wca")
                    .desc("get results of wca rules (monte_carlo if not specified)")
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
        OnlineWorkflowRulesResults wfRulesResults = onlinedb.getRulesResults(workflowId);
        if ( line.hasOption("wca"))
            wfRulesResults = onlinedb.getWcaRulesResults(workflowId);
        if ( wfRulesResults != null ) {
            if ( !wfRulesResults.getContingenciesWithSecurityRulesResults().isEmpty() ) {
                OnlineWorkflowParameters parameters = onlinedb.getWorkflowParameters(workflowId);
                SecurityIndexType[] securityIndexTypes = parameters.getSecurityIndexes() == null ? SecurityIndexType.values()
                        : parameters.getSecurityIndexes().toArray(new SecurityIndexType[parameters.getSecurityIndexes().size()]);
                Table table = new Table(securityIndexTypes.length+3, BorderStyle.CLASSIC_WIDE);
                StringWriter content = new StringWriter();
                CsvWriter cvsWriter = new CsvWriter(content, ',');
                String[] headers = new String[securityIndexTypes.length+3];
                int i = 0;
                table.addCell("Contingency", new CellStyle(CellStyle.HorizontalAlign.center));
                headers[i++] = "Contingency";
                table.addCell("State", new CellStyle(CellStyle.HorizontalAlign.center));
                headers[i++] = "State";
                table.addCell("Status", new CellStyle(CellStyle.HorizontalAlign.center));
                headers[i++] = "Status";
                for (SecurityIndexType securityIndexType : securityIndexTypes) {
                    table.addCell(securityIndexType.getLabel(), new CellStyle(CellStyle.HorizontalAlign.center));
                    headers[i++] = securityIndexType.getLabel();
                }
                cvsWriter.writeRecord(headers);
                for (String contingencyId : wfRulesResults.getContingenciesWithSecurityRulesResults()) {
                    for (Integer stateId : wfRulesResults.getStatesWithSecurityRulesResults(contingencyId)) {
                        String[] values = new String[securityIndexTypes.length+3];
                        i = 0;
                        table.addCell(contingencyId);
                        values[i++] = contingencyId;
                        table.addCell(stateId.toString(), new CellStyle(CellStyle.HorizontalAlign.right));
                        values[i++] = stateId.toString();
                        if ( wfRulesResults.areValidRulesAvailable(contingencyId, stateId) ) {
                            table.addCell(wfRulesResults.getStateStatus(contingencyId, stateId).name());
                            values[i++] = wfRulesResults.getStateStatus(contingencyId, stateId).name();
                        } else {
                            table.addCell(NO_RULES_AVAILABLE);
                            values[i++] = NO_RULES_AVAILABLE;
                        }
                        HashMap<String, String> rulesResults = getRulesResults(wfRulesResults.getStateResults(contingencyId, stateId), securityIndexTypes, 
                                wfRulesResults.getInvalidRules(contingencyId, stateId));
                        for (SecurityIndexType securityIndexType : securityIndexTypes) {
                            table.addCell(rulesResults.get(securityIndexType.getLabel()), new CellStyle(CellStyle.HorizontalAlign.center));
                            values[i++] = rulesResults.get(securityIndexType.getLabel());
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
                System.out.println("\nNo results of security rules applications for this workflow");
        } else
            System.out.println("No results for this workflow");
        onlinedb.close();
    }

    private HashMap<String, String> getRulesResults(Map<String,Boolean> stateResults, SecurityIndexType[] securityIndexTypes, List<SecurityIndexType> invalidRules) {
        HashMap<String, String> rulesResults = new HashMap<String, String>();
        for (SecurityIndexType securityIndexType : securityIndexTypes) {
            if ( stateResults.containsKey(securityIndexType.getLabel()) )
                rulesResults.put(securityIndexType.getLabel(), stateResults.get(securityIndexType.getLabel()) ? "Safe" : "Unsafe");
            else if ( invalidRules.contains(securityIndexType) )
                rulesResults.put(securityIndexType.getLabel(), INVALID_RULE);
            else
                rulesResults.put(securityIndexType.getLabel(), "-");
        }
        return rulesResults;
    }

}
