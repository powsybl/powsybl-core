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
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Security {

    private static final String PERMANENT_LIMIT_NAME = "Permanent limit";

    private static final int MAXIMUM_FRACTION_DIGITS = 4;

    private static final int MINIMUM_FRACTION_DIGITS = 4;

    private static final boolean NUMBER_FORMAT_GROUPING_USED = false;

    public enum CurrentLimitType {
        PATL,
        TATL
    }

    private Security() {
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
                PERMANENT_LIMIT_NAME,
                branch.getCurrentLimits(side).getPermanentLimit(),
                limitReduction,
                branch.getTerminal(side).getI(),
                side));
        }
    }

    private static void checkCurrentLimits(Branch branch, Branch.Side side, Set<CurrentLimitType> currentLimitTypes,
                                           float limitReduction, List<LimitViolation> violations) {
        Branch.Overload o1 = branch.checkTemporaryLimits(side, limitReduction);
        if (currentLimitTypes.contains(CurrentLimitType.TATL) && (o1 != null)) {
            violations.add(new LimitViolation(branch.getId(),
                LimitViolationType.CURRENT,
                getLimitName(o1.getTemporaryLimit().getAcceptableDuration()),
                o1.getPreviousLimit(),
                limitReduction,
                branch.getTerminal(side).getI(),
                side));
        } else if (currentLimitTypes.contains(CurrentLimitType.PATL)) {
            checkPermanentLimit(branch, side, limitReduction, violations);
        }
    }

    private static void checkCurrentLimits(Iterable<? extends Branch> branches, Set<CurrentLimitType> currentLimitTypes,
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

    public static List<LimitViolation> checkLimits(Network network, Set<CurrentLimitType> currentLimitTypes, float limitReduction) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(currentLimitTypes);

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
                            violations.add(new LimitViolation(vl.getId(), LimitViolationType.LOW_VOLTAGE, vl.getLowVoltageLimit(),
                                    1, b.getV()));
                        }
                    }
                }
            }
            if (!Float.isNaN(vl.getHighVoltageLimit())) {
                for (Bus b : vl.getBusView().getBuses()) {
                    if (!Float.isNaN(b.getV())) {
                        if (b.getV() > vl.getHighVoltageLimit()) {
                            violations.add(new LimitViolation(vl.getId(), LimitViolationType.HIGH_VOLTAGE, vl.getHighVoltageLimit(),
                                    1, b.getV()));
                        }
                    }
                }
            }
        }
        return violations;
    }

    /**
     * @deprecated use printLimitsViolations(network, List<LimitViolation>, LimitViolationFilter, TableFormatterConfig) instead.
     */
    @Deprecated
    public static String printLimitsViolations(List<LimitViolation> violations) {
        return printLimitsViolations(null, violations, LimitViolationFilter.load(), TableFormatterConfig.load());
    }

    /**
     * @deprecated use printLimitsViolations(network, List<LimitViolation>, LimitViolationFilter, TableFormatterConfig) instead.
     */
    @Deprecated
    public static String printLimitsViolations(List<LimitViolation> violations, LimitViolationFilter filter) {
        return printLimitsViolations(null, violations, filter, TableFormatterConfig.load());
    }

    /**
     * @deprecated use printLimitsViolations(network, List<LimitViolation>, LimitViolationFilter, TableFormatterConfig) instead.
     */
    @Deprecated
    public static String printLimitsViolations(List<LimitViolation> violations, LimitViolationFilter filter, TableFormatterConfig formatterConfig) {
        return printLimitsViolations(null, violations, filter, formatterConfig);
    }

    public static String printLimitsViolations(Network network) {
        return printLimitsViolations(network, checkLimits(network), LimitViolationFilter.load(), TableFormatterConfig.load());
    }

    public static String printLimitsViolations(Network network, List<LimitViolation> violations, LimitViolationFilter filter) {
        return printLimitsViolations(network, violations, filter, TableFormatterConfig.load());
    }

    public static String printLimitsViolations(Network network, List<LimitViolation> violations, LimitViolationFilter filter, TableFormatterConfig formatterConfig) {
        Objects.requireNonNull(violations);
        Objects.requireNonNull(filter);
        Objects.requireNonNull(formatterConfig);

        TableFormatterFactory formatterFactory = new AsciiTableFormatterFactory();
        Writer writer = new StringWriter();
        List<LimitViolation> filteredViolations = filter.apply(violations, network);

        NumberFormat numberFormatter = createNumberFormat(formatterConfig.getLocale());
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
                    .forEach(writeLimitViolation(network, formatter, numberFormatter));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return writer.toString().trim();
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
        Objects.requireNonNull(formatterConfig);

        NumberFormat numberFormatter = createNumberFormat(formatterConfig.getLocale());
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
                    ? limitViolationFilter.apply(result.getPreContingencyResult().getLimitViolations(), result.getNetwork())
                    : result.getPreContingencyResult().getLimitViolations();
            filteredLimitViolations.stream()
                    .sorted(Comparator.comparing(LimitViolation::getSubjectId))
                    .forEach(writePreContingencyLimitViolation(formatter, numberFormatter));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        if (!result.getPostContingencyResults().isEmpty()) {
            Set<LimitViolationKey> preContingencyViolations = filterPreContingencyViolations
                ? result.getPreContingencyResult().getLimitViolations()
                .stream()
                .map(Security::toKey)
                .collect(Collectors.toSet())
                : Collections.emptySet();

            NumberFormat numberFormatter = createNumberFormat(formatterConfig.getLocale());
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
                    .forEach(writePostContingencyResult(result.getNetwork(), limitViolationFilter, preContingencyViolations, formatter, numberFormatter));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static Consumer<? super LimitViolation> writePreContingencyLimitViolation(TableFormatter formatter, NumberFormat numberFormatter) {
        return violation -> {
            try {
                formatter.writeEmptyCell()
                    .writeCell(violation.getSubjectId())
                    .writeCell(violation.getLimitType().name())
                    .writeCell(getViolationName(violation))
                    .writeCell(violation.getValue(), HorizontalAlignment.RIGHT, numberFormatter)
                    .writeCell(getViolationLimit(violation), HorizontalAlignment.RIGHT, numberFormatter)
                    .writeCell(getLoadingRate(violation), HorizontalAlignment.RIGHT, numberFormatter);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static Consumer<? super PostContingencyResult> writePostContingencyResult(Network network, LimitViolationFilter limitViolationFilter,
                                                                                      Set<LimitViolationKey> preContingencyViolations,
                                                                                      TableFormatter formatter, NumberFormat numberFormatter) {
        return postContingencyResult -> {
            try {
                // configured filtering
                List<LimitViolation> filteredLimitViolations = limitViolationFilter != null
                    ? limitViolationFilter.apply(postContingencyResult.getLimitViolationsResult().getLimitViolations(), network)
                    : postContingencyResult.getLimitViolationsResult().getLimitViolations();

                // pre-contingency violations filtering
                List<LimitViolation> filteredLimitViolations2 = filteredLimitViolations.stream()
                    .filter(violation -> preContingencyViolations.isEmpty() || !preContingencyViolations.contains(toKey(violation)))
                    .collect(Collectors.toList());

                if (!filteredLimitViolations2.isEmpty() || !postContingencyResult.getLimitViolationsResult().isComputationOk()) {
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
                        .forEach(writePostContigencyLimitViolation(formatter, numberFormatter));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static Consumer<? super LimitViolation> writePostContigencyLimitViolation(TableFormatter formatter, NumberFormat numberFormatter) {
        return violation -> {
            try {
                formatter.writeEmptyCell()
                    .writeEmptyCell()
                    .writeEmptyCell()
                    .writeCell(violation.getSubjectId())
                    .writeCell(violation.getLimitType().name())
                    .writeCell(getViolationName(violation))
                    .writeCell(violation.getValue(), HorizontalAlignment.RIGHT, numberFormatter)
                    .writeCell(getViolationLimit(violation), HorizontalAlignment.RIGHT, numberFormatter)
                    .writeCell(getLoadingRate(violation), HorizontalAlignment.RIGHT, numberFormatter);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static Consumer<? super LimitViolation> writeLimitViolation(Network network, TableFormatter formatter, NumberFormat numberFormatter) {
        return violation -> {
            try {
                formatter.writeCell(getCountry(violation, network))
                    .writeCell(getNominalVoltage(violation, network))
                    .writeCell(violation.getSubjectId())
                    .writeCell(violation.getLimitType().name())
                    .writeCell(getViolationName(violation))
                    .writeCell(violation.getValue(), HorizontalAlignment.RIGHT, numberFormatter)
                    .writeCell(getViolationLimit(violation), HorizontalAlignment.RIGHT, numberFormatter)
                    .writeCell(getViolation(violation), HorizontalAlignment.RIGHT, numberFormatter)
                    .writeCell(getLoadingRate(violation), HorizontalAlignment.RIGHT, numberFormatter);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static String getCountry(LimitViolation limitViolation, Network network) {
        Country country = (network == null) ? null : LimitViolation.getCountry(limitViolation, network);

        return Objects.toString(country, "");
    }

    private static String getNominalVoltage(LimitViolation limitViolation, Network network) {
        float nominalVoltage = (network == null) ? Float.NaN : LimitViolation.getNominalVoltage(limitViolation, network);

        return Float.isNaN(nominalVoltage) ? "" : Float.toString(nominalVoltage);
    }

    private static String getViolationName(LimitViolation violation) {
        return Objects.toString(violation.getLimitName(), "");
    }

    private static float getViolationLimit(LimitViolation violation) {
        return violation.getLimit() * violation.getLimitReduction();
    }

    private static float getLoadingRate(LimitViolation violation) {
        return Math.abs(violation.getValue()) / violation.getLimit() * 100f;
    }

    private static float getViolation(LimitViolation violation) {
        return Math.abs(violation.getValue() - violation.getLimit() * violation.getLimitReduction());
    }

    private static NumberFormat createNumberFormat(Locale locale) {
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        numberFormat.setMaximumFractionDigits(Security.MAXIMUM_FRACTION_DIGITS);
        numberFormat.setMinimumFractionDigits(Security.MINIMUM_FRACTION_DIGITS);
        numberFormat.setGroupingUsed(Security.NUMBER_FORMAT_GROUPING_USED);
        return numberFormat;
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
}
