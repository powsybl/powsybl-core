/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.action.dsl.Rule;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.Security;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

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
    public void beforePreContingencyAnalysis(Network network) {
        out.println("Starting pre-contingency analysis");
    }

    @Override
    public void beforePostContingencyAnalysis(Contingency contingency) {
        out.println("Starting post-contingency '" + contingency.getId() + "' analysis");
    }

    @Override
    public void roundBegin(Contingency contingency, int round) {
        out.println("    Round " + round);
    }

    @Override
    public void loadFlowDiverged(Contingency contingency, Network network, int round) {
        out.println("    Load flow diverged");
    }

    @Override
    public void loadFlowConverged(Contingency contingency, List<LimitViolation> violations, Network network, int round) {
        if (!violations.isEmpty()) {
            out.println("        Violations:");
            out.println(Security.printLimitsViolations(violations, network, LoadFlowActionSimulator.NO_FILTER));
        }
    }

    public void ruleChecked(Contingency contingency, Rule rule, RuleEvaluationStatus status, Map<String, Object> variables, Map<String, Boolean> actions) {
        if (verbose || status == RuleEvaluationStatus.TRUE) {
            out.println("        Rule '" + rule.getId() + "' evaluated to " + status);
        }
        if (verbose) {
            if (variables.size() + actions.size() > 0) {
                Table table = new Table(3, BorderStyle.CLASSIC_WIDE);
                table.addCell("Variable");
                table.addCell("Value");
                variables.entrySet().forEach(e -> {
                    table.addCell(e.getKey());
                    table.addCell(Objects.toString(e.getValue()));
                });
                actions.entrySet().forEach(e -> {
                    table.addCell(e.getKey() + ".actionTaken");
                    table.addCell(e.getValue().toString());
                });
                out.println(table.render());
            }
        }
    }

    @Override
    public void beforeAction(Contingency contingency, String actionId) {
        out.println("        Applying action '" + actionId + "'");
    }

    @Override
    public void noMoreViolations(Contingency contingency) {
        out.println("        No more violation");
    }

    @Override
    public void violationsAnymoreAndNoRulesMatch(Contingency contingency) {
        err.println("        Still some violations and no rule match...");
    }
}
