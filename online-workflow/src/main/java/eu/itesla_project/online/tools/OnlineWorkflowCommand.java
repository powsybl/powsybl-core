/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import eu.itesla_project.commons.tools.Command;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineWorkflowCommand implements Command {

    public static final OnlineWorkflowCommand INSTANCE = new OnlineWorkflowCommand();

    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String START_CMD = "start-workflow";
    public static final String SHUTDOWN_CMD = "shutdown";
    public static final String BASE_CASE = "base-case";
    public static final String TIME_HORIZON = "time-horizon";
    public static final String STATES = "states";
    public static final String WORKFLOW_ID = "workflow";
    public static final String HISTODB_INTERVAL = "histodb-interval";
    public static final String THREADS = "threads";
    public static final String FEANALYSIS_ID = "fe-analysis-id";
    public static final String RULES_PURITY = "rules-purity";
    public static final String STORE_STATES = "store-states";
    public static final String ANALYSE_BASECASE = "analyse-basecase";
    public static final String VALIDATION = "validation";
    public static final String SECURITY_INDEXES = "security-indexes";
    public static final String BASECASES_INTERVAL = "basecases-interval";
    public static final String CASE_TYPE = "case-type";
    public static final String COUNTRIES = "countries";
    public static final String MERGE_OPTIMIZED = "merge-optimized";
    public static final String LIMIT_REDUCTION = "limits-reduction";
    public static final String HANDLE_VIOLATION_IN_N = "handle-violations";
    public static final String CONSTRAINT_MARGIN = "constraint-margin";
    public static final String CASE_FILE = "case-file";

    @Override
    public String getName() {
        return "online-workflow-control";
    }

    @Override
    public String getTheme() {
        return Themes.ONLINE_WORKFLOW;
    }

    @Override
    public String getDescription() {
        return "Online workflow application control";
    }

    @Override
    public Options getOptions() {
        Options opts = new Options();

        OptionGroup og = new OptionGroup();
        og.setRequired(true);

        og.addOption(Option.builder().longOpt(START_CMD)
                .desc("start new online workflow")
                .build());

        og.addOption(Option.builder().longOpt(SHUTDOWN_CMD)
                .desc("shutdown online workflow application")
                .build());
        opts.addOptionGroup(og);

        opts.addOption(Option.builder().longOpt(HOST)
                .desc("jmx host")
                .hasArg()
                .argName("HOST")
                .build());
        opts.addOption(Option.builder().longOpt(PORT)
                .desc("jmx port")
                .hasArg()
                .argName("PORT")
                .build());

        opts.addOption(Option.builder().longOpt(BASE_CASE)
                .desc("Base case")
                .hasArg()
                .argName(BASE_CASE)
                .build());

        opts.addOption(Option.builder().longOpt(TIME_HORIZON)
                .desc("time horizon for the online analysis")
                .hasArg()
                .argName(TIME_HORIZON)
                .build());

        opts.addOption(Option.builder().longOpt(BASE_CASE)
                .desc("Basecase to be analyzed")
                .hasArg()
                .argName(BASE_CASE)
                .build());

        opts.addOption(Option.builder().longOpt(STATES)
                .desc("States number")
                .hasArg()
                .argName(STATES)
                .build());

        opts.addOption(Option.builder().longOpt(WORKFLOW_ID)
                .desc("offline workflow id that produced the security rules")
                .hasArg()
                .argName(WORKFLOW_ID)
                .build());

        opts.addOption(Option.builder().longOpt(HISTODB_INTERVAL)
                .desc("interval of historical data to be used for WCA")
                .hasArg()
                .argName(HISTODB_INTERVAL)
                .build());

        opts.addOption(Option.builder().longOpt(THREADS)
                .desc("Executing threads number")
                .hasArg()
                .argName(THREADS)
                .build());

        opts.addOption(Option.builder().longOpt(FEANALYSIS_ID)
                .desc("id of the forecast error analysis")
                .hasArg()
                .argName(FEANALYSIS_ID)
                .build());

        opts.addOption(Option.builder().longOpt(RULES_PURITY)
                .desc("purity threshold for the security rules")
                .hasArg()
                .argName(RULES_PURITY)
                .build());

        opts.addOption(Option.builder().longOpt(STORE_STATES)
                .desc("store states")
                .build());

        opts.addOption(Option.builder().longOpt(ANALYSE_BASECASE)
                .desc("analyse basecase")
                .build());

        opts.addOption(Option.builder().longOpt(VALIDATION)
                .desc("validation")
                .build());

        opts.addOption(Option.builder().longOpt(SECURITY_INDEXES)
                .desc("sub list of security index types to use, use ALL for using all of them")
                .hasArg()
                .argName("INDEX_TYPE,INDEX_TYPE,...")
                .build());

        opts.addOption(Option.builder().longOpt(BASECASES_INTERVAL)
                .desc("interval for basecases to be considered; when set, overrides base-case parameter")
                .hasArg()
                .argName(BASECASES_INTERVAL)
                .build());

        opts.addOption(Option.builder().longOpt(CASE_TYPE)
                .desc("the type (FO/SN) of the base case")
                .hasArg()
                .argName(CASE_TYPE)
                .build());

        opts.addOption(Option.builder().longOpt(COUNTRIES)
                .desc("the countries of the base case, separated by comma")
                .hasArg()
                .argName("COUNTRY,COUNTRY,...")
                .build());

        opts.addOption(Option.builder().longOpt(MERGE_OPTIMIZED)
                .desc("merge optimized")
                .build());

        opts.addOption(Option.builder().longOpt(LIMIT_REDUCTION)
                .desc("limits reduction")
                .hasArg()
                .argName(LIMIT_REDUCTION)
                .build());

        opts.addOption(Option.builder().longOpt(HANDLE_VIOLATION_IN_N)
                .desc("handle violation in n")
                .build());

        opts.addOption(Option.builder().longOpt(CONSTRAINT_MARGIN)
                .desc("constraint margin")
                .hasArg()
                .argName(CONSTRAINT_MARGIN)
                .build());

        opts.addOption(Option.builder().longOpt(CASE_FILE)
                .desc("case file: Note: parameter " + CASE_FILE + "cannot be used together with parameters " + BASE_CASE + ", " + CASE_TYPE + ", " + COUNTRIES + ", " + BASECASES_INTERVAL)
                .hasArg()
                .argName(CASE_FILE)
                .build());

        return opts;
    }

    @Override
    public String getUsageFooter() {
        return null;
    }

}
