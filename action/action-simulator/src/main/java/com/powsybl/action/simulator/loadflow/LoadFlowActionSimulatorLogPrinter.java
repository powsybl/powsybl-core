/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.action.dsl.Rule;
import com.powsybl.commons.io.table.AsciiTableFormatter;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.Security;


import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowActionSimulatorLogPrinter extends DefaultLoadFlowActionSimulatorObserver {

    private final PrintStream out;

    private final PrintStream err;

    private final boolean verbose;

    public LoadFlowActionSimulatorLogPrinter(PrintStream out, PrintStream err, boolean verbose) {
        this.out = Objects.requireNonNull(out);
        this.err = Objects.requireNonNull(err);
        this.verbose = verbose;
    }

    @Override
    public void beforePreContingencyAnalysis(RunningContext runningContext) {
        out.println("Starting pre-contingency analysis");
    }

    @Override
    public void beforePostContingencyAnalysis(RunningContext runningContext) {
        out.println("Starting post-contingency '" + runningContext.getContingency().getId() + "' analysis");
    }

    @Override
    public void roundBegin(RunningContext runningContext) {
        out.println("    Round " + runningContext.getRound());
    }

    @Override
    public void loadFlowDiverged(RunningContext runningContext) {
        out.println("    Load flow diverged");
    }

    @Override
    public void loadFlowConverged(RunningContext runningContext, List<LimitViolation> violations) {
        if (!violations.isEmpty()) {
            out.println("        Violations:");
            out.println(Security.printLimitsViolations(violations, runningContext.getNetwork(), LoadFlowActionSimulator.NO_FILTER));
        }
    }

    @Override
    public void ruleChecked(RunningContext runningContext, Rule rule, RuleEvaluationStatus status, Map<String, Object> variables, Map<String, Boolean> actions) {
        if (verbose || status == RuleEvaluationStatus.TRUE) {
            out.println("        Rule '" + rule.getId() + "' evaluated to " + status);
        }
        if (verbose &&  (variables.size() + actions.size() > 0)) {
            AsciiTableFormatter formatter = new AsciiTableFormatter("myFormatter",2);

            try {
                formatter.writeCell("Variable");
                formatter.writeCell("Value");
                variables.forEach((key, value) -> {
                    try {
                        formatter.writeCell(key);
                        formatter.writeCell(Objects.toString(value));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                actions.forEach((key, value) -> {
                    try {
                        formatter.writeCell(key + ".actionTaken");
                        formatter.writeCell(value.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.println(formatter.getTable().render());
        }
    }

    @Override
    public void beforeAction(RunningContext runningContext, String actionId) {
        out.println("        Applying action '" + actionId + "'");
    }

    @Override
    public void beforeTest(RunningContext runningContext, String actionId) {
        out.println("        Testing action '" + actionId + "'");
    }

    @Override
    public void noMoreViolations(RunningContext runningContext) {
        out.println("        No more violation");
    }

    @Override
    public void violationsAnymoreAndNoRulesMatch(RunningContext runningContext) {
        err.println("        Still some violations and no rule match...");
    }

    @Override
    public void maxIterationsReached(RunningContext runningContext) {
        out.println("        Max number of iterations reached");
    }
}
