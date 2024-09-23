/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.commons.io.table.*;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.detectors.LoadingLimitType;
import com.powsybl.security.limitreduction.SimpleLimitsComputer;
import com.powsybl.security.results.PostContingencyResult;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class Security {

    private static final String PERMANENT_LIMIT_NAME = "Permanent limit";

    private static final String CONTINGENCY = "Contingency";
    private static final String STATUS = "Status";
    private static final String ACTION = "Action";
    private static final String COUNTRY = "Country";
    private static final String BASE_VOLTAGE = "Base voltage";
    private static final String EQUIPMENT = "Equipment";
    private static final String END = "End";
    private static final String VIOLATION_TYPE = "Violation type";
    private static final String VIOLATION_NAME = "Violation name";
    private static final String VALUE = "Value";
    private static final String LIMIT = "Limit";
    private static final String ABS_VALUE_LIMIT = "abs(value-limit)";
    private static final String LOADING_RATE = "Loading rate %";

    private Security() {
    }

    public static List<LimitViolation> checkLimits(Network network) {
        return checkLimits(network, EnumSet.allOf(LoadingLimitType.class), LimitsComputer.NO_MODIFICATIONS);
    }

    public static List<LimitViolation> checkLimits(Network network, double limitReductionValue) {
        return checkLimits(network, EnumSet.allOf(LoadingLimitType.class), limitReductionValue);
    }

    public static List<LimitViolation> checkLimits(Network network, LoadingLimitType currentLimitType, double limitReductionValue) {
        Objects.requireNonNull(currentLimitType);
        return checkLimits(network, EnumSet.of(currentLimitType), limitReductionValue);
    }

    public static List<LimitViolation> checkLimits(Network network, Set<LoadingLimitType> currentLimitTypes, double limitReductionValue) {
        // allow to increase the limits
        if (limitReductionValue <= 0) {
            throw new IllegalArgumentException("Bad limit reduction " + limitReductionValue);
        }
        return checkLimits(network, currentLimitTypes, new SimpleLimitsComputer(limitReductionValue));
    }

    public static List<LimitViolation> checkLimits(Network network, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer) {
        return checkLimits(network, EnumSet.allOf(LoadingLimitType.class), limitsComputer);
    }

    public static List<LimitViolation> checkLimits(Network network, Set<LoadingLimitType> currentLimitTypes, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer) {
        return checkLimits(network, currentLimitTypes, limitsComputer, LimitViolationUtils.VoltageLimitViolationIdType.VOLTAGE_LEVEL_ID);
    }

    public static List<LimitViolation> checkLimits(Network network, Set<LoadingLimitType> currentLimitTypes, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, LimitViolationUtils.VoltageLimitViolationIdType voltageLimitViolationIdType) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(currentLimitTypes);
        Objects.requireNonNull(voltageLimitViolationIdType);
        List<LimitViolation> violations = new ArrayList<>();
        LimitViolationDetection.checkAll(network, currentLimitTypes, limitsComputer, violations::add, voltageLimitViolationIdType);
        return violations;
    }

    public static List<LimitViolation> checkLimitsDc(Network network, double limitReductionValue, double dcPowerFactor) {
        // allow to increase the limits
        if (limitReductionValue <= 0) {
            throw new IllegalArgumentException("Bad limit reduction " + limitReductionValue);
        }
        return checkLimitsDc(network, new SimpleLimitsComputer(limitReductionValue), dcPowerFactor);
    }

    public static List<LimitViolation> checkLimitsDc(Network network, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, double dcPowerFactor) {
        Objects.requireNonNull(network);
        if (dcPowerFactor <= 0 || dcPowerFactor > 1) {
            throw new IllegalArgumentException("Invalid DC power factor " + dcPowerFactor);
        }
        List<LimitViolation> violations = new ArrayList<>();
        LimitViolationDetection.checkAllDc(network, dcPowerFactor, EnumSet.allOf(LoadingLimitType.class), limitsComputer, violations::add);
        return violations;
    }

    public static String printLimitsViolations(Network network) {
        return printLimitsViolations(network, LimitViolationFilter.load());
    }

    public static String printLimitsViolations(Network network, boolean writeName) {
        return printLimitsViolations(checkLimits(network), network, new LimitViolationWriteConfig(LimitViolationFilter.load(),
                                                                                                  TableFormatterConfig.load(),
                                                                                                  writeName));
    }

    public static String printLimitsViolations(Network network, LimitViolationFilter filter) {
        return printLimitsViolations(checkLimits(network), network, filter);
    }

    public static String printLimitsViolations(List<LimitViolation> violations, Network network) {
        return printLimitsViolations(violations, network, LimitViolationFilter.load());
    }

    public static String printLimitsViolations(List<LimitViolation> violations, Network network, LimitViolationFilter filter) {
        return printLimitsViolations(violations, network, filter, TableFormatterConfig.load());
    }

    public static class LimitViolationWriteConfig {

        private final LimitViolationFilter filter;

        private final TableFormatterConfig formatterConfig;

        private final boolean writeName;

        public LimitViolationWriteConfig(LimitViolationFilter filter, TableFormatterConfig formatterConfig, boolean writeName) {
            this.filter = filter;
            this.formatterConfig = Objects.requireNonNull(formatterConfig);
            this.writeName = writeName;
        }

        public LimitViolationFilter getFilter() {
            return filter;
        }

        public TableFormatterConfig getFormatterConfig() {
            return formatterConfig;
        }

        public boolean isWriteName() {
            return writeName;
        }
    }

    public static class PostContingencyLimitViolationWriteConfig extends LimitViolationWriteConfig {

        private final boolean filterPreContingencyViolations;

        public PostContingencyLimitViolationWriteConfig(LimitViolationFilter filter, TableFormatterConfig formatterConfig,
                                                        boolean writeName, boolean filterPreContingencyViolations) {
            super(filter, formatterConfig, writeName);
            this.filterPreContingencyViolations = filterPreContingencyViolations;
        }

        public boolean isFilterPreContingencyViolations() {
            return filterPreContingencyViolations;
        }
    }

    public static String printLimitsViolations(List<LimitViolation> violations, Network network, LimitViolationFilter filter, TableFormatterConfig formatterConfig) {
        return printLimitsViolations(violations, network, new LimitViolationWriteConfig(filter, formatterConfig, false));
    }

    public static String printLimitsViolations(List<LimitViolation> violations, Network network, LimitViolationWriteConfig printConfig) {
        Objects.requireNonNull(violations);
        Objects.requireNonNull(network);
        Objects.requireNonNull(printConfig);

        TableFormatterFactory formatterFactory = new AsciiTableFormatterFactory();
        Writer writer = new StringWriter();
        List<LimitViolation> filteredViolations = printConfig.getFilter() != null ? printConfig.getFilter().apply(violations, network) : violations;

        NumberFormat numberFormat = getFormatter(printConfig.getFormatterConfig().getLocale(), 4, 4);
        NumberFormat percentageFormat = getFormatter(printConfig.getFormatterConfig().getLocale(), 2, 2);

        try (TableFormatter formatter = formatterFactory.create(writer,
                "",
                printConfig.getFormatterConfig(),
                new Column(EQUIPMENT + " (" + filteredViolations.size() + ")"),
                new Column(END),
                new Column(COUNTRY),
                new Column(BASE_VOLTAGE)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT),
                new Column(VIOLATION_TYPE),
                new Column(VIOLATION_NAME),
                new Column(VALUE)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(numberFormat),
                new Column(LIMIT)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(numberFormat),
                new Column(ABS_VALUE_LIMIT)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(numberFormat),
                new Column(LOADING_RATE)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(percentageFormat))) {
            filteredViolations.stream()
                    .sorted(Comparator.comparing(LimitViolation::getSubjectId))
                    .forEach(writeLineLimitsViolations(network, formatter, printConfig.isWriteName()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return writer.toString().trim();
    }

    private static Consumer<? super LimitViolation> writeLineLimitsViolations(Network network, TableFormatter formatter, boolean writeName) {
        return violation -> {
            try {
                formatter.writeCell(writeName ? violation.getSubjectName() : violation.getSubjectId())
                         .writeCell(LimitViolationHelper.getVoltageLevelId(violation, network, writeName))
                         .writeCell(LimitViolationHelper.getCountry(violation, network).map(Country::name).orElse(""))
                         .writeCell((int) LimitViolationHelper.getNominalVoltage(violation, network))
                         .writeCell(violation.getLimitType().name())
                         .writeCell(getViolationName(violation))
                         .writeCell(violation.getValue())
                         .writeCell(getViolationLimit(violation))
                         .writeCell(getAbsValueLimit(violation))
                         .writeCell(getViolationRate(violation));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static double getAbsValueLimit(LimitViolation violation) {
        return Math.abs(violation.getValue() - violation.getLimit() * violation.getLimitReduction());
    }

    public static void printPreContingencyViolations(SecurityAnalysisResult result, Network network, Writer writer, TableFormatterFactory formatterFactory,
                                                     LimitViolationFilter limitViolationFilter) {
        printPreContingencyViolations(result, network, writer, formatterFactory, TableFormatterConfig.load(), limitViolationFilter);
    }

    public static void printPreContingencyViolations(SecurityAnalysisResult result, Network network, Writer writer, TableFormatterFactory formatterFactory,
                                                     TableFormatterConfig formatterConfig, LimitViolationFilter limitViolationFilter) {
        printPreContingencyViolations(result, network, writer, formatterFactory, new LimitViolationWriteConfig(limitViolationFilter, formatterConfig, false));
    }

    public static void printPreContingencyViolations(SecurityAnalysisResult result, Network network, Writer writer, TableFormatterFactory formatterFactory,
                                                     LimitViolationWriteConfig printConfig) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(network);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(formatterFactory);
        Objects.requireNonNull(printConfig);

        NumberFormat numberFormat = getFormatter(printConfig.getFormatterConfig().getLocale(), 4, 4);
        NumberFormat percentageFormat = getFormatter(printConfig.getFormatterConfig().getLocale(), 2, 2);

        List<LimitViolation> filteredLimitViolations = printConfig.getFilter() != null
                ? printConfig.getFilter().apply(result.getPreContingencyLimitViolationsResult().getLimitViolations(), network)
                : result.getPreContingencyLimitViolationsResult().getLimitViolations();

        try (TableFormatter formatter = formatterFactory.create(writer,
                "Pre-contingency violations",
                printConfig.getFormatterConfig(),
                new Column(ACTION),
                new Column(EQUIPMENT + " (" + filteredLimitViolations.size() + ")"),
                new Column(END),
                new Column(COUNTRY),
                new Column(BASE_VOLTAGE)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT),
                new Column(VIOLATION_TYPE),
                new Column(VIOLATION_NAME),
                new Column(VALUE)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(numberFormat),
                new Column(LIMIT)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(numberFormat),
                new Column(ABS_VALUE_LIMIT)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(numberFormat),
                new Column(LOADING_RATE)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(percentageFormat))) {
            for (String action : result.getPreContingencyLimitViolationsResult().getActionsTaken()) {
                formatter.writeCell(action)
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell();
            }
            filteredLimitViolations.stream()
                    .sorted(Comparator.comparing(LimitViolation::getSubjectId))
                    .forEach(writeLinePreContingencyViolations(network, formatter, printConfig.isWriteName()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Consumer<? super LimitViolation> writeLinePreContingencyViolations(Network network, TableFormatter formatter, boolean writeName) {
        return violation -> {
            try {
                formatter.writeEmptyCell()
                        .writeCell(writeName ? violation.getSubjectName() : violation.getSubjectId())
                        .writeCell(LimitViolationHelper.getVoltageLevelId(violation, network, writeName))
                        .writeCell(LimitViolationHelper.getCountry(violation, network).map(Country::name).orElse(""))
                        .writeCell((int) LimitViolationHelper.getNominalVoltage(violation, network))
                        .writeCell(violation.getLimitType().name())
                        .writeCell(getViolationName(violation))
                        .writeCell(violation.getValue())
                        .writeCell(getViolationLimit(violation))
                        .writeCell(getAbsValueLimit(violation))
                        .writeCell(getViolationRate(violation));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static String getViolationName(LimitViolation violation) {
        if (violation.getLimitName() != null) {
            return violation.getLimitName();
        } else if (violation.getAcceptableDuration() != Integer.MAX_VALUE) {
            // TATL
            return String.format("Overload %d'", violation.getAcceptableDuration() / 60);
        } else if (violation.getLimitType() == LimitViolationType.CURRENT) {
            // PATL
            return PERMANENT_LIMIT_NAME;
        } else {
            return "";
        }
    }

    private static double getViolationLimit(LimitViolation violation) {
        return violation.getLimit() * violation.getLimitReduction();
    }

    private static double getViolationRate(LimitViolation violation) {
        return Math.abs(violation.getValue()) / violation.getLimit() * 100.0;
    }

    private static NumberFormat getFormatter(Locale locale, int minimumFractionDigits, int maximumFractionDigits) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        numberFormat.setMinimumFractionDigits(minimumFractionDigits);
        numberFormat.setMaximumFractionDigits(maximumFractionDigits);
        numberFormat.setGroupingUsed(false);
        return numberFormat;
    }

    /**
     * Used to identify a limit violation to avoid duplicated violation between pre and post contingency analysis
     */
    private static class LimitViolationKey {

        private final String id;
        private final LimitViolationType limitType;
        private final double limit;

        public LimitViolationKey(String id, LimitViolationType limitType, double limit) {
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
            if (obj instanceof LimitViolationKey other) {
                return id.equals(other.id) && limitType == other.limitType && limit == other.limit;
            }
            return false;
        }
    }

    private static LimitViolationKey toKey(LimitViolation violation) {
        return new LimitViolationKey(violation.getSubjectId(), violation.getLimitType(), violation.getLimit());
    }

    public static void printPostContingencyViolations(SecurityAnalysisResult result, Network network, Writer writer, TableFormatterFactory formatterFactory,
            LimitViolationFilter limitViolationFilter) {
        printPostContingencyViolations(result, network, writer, formatterFactory, limitViolationFilter, true);
    }

    public static void printPostContingencyViolations(SecurityAnalysisResult result, Network network, Writer writer, TableFormatterFactory formatterFactory,
                                                      LimitViolationFilter limitViolationFilter, boolean filterPreContingencyViolations) {
        printPostContingencyViolations(result, network, writer, formatterFactory, TableFormatterConfig.load(), limitViolationFilter, filterPreContingencyViolations);
    }

    public static void printPostContingencyViolations(SecurityAnalysisResult result, Network network, Writer writer, TableFormatterFactory formatterFactory,
                                                      TableFormatterConfig formatterConfig, LimitViolationFilter limitViolationFilter, boolean filterPreContingencyViolations) {
        printPostContingencyViolations(result, network, writer, formatterFactory,
                new PostContingencyLimitViolationWriteConfig(limitViolationFilter, formatterConfig, false, filterPreContingencyViolations));
    }

    public static void printPostContingencyViolations(SecurityAnalysisResult result, Network network, Writer writer, TableFormatterFactory formatterFactory,
                                                      PostContingencyLimitViolationWriteConfig writeConfig) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(network);
        Objects.requireNonNull(writer);
        Objects.requireNonNull(formatterFactory);
        Objects.requireNonNull(writeConfig);
        if (!result.getPostContingencyResults().isEmpty()) {
            Set<LimitViolationKey> preContingencyViolations = writeConfig.isFilterPreContingencyViolations()
                    ? result.getPreContingencyLimitViolationsResult().getLimitViolations()
                            .stream()
                            .map(Security::toKey)
                            .collect(Collectors.toSet())
                    : Collections.emptySet();

            NumberFormat numberFormat = getFormatter(writeConfig.getFormatterConfig().getLocale(), 4, 4);
            NumberFormat percentageFormat = getFormatter(writeConfig.getFormatterConfig().getLocale(), 2, 2);

            int sumFilter = result.getPostContingencyResults()
                .stream()
                .sorted(Comparator.comparing(o2 -> o2.getContingency().getId()))
                .mapToInt(postContingencyResult -> {
                    // configured filtering
                    List<LimitViolation> filteredLimitViolations = writeConfig.getFilter() != null
                            ? writeConfig.getFilter().apply(postContingencyResult.getLimitViolationsResult().getLimitViolations(), network)
                            : postContingencyResult.getLimitViolationsResult().getLimitViolations();

                    // pre-contingency violations filtering
                    List<LimitViolation> filteredLimitViolations2 = filteredLimitViolations.stream()
                            .filter(violation -> preContingencyViolations.isEmpty() || !preContingencyViolations.contains(toKey(violation)))
                            .toList();

                    return filteredLimitViolations2.size();
                }
               ).sum();

            try (TableFormatter formatter = formatterFactory.create(writer,
                "Post-contingency limit violations",
                writeConfig.getFormatterConfig(),
                new Column(CONTINGENCY),
                new Column(STATUS),
                new Column(ACTION),
                new Column(EQUIPMENT + " (" + sumFilter + ")"),
                new Column(END),
                new Column(COUNTRY),
                new Column(BASE_VOLTAGE)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT),
                new Column(VIOLATION_TYPE),
                new Column(VIOLATION_NAME),
                new Column(VALUE)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(numberFormat),
                new Column(LIMIT)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(numberFormat),
                new Column(ABS_VALUE_LIMIT)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(numberFormat),
                new Column(LOADING_RATE)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    .setNumberFormat(percentageFormat))) {
                result.getPostContingencyResults()
                    .stream()
                    .sorted(Comparator.comparing(o2 -> o2.getContingency().getId()))
                    .forEach(writePostContingencyResult(writeConfig.getFilter(), network, preContingencyViolations, formatter, writeConfig.isWriteName()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static Consumer<? super PostContingencyResult> writePostContingencyResult(LimitViolationFilter limitViolationFilter, Network network,
                                                                                      Set<LimitViolationKey> preContingencyViolations, TableFormatter formatter, boolean writeName) {
        return postContingencyResult -> {
            try {
                // configured filtering
                List<LimitViolation> filteredLimitViolations = limitViolationFilter != null
                        ? limitViolationFilter.apply(postContingencyResult.getLimitViolationsResult().getLimitViolations(), network)
                        : postContingencyResult.getLimitViolationsResult().getLimitViolations();

                // pre-contingency violations filtering
                List<LimitViolation> filteredLimitViolations2 = filteredLimitViolations.stream()
                        .filter(violation -> preContingencyViolations.isEmpty() || !preContingencyViolations.contains(toKey(violation)))
                        .toList();

                if (!filteredLimitViolations2.isEmpty() || postContingencyResult.getStatus() != PostContingencyComputationStatus.CONVERGED) {
                    formatter.writeCell(postContingencyResult.getContingency().getId())
                            .writeCell(postContingencyResult.getStatus().name())
                            .writeEmptyCell()
                            .writeCell(EQUIPMENT + " (" + filteredLimitViolations2.size() + ")")
                            .writeEmptyCell()
                            .writeEmptyCell()
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
                                .writeEmptyCell()
                                .writeEmptyCell()
                                .writeEmptyCell()
                                .writeEmptyCell()
                                .writeEmptyCell();
                    }

                    filteredLimitViolations2.stream()
                            .sorted(Comparator.comparing(LimitViolation::getSubjectId))
                            .forEach(writeLimitViolation(network, formatter, writeName));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static Consumer<? super LimitViolation> writeLimitViolation(Network network, TableFormatter formatter, boolean writeName) {
        return violation -> {
            try {
                formatter.writeEmptyCell()
                        .writeEmptyCell()
                        .writeEmptyCell()
                        .writeCell(writeName ? violation.getSubjectName() : violation.getSubjectId())
                        .writeCell(LimitViolationHelper.getVoltageLevelId(violation, network, writeName))
                        .writeCell(LimitViolationHelper.getCountry(violation, network).map(Country::name).orElse(""))
                        .writeCell((int) LimitViolationHelper.getNominalVoltage(violation, network))
                        .writeCell(violation.getLimitType().name())
                        .writeCell(getViolationName(violation))
                        .writeCell(violation.getValue())
                        .writeCell(getViolationLimit(violation))
                        .writeCell(getAbsValueLimit(violation))
                        .writeCell(getViolationRate(violation));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    public static void print(SecurityAnalysisResult result, Network network, Writer writer, TableFormatterFactory tableFormatterFactory, TableFormatterConfig tableFormatterConfig) {
        print(result, network, writer, tableFormatterFactory, new PostContingencyLimitViolationWriteConfig(null, tableFormatterConfig, false, true));
    }

    public static void print(SecurityAnalysisResult result, Network network, Writer writer, TableFormatterFactory tableFormatterFactory,
                             PostContingencyLimitViolationWriteConfig writeConfig) {
        printPreContingencyViolations(result, network, writer, tableFormatterFactory, writeConfig);
        printPostContingencyViolations(result, network, writer, tableFormatterFactory, writeConfig);
    }
}
