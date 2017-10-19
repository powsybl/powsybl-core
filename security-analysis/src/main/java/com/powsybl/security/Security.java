/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.HorizontalAlignment;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Security {

    private static final String PERMANENT_LIMIT_NAME = "Permanent limit";

    private static final String NUNBER_FORMAT = "##.0000";

    public enum CurrentLimitType {
        PATL,
        TATL
    }

    private Security() {
    }

    private static Country getCountry(Branch branch, Branch.Side side) {
        return branch.getTerminal(side).getVoltageLevel().getSubstation().getCountry();
    }

    private static float getNominalVoltage(Branch branch) {
        return Math.max(branch.getTerminal1().getVoltageLevel().getNominalV(),
                        branch.getTerminal2().getVoltageLevel().getNominalV());
    }

    private static float getNominalVoltage(Branch branch, Branch.Side side) {
        return branch.getTerminal(side).getVoltageLevel().getNominalV();
    }

    public static String getLimitName(int acceptableDuration) {
        if (acceptableDuration == Integer.MAX_VALUE) {
            return PERMANENT_LIMIT_NAME;
        } else {
            return String.format("Overload %d'", acceptableDuration / 60);
        }
    }

    private static void checkPermanentLimit(Branch branch, Branch.Side side, float limitReduction, List<LimitViolation> violations) {
        if (branch.checkPermanentLimit(side, limitReduction)) {
            violations.add(new LimitViolation(branch.getId(),
                LimitViolationType.CURRENT,
                branch.getCurrentLimits(side).getPermanentLimit(),
                PERMANENT_LIMIT_NAME,
                limitReduction,
                branch.getTerminal(side).getI(),
                getCountry(branch, side),
                getNominalVoltage(branch, side)));
        }
    }

    private static void checkCurrentLimits(Branch branch, Branch.Side side, EnumSet<CurrentLimitType> currentLimitTypes,
                                           float limitReduction, List<LimitViolation> violations) {
        Branch.Overload o1 = branch.checkTemporaryLimits(side, limitReduction);
        if (currentLimitTypes.contains(CurrentLimitType.TATL) && (o1 != null)) {
            violations.add(new LimitViolation(branch.getId(),
                LimitViolationType.CURRENT,
                o1.getPreviousLimit(),
                getLimitName(o1.getTemporaryLimit().getAcceptableDuration()),
                limitReduction,
                branch.getTerminal(side).getI(),
                getCountry(branch, side),
                getNominalVoltage(branch, side)));
        } else if (currentLimitTypes.contains(CurrentLimitType.PATL)) {
            checkPermanentLimit(branch, side, limitReduction, violations);
        }
    }

    private static void checkCurrentLimits(Iterable<? extends Branch> branches, EnumSet<CurrentLimitType> currentLimitTypes,
                                           float limitReduction, List<LimitViolation> violations) {
        for (Branch branch : branches) {
            checkCurrentLimits(branch, Branch.Side.ONE, currentLimitTypes, limitReduction, violations);
            checkCurrentLimits(branch, Branch.Side.TWO, currentLimitTypes, limitReduction, violations);
        }
    }

    public static List<LimitViolation> checkLimits(Network network) {
        return checkLimits(network, EnumSet.allOf(CurrentLimitType.class), 1f);
    }

    public static List<LimitViolation> checkLimits(Network network, float limitReduction) {
        return checkLimits(network, EnumSet.allOf(CurrentLimitType.class), limitReduction);
    }

    public static List<LimitViolation> checkLimits(Network network, CurrentLimitType currentLimitType, float limitReduction) {
        Objects.requireNonNull(currentLimitType);
        return checkLimits(network, EnumSet.of(currentLimitType), limitReduction);
    }

    public static List<LimitViolation> checkLimits(Network network, EnumSet<CurrentLimitType> currentLimitTypes, float limitReduction) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(currentLimitTypes);
        //if (limitReduction <= 0 || limitReduction > 1) {
        // allow to increase the limits
        if (limitReduction <= 0) {
            throw new IllegalArgumentException("Bad limit reduction " + limitReduction);
        }
        List<LimitViolation> violations = new ArrayList<>();
        checkCurrentLimits(network.getLines(), currentLimitTypes, limitReduction, violations);
        checkCurrentLimits(network.getTwoWindingsTransformers(), currentLimitTypes, limitReduction, violations);
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (!Float.isNaN(vl.getLowVoltageLimit())) {
                for (Bus b : vl.getBusView().getBuses()) {
                    if (!Float.isNaN(b.getV())) {
                        if (b.getV() < vl.getLowVoltageLimit()) {
                            violations.add(new LimitViolation(vl.getId(), LimitViolationType.LOW_VOLTAGE, vl.getLowVoltageLimit(), null,
                                    1, b.getV(), vl.getSubstation().getCountry(), vl.getNominalV()));
                        }
                    }
                }
            }
            if (!Float.isNaN(vl.getHighVoltageLimit())) {
                for (Bus b : vl.getBusView().getBuses()) {
                    if (!Float.isNaN(b.getV())) {
                        if (b.getV() > vl.getHighVoltageLimit()) {
                            violations.add(new LimitViolation(vl.getId(), LimitViolationType.HIGH_VOLTAGE, vl.getHighVoltageLimit(), null,
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

        TableFormatterFactory formatterFactory = new AsciiTableFormatterFactory();
        Writer writer = new StringWriter();
        TableFormatterConfig formatterConfig = new TableFormatterConfig(Locale.getDefault(), ',', "inv", true, true);
        List<LimitViolation> filteredViolations = filter.apply(violations);

        try (TableFormatter formatter = formatterFactory.create(writer,
                "",
                formatterConfig,
                new Column("Country"),
                new Column("Base voltage"),
                new Column("Equipment (" + filteredViolations.size() + ")"),
                new Column("Violation type"),
                new Column("Violation name"),
                new Column("Value"),
                new Column("Limit"),
                new Column("abs(value-limit)"),
                new Column("Loading rate %"))) {
            filteredViolations.stream()
                    .sorted(Comparator.comparing(LimitViolation::getSubjectId))
                    .forEach(writeLineLimitsViolations(formatter));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return writer.toString().trim();
    }

    private static Consumer<? super LimitViolation> writeLineLimitsViolations(TableFormatter formatter) {
        NumberFormat numberFormatter = createNumberFormat();
        return violation -> {
            try {
                formatter.writeCell(violation.getCountry() != null ? violation.getCountry().name() : "")
                         .writeCell(Float.isNaN(violation.getBaseVoltage()) ? "" : Float.toString(violation.getBaseVoltage()))
                         .writeCell(violation.getSubjectId())
                         .writeCell(violation.getLimitType().name())
                         .writeCell(getViolationName(violation))
                         .writeCell(violation.getValue(), HorizontalAlignment.R, numberFormatter)
                         .writeCell(getViolationLimit(violation), HorizontalAlignment.R, numberFormatter)
                         .writeCell(getAbsValueLimit(violation), HorizontalAlignment.R, numberFormatter)
                         .writeCell(getViolationValue(violation), HorizontalAlignment.R, numberFormatter);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static float getAbsValueLimit(LimitViolation violation) {
        return Float.valueOf(Float.toString(violation.getLimit()) + (violation.getLimitReduction() != 1f ? " * " + violation.getLimitReduction() : ""));
    }

    public static void printPreContingencyViolations(SecurityAnalysisResult result, Writer writer, TableFormatterFactory formatterFactory,
                                                     LimitViolationFilter limitViolationFilter) {
        printPreContingencyViolations(result, writer, formatterFactory, TableFormatterConfig.load(), limitViolationFilter);
    }

    public static void printPreContingencyViolations(SecurityAnalysisResult result, Writer writer, TableFormatterFactory formatterFactory,
                                                     TableFormatterConfig formatterConfig, LimitViolationFilter limitViolationFilter) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(formatterFactory);
        try (TableFormatter formatter = formatterFactory.create(writer,
                "Pre-contingency violations",
                formatterConfig,
                new Column("Action"),
                new Column("Equipment"),
                new Column("Violation type"),
                new Column("Violation name"),
                new Column("Value"),
                new Column("Limit"),
                new Column("Loading rate %"))) {
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
                    .sorted(Comparator.comparing(LimitViolation::getSubjectId))
                    .forEach(writeLinePreContingencyViolations(formatter));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Consumer<? super LimitViolation> writeLinePreContingencyViolations(TableFormatter formatter) {
        NumberFormat numberFormatter = createNumberFormat();
        return violation -> {
            try {
                formatter.writeEmptyCell()
                        .writeCell(violation.getSubjectId())
                        .writeCell(violation.getLimitType().name())
                        .writeCell(getViolationName(violation))
                        .writeCell(violation.getValue(), HorizontalAlignment.R, numberFormatter)
                        .writeCell(getViolationLimit(violation), HorizontalAlignment.R, numberFormatter)
                        .writeCell(getViolationValue(violation), HorizontalAlignment.R, numberFormatter);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static String getViolationName(LimitViolation violation) {
        return Objects.toString(violation.getLimitName(), "");
    }

    private static float getViolationLimit(LimitViolation violation) {
        return Float.valueOf(Float.toString(violation.getLimit()) + (violation.getLimitReduction() != 1f ? " * " + violation.getLimitReduction() : ""));
    }

    private static float getViolationValue(LimitViolation violation) {
        return Math.abs(violation.getValue()) / violation.getLimit() * 100f;
    }

    private static NumberFormat createNumberFormat() {
        return  new DecimalFormat(Security.NUNBER_FORMAT);
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
        return new LimitViolationKey(violation.getSubjectId(), violation.getLimitType(), violation.getLimit());
    }

    public static void printPostContingencyViolations(SecurityAnalysisResult result, Writer writer, TableFormatterFactory formatterFactory,
                                                      LimitViolationFilter limitViolationFilter) {
        printPostContingencyViolations(result, writer, formatterFactory, limitViolationFilter, true);
    }

    public static void printPostContingencyViolations(SecurityAnalysisResult result, Writer writer, TableFormatterFactory formatterFactory,
                                                      LimitViolationFilter limitViolationFilter, boolean filterPreContingencyViolations) {
        printPostContingencyViolations(result, writer, formatterFactory, TableFormatterConfig.load(), limitViolationFilter, filterPreContingencyViolations);
    }

    public static void printPostContingencyViolations(SecurityAnalysisResult result, Writer writer, TableFormatterFactory formatterFactory,
                                                      TableFormatterConfig formatterConfig, LimitViolationFilter limitViolationFilter, boolean filterPreContingencyViolations) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(formatterFactory);
        if (result.getPostContingencyResults().size() > 0) {
            Set<LimitViolationKey> preContingencyViolations = filterPreContingencyViolations
                    ? result.getPreContingencyResult().getLimitViolations()
                            .stream()
                            .map(Security::toKey)
                            .collect(Collectors.toSet())
                    : Collections.emptySet();

            try (TableFormatter formatter = formatterFactory.create(writer,
                    "Post-contingency limit violations",
                    formatterConfig,
                    new Column("Contingency"),
                    new Column("Status"),
                    new Column("Action"),
                    new Column("Equipment"),
                    new Column("Violation type"),
                    new Column("Violation name"),
                    new Column("Value"),
                    new Column("Limit"),
                    new Column("Loading rate %"))) {
                result.getPostContingencyResults()
                        .stream()
                        .sorted(Comparator.comparing(o2 -> o2.getContingency().getId()))
                        .forEach(writeLinePostContingencyResult(limitViolationFilter, preContingencyViolations, formatter));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static Consumer<? super PostContingencyResult> writeLinePostContingencyResult(
        LimitViolationFilter limitViolationFilter, Set<LimitViolationKey> preContingencyViolations,
        TableFormatter formatter) {
        return postContingencyResult -> {
            try {
                // configured filtering
                List<LimitViolation> filteredLimitViolations = limitViolationFilter != null
                        ? limitViolationFilter.apply(postContingencyResult.getLimitViolationsResult().getLimitViolations())
                        : postContingencyResult.getLimitViolationsResult().getLimitViolations();

                // pre-contingency violations filtering
                List<LimitViolation> filteredLimitViolations2 = filteredLimitViolations.stream()
                        .filter(violation -> preContingencyViolations.isEmpty() || !preContingencyViolations.contains(toKey(violation)))
                        .collect(Collectors.toList());

                if (filteredLimitViolations2.size() > 0 || !postContingencyResult.getLimitViolationsResult().isComputationOk()) {
                    formatter.writeCell(postContingencyResult.getContingency().getId())
                            .writeCell(postContingencyResult.getLimitViolationsResult().isComputationOk() ? "converge" : "diverge")
                            .writeEmptyCell()
                            .writeEmptyCell()
                            .writeEmptyCell()
                            .writeEmptyCell()
                            .writeEmptyCell()
                            .writeEmptyCell()
                            .writeEmptyCell();

                    for (String action : postContingencyResult.getLimitViolationsResult().getActionsTaken()) {
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
                            .sorted(Comparator.comparing(LimitViolation::getSubjectId))
                            .forEach(writeLineFilteredLimitViolations2(formatter));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static Consumer<? super LimitViolation> writeLineFilteredLimitViolations2(TableFormatter formatter) {
        NumberFormat numberFormatter = createNumberFormat();
        return violation -> {
            try {
                formatter.writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeCell(violation.getSubjectId())
                        .writeCell(violation.getLimitType().name())
                        .writeCell(getViolationName(violation))
                        .writeCell(violation.getValue(), HorizontalAlignment.R, numberFormatter)
                        .writeCell(getViolationLimit(violation), HorizontalAlignment.R, numberFormatter)
                        .writeCell(getViolationValue(violation), HorizontalAlignment.R, numberFormatter);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
