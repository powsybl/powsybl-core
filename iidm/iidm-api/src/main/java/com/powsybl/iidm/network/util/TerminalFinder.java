/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A class which allows to find the best terminal, according to given comparator or list of predicate, among several
 * terminals.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class TerminalFinder {

    private final Comparator<Terminal> comparator;

    public static TerminalFinder getDefault() {
        return new TerminalFinder(getDefaultRules());
    }

    public TerminalFinder(Comparator<Terminal> comparator) {
        this.comparator = Objects.requireNonNull(comparator);
    }

    /**
     * Constructs a TerminalFinder based on the given list of predicates, which defines a comparator based on the
     * following rules: a terminal t1 is better than a terminal t2 if the first predicate in the list on which t1 is
     * satisfied is before the first predicate in the list on which t2 is satisfied.
     * @param predicates the list of predicates
     */
    public TerminalFinder(List<Predicate<Terminal>> predicates) {
        this.comparator = getComparator(predicates);
    }

    /**
     * @param terminals the terminals among which a terminal has to be chosen
     * @return a terminal among the best ones based on the comparator field
     */
    public Optional<? extends Terminal> find(Iterable<? extends Terminal> terminals) {
        return find(StreamSupport.stream(terminals.spliterator(), false));
    }

    /**
     * @param terminals the terminals among which a terminal has to be chosen
     * @return a terminal among the best ones based on the comparator field
     */
    public Optional<? extends Terminal> find(Stream<? extends Terminal> terminals) {
        return terminals.max(comparator);
    }

    /**
     * Constructs a comparator based on a list of predicates, based on the following: a terminal t1 is better than a
     * terminal t2 if the first predicate in the list on which t1 is satisfied is before the first predicate in the list
     * on which t2 is satisfied.
     * @param predicates the list of predicates on which the comparator is based
     * @return the corresponding comparator
     */
    private static Comparator<Terminal> getComparator(List<Predicate<Terminal>> predicates) {
        Objects.requireNonNull(predicates);
        return (t1, t2) -> {
            if (t1.equals(t2)) {
                return 0;
            }
            // Looping over the predicates to find the first predicate which is satisfied on t1 or t2
            for (Predicate<Terminal> p : predicates) {
                if (p.test(t1)) {
                    return 1; // t1 is better as t2 didn't satisfy any previous predicate (t1 is then considered better even if t2 satisfies current predicate)
                } else if (p.test(t2)) {
                    return -1;  // t2 is better as t1 didn't satisfy current predicate nor any previous predicate
                }
            }
            return 1; // no predicate is verified by t1 or t2, the first is then considered better than the latter
        };
    }

    private static List<Predicate<Terminal>> getDefaultRules() {
        List<Predicate<Terminal>> rules = new ArrayList<>();
        rules.add(t -> t.getConnectable() instanceof BusbarSection);
        rules.add(t -> t.getConnectable() instanceof Injection);
        rules.add(t -> t.getConnectable() instanceof Branch);
        rules.add(t -> t.getConnectable() instanceof ThreeWindingsTransformer);
        rules.add(t -> t.getConnectable() instanceof HvdcConverterStation);
        rules.add(Objects::nonNull);
        return rules;
    }
}
