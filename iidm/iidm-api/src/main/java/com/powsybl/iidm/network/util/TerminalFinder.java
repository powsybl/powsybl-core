/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A class which allows to choose the best terminal, according to given comparator, among several terminals.
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public final class TerminalFinder {

    private final Comparator<Terminal> comparator;

    public static TerminalFinder getDefault() {
        return new TerminalFinder(getDefaultRules());
    }

    public TerminalFinder(Comparator<Terminal> comparator) {
        this.comparator = Objects.requireNonNull(comparator);
    }

    public TerminalFinder(List<Predicate<Terminal>> predicates) {
        this.comparator = getComparator(predicates);
    }

    /**
     * @param terminals the terminals among which a terminal has to be chosen
     * @return the first terminal satisfying a rule (rules are checked in ascending order)
     */
    public Optional<? extends Terminal> find(Iterable<? extends Terminal> terminals) {
        return find(StreamSupport.stream(terminals.spliterator(), false));
    }

    /**
     * @param terminals the terminals among which a terminal has to be chosen
     * @return the first terminal satisfying a rule (rules are checked in ascending order)
     */
    public Optional<? extends Terminal> find(Stream<? extends Terminal> terminals) {
        return terminals.max(comparator);
    }

    private static Comparator<Terminal> getComparator(List<Predicate<Terminal>> predicates) {
        Objects.requireNonNull(predicates);
        return (o1, o2) -> {
            if (o1.equals(o2)) {
                return 0;
            }
            for (Predicate<Terminal> p : predicates) {
                if (p.test(o1)) {
                    return 1;
                } else if (p.test(o2)) {
                    return -1;
                }
            }
            return 1;
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
