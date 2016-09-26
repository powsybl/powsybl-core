/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.security;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.modules.rules.RulesDbClient;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Security {

    public Security() {
    }

    public enum CurrentLimitType {
        PATL,
        TATL
    }

    private static float getCurrentLimit(CurrentLimits limits, CurrentLimitType limitType, int maxAcceptableDuration) {
        float limit = Float.NaN;
        switch (limitType) {
            case PATL:
                limit = limits.getPermanentLimit();
                break;

            case TATL:
                for (CurrentLimits.TemporaryLimit tempoLimit : limits.getTemporaryLimits()) {
                    if (tempoLimit.getAcceptableDuration() <= maxAcceptableDuration) {
                        limit = tempoLimit.getLimit();
                        break;
                    }
                }
                break;

            default:
                throw new AssertionError();
        }
        return limit;
    }

    public static void checkCurrentLimits(TwoTerminalsConnectable branch, Terminal terminal, CurrentLimits limits,
                                          CurrentLimitType limitType,int maxAcceptableDuration, float limitReduction,
                                          List<LimitViolation> violations) {
        if (limits != null) {
            float i = terminal.getI();
            if (!Float.isNaN(i)) {
                float limit = getCurrentLimit(limits, limitType, maxAcceptableDuration);
                if (!Float.isNaN(limit)) {
                    if (i > limit * limitReduction) {
                        Country country = terminal == branch.getTerminal1() ? branch.getTerminal1().getVoltageLevel().getSubstation().getCountry()
                                : branch.getTerminal2().getVoltageLevel().getSubstation().getCountry();
                        float baseVoltage = Math.max(branch.getTerminal1().getVoltageLevel().getNominalV(),
                                                     branch.getTerminal2().getVoltageLevel().getNominalV());
                        violations.add(new LimitViolation(branch, LimitViolationType.CURRENT, limit, limitReduction, i, country, baseVoltage));
                    }
                }
            }
        }
    }

    public static void checkCurrentLimits(Iterable<? extends TwoTerminalsConnectable> branches, CurrentLimitType currentLimitType,
                                          int maxAcceptableDuration, float limitReduction, List<LimitViolation> violations) {
        for (TwoTerminalsConnectable branch : branches) {
            checkCurrentLimits(branch, branch.getTerminal1(), branch.getCurrentLimits1(), currentLimitType, maxAcceptableDuration, limitReduction, violations);
            checkCurrentLimits(branch, branch.getTerminal2(), branch.getCurrentLimits2(), currentLimitType, maxAcceptableDuration, limitReduction, violations);
        }
    }

    public static List<LimitViolation> checkLimits(Network network) {
        return checkLimits(network, CurrentLimitType.PATL, Integer.MAX_VALUE, 1f);
    }

    public static List<LimitViolation> checkLimits(Network network, CurrentLimitType currentLimitType, int maxAcceptableDuration, float limitReduction) {
        //if (limitReduction <= 0 || limitReduction > 1) {
        // allow to increase the limits
        if (limitReduction <= 0) {
            throw new IllegalArgumentException("Bad limit reduction " + limitReduction);
        }
        List<LimitViolation> violations = new ArrayList<>();
        checkCurrentLimits(network.getLines(), currentLimitType, maxAcceptableDuration, limitReduction, violations);
        checkCurrentLimits(network.getTwoWindingsTransformers(), currentLimitType, maxAcceptableDuration, limitReduction, violations);
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (!Float.isNaN(vl.getLowVoltageLimit())) {
                for (Bus b : vl.getBusView().getBuses()) {
                    if (!Float.isNaN(b.getV())) {
                        if (b.getV() < vl.getLowVoltageLimit()) {
                            violations.add(new LimitViolation(vl, LimitViolationType.LOW_VOLTAGE, vl.getLowVoltageLimit(),
                                    1, b.getV(), vl.getSubstation().getCountry(), vl.getNominalV()));
                        }
                    }
                }
            }
            if (!Float.isNaN(vl.getHighVoltageLimit())) {
                for (Bus b : vl.getBusView().getBuses()) {
                    if (!Float.isNaN(b.getV())) {
                        if (b.getV() > vl.getHighVoltageLimit()) {
                            violations.add(new LimitViolation(vl, LimitViolationType.HIGH_VOLTAGE, vl.getHighVoltageLimit(),
                                    1, b.getV(), vl.getSubstation().getCountry(), vl.getNominalV()));
                        }
                    }
                }
            }
        }
        return violations;
    }

    public static List<PostContingencyRuleIssue> checkPostContingencyRules(Network network, RulesDbClient rulesDb) {
        List<PostContingencyRuleIssue> issues = new ArrayList<>();
        // TODO
        return issues;
    }

    public static String printLimitsViolations(Network network) {
        return printLimitsViolations(network, LimitViolationFilter.load());
    }

    public static String printLimitsViolations(Network network, LimitViolationFilter filter) {
        return printLimitsViolations(checkLimits(network), filter);
    }

    public static String printLimitsViolations(List<LimitViolation> violations) {
        return printLimitsViolations(violations, LimitViolationFilter.load());
    }

    public static String printLimitsViolations(List<LimitViolation> violations, LimitViolationFilter filter) {
        Objects.requireNonNull(filter);
        List<LimitViolation> filteredViolations = filter.apply(violations);
        if (filteredViolations.size() > 0) {
            Collections.sort(filteredViolations, (o1, o2) -> o1.getSubject().getId().compareTo(o2.getSubject().getId()));
            Table table = new Table(8, BorderStyle.CLASSIC_WIDE);
            table.addCell("Country");
            table.addCell("Base voltage");
            table.addCell("Equipment (" + filteredViolations.size() + ")");
            table.addCell("Violation type");
            table.addCell("value");
            table.addCell("limit");
            table.addCell("abs(value-limit)");
            table.addCell("charge %");
            for (LimitViolation violation : filteredViolations) {
                table.addCell(violation.getCountry() != null ? violation.getCountry().name() : "");
                table.addCell(Float.isNaN(violation.getBaseVoltage()) ? "" : Float.toString(violation.getBaseVoltage()));
                table.addCell(violation.getSubject().getId());
                table.addCell(violation.getLimitType().name());
                table.addCell(Float.toString(violation.getValue()));
                table.addCell(Float.toString(violation.getLimit()) + (violation.getLimitReduction() != 1f ? " * " + violation.getLimitReduction() : ""));
                table.addCell(Float.toString(Math.abs(violation.getValue() - violation.getLimit() * violation.getLimitReduction())));
                table.addCell(Integer.toString(Math.round(Math.abs(violation.getValue()) / violation.getLimit() * 100f)));
            }
            return table.render();
        }
        return null;
    }

}
