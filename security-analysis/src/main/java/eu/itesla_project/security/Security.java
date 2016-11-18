/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import eu.itesla_project.commons.io.table.Column;
import eu.itesla_project.commons.io.table.TableFormatter;
import eu.itesla_project.commons.io.table.TableFormatterConfig;
import eu.itesla_project.commons.io.table.TableFormatterFactory;
import eu.itesla_project.iidm.network.*;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

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

    private static Country getCountry(TwoTerminalsConnectable branch, Terminal terminal) {
        return terminal == branch.getTerminal1() ? branch.getTerminal1().getVoltageLevel().getSubstation().getCountry()
                                                 : branch.getTerminal2().getVoltageLevel().getSubstation().getCountry();
    }

    private static float getBaseVoltage(TwoTerminalsConnectable branch) {
        return Math.max(branch.getTerminal1().getVoltageLevel().getNominalV(),
                        branch.getTerminal2().getVoltageLevel().getNominalV());
    }

    private static void checkCurrentLimits(Iterable<? extends TwoTerminalsConnectable> branches, CurrentLimitType currentLimitType,
                                           float limitReduction, List<LimitViolation> violations) {
        for (TwoTerminalsConnectable branch : branches) {
            switch (currentLimitType) {
                case PATL:
                    if (branch.checkPermanentLimit1(limitReduction)) {
                        violations.add(new LimitViolation(branch,
                                                          LimitViolationType.CURRENT,
                                                          branch.getCurrentLimits1().getPermanentLimit(),
                                                          null,
                                                          limitReduction,
                                                          branch.getTerminal1().getI(),
                                                          getCountry(branch, branch.getTerminal1()),
                                                          getBaseVoltage(branch)));
                    }
                    if (branch.checkPermanentLimit2(limitReduction)) {
                        violations.add(new LimitViolation(branch,
                                                          LimitViolationType.CURRENT,
                                                          branch.getCurrentLimits2().getPermanentLimit(),
                                                          null,
                                                          limitReduction,
                                                          branch.getTerminal2().getI(),
                                                          getCountry(branch, branch.getTerminal2()),
                                                          getBaseVoltage(branch)));
                    }
                    break;

                case TATL:
                    TwoTerminalsConnectable.Overload o1 = branch.checkTemporaryLimits1(limitReduction);
                    if (o1 != null) {
                        violations.add(new LimitViolation(branch,
                                                          LimitViolationType.CURRENT,
                                                          o1.getPreviousLimit(),
                                                          o1.getTemporaryLimit().getName(),
                                                          limitReduction,
                                                          branch.getTerminal1().getI(),
                                                          getCountry(branch, branch.getTerminal1()),
                                                          getBaseVoltage(branch)));
                    }
                    TwoTerminalsConnectable.Overload o2 = branch.checkTemporaryLimits2(limitReduction);
                    if (o2 != null) {
                        violations.add(new LimitViolation(branch,
                                                          LimitViolationType.CURRENT,
                                                          o2.getPreviousLimit(),
                                                          o2.getTemporaryLimit().getName(),
                                                          limitReduction,
                                                          branch.getTerminal2().getI(),
                                                          getCountry(branch, branch.getTerminal2()),
                                                          getBaseVoltage(branch)));
                    }
                    break;

                default:
                    throw new AssertionError();
            }
        }
    }

    public static List<LimitViolation> checkLimits(Network network) {
        return checkLimits(network, CurrentLimitType.PATL, 1f);
    }

    public static List<LimitViolation> checkLimits(Network network, CurrentLimitType currentLimitType, float limitReduction) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(currentLimitType);
        //if (limitReduction <= 0 || limitReduction > 1) {
        // allow to increase the limits
        if (limitReduction <= 0) {
            throw new IllegalArgumentException("Bad limit reduction " + limitReduction);
        }
        List<LimitViolation> violations = new ArrayList<>();
        checkCurrentLimits(network.getLines(), currentLimitType, limitReduction, violations);
        checkCurrentLimits(network.getTwoWindingsTransformers(), currentLimitType, limitReduction, violations);
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (!Float.isNaN(vl.getLowVoltageLimit())) {
                for (Bus b : vl.getBusView().getBuses()) {
                    if (!Float.isNaN(b.getV())) {
                        if (b.getV() < vl.getLowVoltageLimit()) {
                            violations.add(new LimitViolation(vl, LimitViolationType.LOW_VOLTAGE, vl.getLowVoltageLimit(), null,
                                    1, b.getV(), vl.getSubstation().getCountry(), vl.getNominalV()));
                        }
                    }
                }
            }
            if (!Float.isNaN(vl.getHighVoltageLimit())) {
                for (Bus b : vl.getBusView().getBuses()) {
                    if (!Float.isNaN(b.getV())) {
                        if (b.getV() > vl.getHighVoltageLimit()) {
                            violations.add(new LimitViolation(vl, LimitViolationType.HIGH_VOLTAGE, vl.getHighVoltageLimit(), null,
                                    1, b.getV(), vl.getSubstation().getCountry(), vl.getNominalV()));
                        }
                    }
                }
            }
        }
        return violations;
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
        Objects.requireNonNull(violations);
        Objects.requireNonNull(filter);
        List<LimitViolation> filteredViolations = filter.apply(violations);
        if (filteredViolations.size() > 0) {
            Collections.sort(filteredViolations, (o1, o2) -> o1.getSubject().getId().compareTo(o2.getSubject().getId()));
            Table table = new Table(9, BorderStyle.CLASSIC_WIDE);
            table.addCell("Country");
            table.addCell("Base voltage");
            table.addCell("Equipment (" + filteredViolations.size() + ")");
            table.addCell("Violation type");
            table.addCell("Violation name");
            table.addCell("value");
            table.addCell("limit");
            table.addCell("abs(value-limit)");
            table.addCell("charge %");
            for (LimitViolation violation : filteredViolations) {
                table.addCell(violation.getCountry() != null ? violation.getCountry().name() : "");
                table.addCell(Float.isNaN(violation.getBaseVoltage()) ? "" : Float.toString(violation.getBaseVoltage()));
                table.addCell(violation.getSubject().getId());
                table.addCell(violation.getLimitType().name());
                table.addCell(Objects.toString(violation.getLimitName(), ""));
                table.addCell(Float.toString(violation.getValue()));
                table.addCell(Float.toString(violation.getLimit()) + (violation.getLimitReduction() != 1f ? " * " + violation.getLimitReduction() : ""));
                table.addCell(Float.toString(Math.abs(violation.getValue() - violation.getLimit() * violation.getLimitReduction())));
                table.addCell(Integer.toString(Math.round(Math.abs(violation.getValue()) / violation.getLimit() * 100f)));
            }
            return table.render();
        }
        return null;
    }

    public static void printPreContingencyViolations(SecurityAnalysisResult result, Writer writer, TableFormatterFactory formatterFactory,
                                                     LimitViolationFilter limitViolationFilter) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(formatterFactory);
        try (TableFormatter formatter = formatterFactory.create(writer,
                "Pre-contingency violations",
                TableFormatterConfig.load(),
                new Column("Action"),
                new Column("Equipment"),
                new Column("Violation type"),
                new Column("Violation name"),
                new Column("Value"),
                new Column("Limit"),
                new Column("Charge %"))) {
            for (String action : result.getPreContingencyResult().getActionsTaken()) {
                formatter.writeCell(action)
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell();
            }
            List<LimitViolation> filteredLimitViolations = limitViolationFilter != null
                    ? limitViolationFilter.apply(result.getPreContingencyResult().getLimitViolations())
                    : result.getPreContingencyResult().getLimitViolations();
            filteredLimitViolations.stream()
                    .sorted((o1, o2) -> o1.getSubject().getId().compareTo(o2.getSubject().getId()))
                    .forEach(violation -> {
                        try {
                            formatter.writeEmptyCell()
                                    .writeCell(violation.getSubject().getId())
                                    .writeCell(violation.getLimitType().name())
                                    .writeCell(Objects.toString(violation.getLimitName(), ""))
                                    .writeCell(violation.getValue())
                                    .writeCell(Float.toString(violation.getLimit()) + (violation.getLimitReduction() != 1f ? " * " + violation.getLimitReduction() : ""))
                                    .writeCell(Math.round(Math.abs(violation.getValue()) / violation.getLimit() * 100f));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used to identify a limit violation to avoid duplicated violation between pre and post contingency analysis
     */
    private static class LimitViolationKey {

        private final String id;
        private final LimitViolationType limitType;
        private final float limit;

        public LimitViolationKey(String id, LimitViolationType limitType, float limit) {
            this.id = Objects.requireNonNull(id);
            this.limitType = Objects.requireNonNull(limitType);
            this.limit = limit;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, limitType, limit);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LimitViolationKey) {
                LimitViolationKey other = (LimitViolationKey) obj;
                return id.equals(other.id) && limitType == other.limitType && limit == other.limit;
            }
            return false;
        }
    }

    private static LimitViolationKey toKey(LimitViolation violation) {
        return new LimitViolationKey(violation.getSubject().getId(), violation.getLimitType(), violation.getLimit());
    }

    public static void printPostContingencyViolations(SecurityAnalysisResult result, Writer writer, TableFormatterFactory formatterFactory,
                                                      LimitViolationFilter limitViolationFilter) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(formatterFactory);
        if (result.getPostContingencyResults().size() > 0) {
            Set<LimitViolationKey> preContingencyViolations = result.getPreContingencyResult().getLimitViolations()
                    .stream()
                    .map(Security::toKey)
                    .collect(Collectors.toSet());

            try (TableFormatter formatter = formatterFactory.create(writer,
                    "Post-contingency limit violations",
                    TableFormatterConfig.load(),
                    new Column("Contingency"),
                    new Column("Status"),
                    new Column("Action"),
                    new Column("Equipment"),
                    new Column("Violation type"),
                    new Column("Violation name"),
                    new Column("Value"),
                    new Column("Limit"),
                    new Column("Charge %"))) {
                result.getPostContingencyResults()
                        .stream()
                        .sorted((o1, o2) -> o1.getContingency().getId().compareTo(o2.getContingency().getId()))
                        .forEach(postContingencyResult -> {
                            try {
                                // configured filtering
                                List<LimitViolation> filteredLimitViolations = limitViolationFilter != null
                                        ? limitViolationFilter.apply(postContingencyResult.getLimitViolations())
                                        : postContingencyResult.getLimitViolations();

                                // pre-contingency violations filtering
                                List<LimitViolation> filteredLimitViolations2 = filteredLimitViolations.stream()
                                        .filter(violation -> preContingencyViolations.isEmpty() || !preContingencyViolations.contains(toKey(violation)))
                                        .collect(Collectors.toList());

                                if (filteredLimitViolations2.size() > 0 || !postContingencyResult.isComputationOk()) {
                                    formatter.writeCell(postContingencyResult.getContingency().getId())
                                            .writeCell(postContingencyResult.isComputationOk() ? "converge" : "diverge")
                                            .writeEmptyCell()
                                            .writeEmptyCell()
                                            .writeEmptyCell()
                                            .writeEmptyCell()
                                            .writeEmptyCell()
                                            .writeEmptyCell()
                                            .writeEmptyCell();

                                    for (String action : postContingencyResult.getActionsTaken()) {
                                        formatter.writeEmptyCell()
                                                .writeEmptyCell()
                                                .writeCell(action)
                                                .writeEmptyCell()
                                                .writeEmptyCell()
                                                .writeEmptyCell()
                                                .writeEmptyCell()
                                                .writeEmptyCell()
                                                .writeEmptyCell();
                                    }

                                    filteredLimitViolations2.stream()
                                            .sorted((o1, o2) -> o1.getSubject().getId().compareTo(o2.getSubject().getId()))
                                            .forEach(violation -> {
                                                try {
                                                    formatter.writeEmptyCell()
                                                            .writeEmptyCell()
                                                            .writeEmptyCell()
                                                            .writeCell(violation.getSubject().getId())
                                                            .writeCell(violation.getLimitType().name())
                                                            .writeCell(Objects.toString(violation.getLimitName(), ""))
                                                            .writeCell(violation.getValue())
                                                            .writeCell(Float.toString(violation.getLimit()) + (violation.getLimitReduction() != 1f ? " * " + violation.getLimitReduction() : ""))
                                                            .writeCell(Math.round(Math.abs(violation.getValue()) / violation.getLimit() * 100f));
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            });
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
