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

import java.util.List;

/**
 *
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
            options.addOption(Option.builder().longOpt("parameters")
                    .desc("print the workflow parameters")
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
        if ( line.hasOption("basecase") ) {
            DateTime basecaseDate = DateTime.parse(line.getOptionValue("basecase"));
            workflows = onlinedb.listWorkflows(basecaseDate);
        } else if ( line.hasOption("basecases-interval") ) {
            Interval basecasesInterval = Interval.parse(line.getOptionValue("basecases-interval"));
            workflows = onlinedb.listWorkflows(basecasesInterval);
        } else
            workflows = onlinedb.listWorkflows();	
        boolean printParameters = line.hasOption("parameters");
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        Table table = new Table(2, BorderStyle.CLASSIC_WIDE);
        if ( printParameters )
            table = new Table(3, BorderStyle.CLASSIC_WIDE);
        table.addCell("ID", new CellStyle(CellStyle.HorizontalAlign.center));
        table.addCell("Date", new CellStyle(CellStyle.HorizontalAlign.center));
        if ( printParameters )
            table.addCell("Parameters", new CellStyle(CellStyle.HorizontalAlign.center));
        for (OnlineWorkflowDetails workflow : workflows) {
            table.addCell(workflow.getWorkflowId());
            table.addCell(formatter.print(workflow.getWorkflowDate()));
            if ( printParameters ) {
                OnlineWorkflowParameters parameters = onlinedb.getWorkflowParameters(workflow.getWorkflowId());
                if ( parameters != null ) {
                    table.addCell("Basecase = "+parameters.getBaseCaseDate().toString());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Time Horizon = "+parameters.getTimeHorizon().getName());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("FE Analysis Id = "+parameters.getFeAnalysisId());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Offline Workflow Id = "+parameters.getOfflineWorkflowId());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Historical Interval = "+parameters.getHistoInterval().toString());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("States = "+Integer.toString(parameters.getStates()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Rules Purity Threshold = "+Double.toString(parameters.getRulesPurityThreshold()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Store States = "+Boolean.toString(parameters.storeStates()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Analyse Basecase = "+Boolean.toString(parameters.analyseBasecase()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Validation = "+Boolean.toString(parameters.validation()));
                    table.addCell(" ");
                    table.addCell(" ");
                    String securityRulesString = parameters.getSecurityIndexes()==null ? "ALL" : parameters.getSecurityIndexes().toString();
                    table.addCell("Security Rules = "+securityRulesString);
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Case Type = "+parameters.getCaseType());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Countries = "+parameters.getCountries().toString());
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Limits Reduction = "+Float.toString(parameters.getLimitReduction()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Handle Violations in N = "+Boolean.toString(parameters.isHandleViolationsInN()));
                    table.addCell(" ");
                    table.addCell(" ");
                    table.addCell("Constrain Margin = "+Float.toString(parameters.getConstraintMargin()));
                } else {
                    table.addCell("-");
                }
            }
        }
        System.out.println(table.render());
        onlinedb.close();
    }

}
