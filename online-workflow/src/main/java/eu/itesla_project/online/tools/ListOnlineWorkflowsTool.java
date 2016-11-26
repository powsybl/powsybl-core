/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
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
import eu.itesla_project.modules.online.OnlineWorkflowDetails;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import net.sf.json.JSONSerializer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class ListOnlineWorkflowsTool implements Tool {

    private static Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "list-online-workflows";
        }

        @Override
        public String getTheme() {
            return Themes.ONLINE_WORKFLOW;
        }

        @Override
        public String getDescription() {
            return "list stored online workflows";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder().longOpt("basecase")
                    .desc("Base case")
                    .hasArg()
                    .argName("BASECASE")
                    .build());
            options.addOption(Option.builder().longOpt("basecases-interval")
                    .desc("Base cases interval")
                    .hasArg()
                    .argName("INTERVAL")
                    .build());
            options.addOption(Option.builder().longOpt("workflow")
                    .desc("Workflow id")
                    .hasArg()
                    .argName("ID")
                    .build());
            options.addOption(Option.builder().longOpt("parameters")
                    .desc("Print the workflow parameters")
                    .build());
            options.addOption(Option.builder().longOpt("json")
                    .desc("Export in a json file")
                    .hasArg()
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
        OnlineConfig config = OnlineConfig.load();
        OnlineDb onlinedb = config.getOnlineDbFactoryClass().newInstance().create();
        List<OnlineWorkflowDetails> workflows = null;
        if (line.hasOption("basecase")) {
            DateTime basecaseDate = DateTime.parse(line.getOptionValue("basecase"));
            workflows = onlinedb.listWorkflows(basecaseDate);
        } else if (line.hasOption("basecases-interval")) {
            Interval basecasesInterval = Interval.parse(line.getOptionValue("basecases-interval"));
            workflows = onlinedb.listWorkflows(basecasesInterval);
        } else if (line.hasOption("workflow")) {
            String workflowId = line.getOptionValue("workflow");
            OnlineWorkflowDetails workflowDetails = onlinedb.getWorkflowDetails(workflowId);
            workflows = new ArrayList<OnlineWorkflowDetails>();
            if (workflowDetails != null)
                workflows.add(workflowDetails);
        } else
            workflows = onlinedb.listWorkflows();
        boolean printParameters = line.hasOption("parameters");
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Table table = new Table(2, BorderStyle.CLASSIC_WIDE);
        if (printParameters)
            table = new Table(3, BorderStyle.CLASSIC_WIDE);
        List<Map<String, String>> jsonData = new ArrayList<Map<String, String>>();
        table.addCell("ID", new CellStyle(CellStyle.HorizontalAlign.center));
        table.addCell("Date", new CellStyle(CellStyle.HorizontalAlign.center));
        if (printParameters)
            table.addCell("Parameters", new CellStyle(CellStyle.HorizontalAlign.center));
        for (OnlineWorkflowDetails workflow : workflows) {
            Map<String, String> wfJsonData = new HashMap<String, String>();
            table.addCell(workflow.getWorkflowId());
            wfJsonData.put("id", workflow.getWorkflowId());
            table.addCell(formatter.print(workflow.getWorkflowDate()));
            wfJsonData.put("date", formatter.print(workflow.getWorkflowDate()));
            if (printParameters) {
                OnlineWorkflowParameters parameters = onlinedb.getWorkflowParameters(workflow.getWorkflowId());
                if (parameters != null) {
                    table.addCell("Basecase = " + parameters.getBaseCaseDate().toString());
                    wfJsonData.put(OnlineWorkflowCommand.BASE_CASE, parameters.getBaseCaseDate().toString());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Time Horizon = " + parameters.getTimeHorizon().getName());
                    wfJsonData.put(OnlineWorkflowCommand.TIME_HORIZON, parameters.getTimeHorizon().getName());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("FE Analysis Id = " + parameters.getFeAnalysisId());
                    wfJsonData.put(OnlineWorkflowCommand.FEANALYSIS_ID, parameters.getFeAnalysisId());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Offline Workflow Id = " + parameters.getOfflineWorkflowId());
                    wfJsonData.put(OnlineWorkflowCommand.WORKFLOW_ID, parameters.getOfflineWorkflowId());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Historical Interval = " + parameters.getHistoInterval().toString());
                    wfJsonData.put(OnlineWorkflowCommand.HISTODB_INTERVAL, parameters.getHistoInterval().toString());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("States = " + Integer.toString(parameters.getStates()));
                    wfJsonData.put(OnlineWorkflowCommand.STATES, Integer.toString(parameters.getStates()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Rules Purity Threshold = " + Double.toString(parameters.getRulesPurityThreshold()));
                    wfJsonData.put(OnlineWorkflowCommand.RULES_PURITY, Double.toString(parameters.getRulesPurityThreshold()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Store States = " + Boolean.toString(parameters.storeStates()));
                    wfJsonData.put(OnlineWorkflowCommand.STORE_STATES, Boolean.toString(parameters.storeStates()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Analyse Basecase = " + Boolean.toString(parameters.analyseBasecase()));
                    wfJsonData.put(OnlineWorkflowCommand.ANALYSE_BASECASE, Boolean.toString(parameters.analyseBasecase()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Validation = " + Boolean.toString(parameters.validation()));
                    wfJsonData.put(OnlineWorkflowCommand.VALIDATION, Boolean.toString(parameters.validation()));
                    table.addCell(" ");
                    table.addCell(" ");
                    String securityRulesString = parameters.getSecurityIndexes() == null ? "ALL" : parameters.getSecurityIndexes().toString();
                    table.addCell("Security Rules = " + securityRulesString);
                    wfJsonData.put(OnlineWorkflowCommand.SECURITY_INDEXES, securityRulesString);
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Case Type = " + parameters.getCaseType());
                    wfJsonData.put(OnlineWorkflowCommand.CASE_TYPE, parameters.getCaseType().name());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Countries = " + parameters.getCountries().toString());
                    wfJsonData.put(OnlineWorkflowCommand.COUNTRIES, parameters.getCountries().toString());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Limits Reduction = " + Float.toString(parameters.getLimitReduction()));
                    wfJsonData.put(OnlineWorkflowCommand.LIMIT_REDUCTION, Float.toString(parameters.getLimitReduction()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Handle Violations in N = " + Boolean.toString(parameters.isHandleViolationsInN()));
                    wfJsonData.put(OnlineWorkflowCommand.HANDLE_VIOLATION_IN_N, Boolean.toString(parameters.isHandleViolationsInN()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Constrain Margin = " + Float.toString(parameters.getConstraintMargin()));
                    wfJsonData.put(OnlineWorkflowCommand.CONSTRAINT_MARGIN, Float.toString(parameters.getConstraintMargin()));
                    if (parameters.getCaseFile() != null) {
                        table.addCell(" ");
                        table.addCell(" ");
                        table.addCell("Case file = " + parameters.getCaseFile());
                        wfJsonData.put(OnlineWorkflowCommand.CASE_FILE, parameters.getCaseFile());
                    }
                } else {
                    table.addCell("-");
                }
            }
            jsonData.add(wfJsonData);
        }
        if (line.hasOption("json")) {
            Path jsonFile = Paths.get(line.getOptionValue("json"));
            try (FileWriter jsonFileWriter = new FileWriter(jsonFile.toFile())) {
                //JSONSerializer.toJSON(jsonData).write(jsonFileWriter);
                jsonFileWriter.write(JSONSerializer.toJSON(jsonData).toString(3));
            }
        } else
            System.out.println(table.render());

        onlinedb.close();
    }

}
