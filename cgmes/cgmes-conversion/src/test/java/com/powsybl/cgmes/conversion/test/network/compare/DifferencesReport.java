/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.network.compare;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Identifiable;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class DifferencesReport implements Differences {

    public DifferencesReport() {
        unexpecteds = new HashMap<>();
        missings = new HashMap<>();
        matches = new HashMap<>();
        diffs = new HashMap<>();
        logDetails = true;
        maxDetailDiffs = 3;
        // Pad size for each category of differences
        padSizes = new int[]{25, 25, 25, 50};
    }

    @Override
    public void current(Identifiable i) {
        this.current = i;
    }

    @Override
    public void end() {
        report(summary());
    }

    @Override
    public void compare(String context, double expected, double actual, double tolerance) {
        if (Math.abs(expected - actual) > tolerance) {
            record(context, expected, actual);
        }
    }

    @Override
    public void compare(String context, Object expected, Object actual) {
        boolean equals = false;
        if (expected == null) {
            if (actual == null) {
                equals = true;
            }
        } else {
            equals = expected.equals(actual);
        }
        if (!equals) {
            record(context, expected, actual);
        }
    }

    @Override
    public void unexpected(Identifiable i) {
        String context = Comparison.className(i);
        record(unexpecteds, context, i);
    }

    @Override
    public void missing(Identifiable i) {
        String context = Comparison.className(i);
        record(missings, context, i);
    }

    @Override
    public void match(Identifiable i) {
        String context = Comparison.className(i);
        record(matches, context, i);
    }

    @Override
    public void unexpected(String property) {
        String context = Comparison.className(current) + "." + property;
        record(unexpecteds, context, null);
    }

    @Override
    public void missing(String property) {
        String context = Comparison.className(current) + "." + property;
        record(missings, context, null);
    }

    @Override
    public void notEquivalent(String context, Identifiable expected, Identifiable actual) {
        record(context, expected, actual);
    }

    private static class Diff {
        Diff(Object expected, Object actual) {
            this.expected = expected;
            this.actual = actual;
        }

        Object expected;
        Object actual;
    }

    private void record(String context, Object expected, Object actual) {
        String ccontext = Comparison.className(current) + "." + context;
        if (!diffs.containsKey(ccontext)) {
            diffs.put(ccontext, new HashMap<>());
        }
        if (!diffs.get(ccontext).containsKey(current)) {
            diffs.get(ccontext).put(current, new HashSet<>());
        }
        diffs.get(ccontext).get(current).add(new Diff(expected, actual));
    }

    private void record(Map<String, Set<Identifiable>> is, String className, Identifiable i) {
        if (!is.containsKey(className)) {
            is.put(className, new HashSet<>());
        }
        is.get(className).add(i);
    }

    private enum Category {
        UNEXPECTED, MISSING, MATCHES, DIFFS
    }

    private static class Summary {
        void inc(Category cat, String context, int size) {
            Map<String, Integer> s = data.get(cat);
            if (s == null) {
                s = new HashMap<>();
                data.put(cat, s);
            }
            Integer value = s.get(context);
            if (value == null) {
                value = 0;
            }
            s.put(context, value + size);
        }

        private final Map<Category, Map<String, Integer>> data = new EnumMap<>(Category.class);
    }

    private void report(Summary s) {
        for (Category cat : Category.values()) {
            LOG.info("{}", cat);
            Map<String, Integer> dataCat = s.data.get(cat);
            if (dataCat != null) {
                dataCat.keySet().stream().sorted().forEach(c -> {
                    int padSize = padSizes[cat.ordinal()];
                    LOG.info("    {} : {}", padr(c, padSize), s.data.get(cat).get(c));
                });
            }
        }
    }

    private Summary summary() {
        Summary summary = new Summary();
        summary(Category.UNEXPECTED, unexpecteds, summary);
        summary(Category.MISSING, missings, summary);
        summary(Category.MATCHES, matches, summary);
        if (!diffs.isEmpty()) {
            diffs.keySet().forEach(category -> {
                if (logDetails) {
                    LOG.warn("Differences for {} ({})", category, diffs.get(category).size());
                }
                diffs.get(category).keySet().stream()
                        .sorted(Comparator.comparing(Identifiable::getId))
                        .limit(maxDetailDiffs)
                        .forEach(id -> {
                            Set<Diff> ds = diffs.get(category).get(id);
                            summary.inc(Category.DIFFS, category, diffs.get(category).size());
                            if (logDetails) {
                                if (ds.size() > 1) {
                                    LOG.warn("    {}", id);
                                    diffs.get(category).get(id).stream()
                                            .limit(maxDetailDiffs)
                                            .forEach(diff -> {
                                                LOG.warn("        {} {}",
                                                        diff.expected,
                                                        diff.actual);
                                            });
                                } else {
                                    Diff diff = ds.iterator().next();
                                    LOG.warn("    {} {} {}", id, diff.expected, diff.actual);
                                }
                            }
                        });
            });
        }
        return summary;
    }

    private void summary(
            Category category,
            Map<String, Set<Identifiable>> iByContext,
            Summary s) {
        if (iByContext.isEmpty()) {
            return;
        }
        iByContext.keySet()
                .forEach(context -> {
                    int size = iByContext.get(context).size();
                    s.inc(category, context, size);
                    if (logDetails) {
                        LOG.warn("{} {} ({})", category, context, size);
                        iByContext.get(context).forEach(id -> LOG.warn("    {}", id));
                    }
                });
    }

    private String padr(String s0, int size) {
        String format = String.format("%%-%ds", size);
        String s = String.format(format, s0);
        int len = s.length();
        if (len > size) {
            int keepr = 5;
            int dots = 3;
            if (size > keepr + dots) {
                String left = s.substring(0, size - keepr - dots);
                String right = s.substring(len - keepr);
                return String.format("%s...%s", left, right);
            } else {
                return s.substring(0, size);
            }
        }
        return s;
    }

    private Identifiable current;
    private final Map<String, Set<Identifiable>> unexpecteds;
    private final Map<String, Set<Identifiable>> missings;
    private final Map<String, Set<Identifiable>> matches;
    private final Map<String, Map<Identifiable, HashSet<Diff>>> diffs;
    private final boolean logDetails;
    private final long maxDetailDiffs;
    private final int[] padSizes;

    private static final Logger LOG = LoggerFactory.getLogger(DifferencesReport.class);
}
