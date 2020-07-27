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
 * A class which allows to choose a terminal, according to given filters, among several terminals
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public final class TerminalFinder {

    private final List<Predicate<Terminal>> rules;

    public static TerminalFinder getDefault() {
        List<Predicate<Terminal>> rules = new ArrayList<>();
        rules.add(t -> t.getConnectable() instanceof BusbarSection);
        rules.add(t -> t.getConnectable() instanceof Injection);
        rules.add(t -> t.getConnectable() instanceof Branch);
        rules.add(t -> t.getConnectable() instanceof ThreeWindingsTransformer);
        rules.add(t -> t.getConnectable() instanceof HvdcConverterStation);
        rules.add(t -> true);
        return new TerminalFinder(rules);
    }

    public TerminalFinder(List<Predicate<Terminal>> rules) {
        this.rules = Objects.requireNonNull(rules);
    }

    /**
     * @param terminals the terminals among which a terminal has to be chosen
     * @return the first terminal satisfying a rule (rules are checked in ascending order)
     */
    public Terminal find(Iterable<? extends Terminal> terminals) {
        return find(StreamSupport.stream(terminals.spliterator(), false));
    }

    /**
     * @param terminals the terminals among which a terminal has to be chosen
     * @return the first terminal satisfying a rule (rules are checked in ascending order)
     */
    public Terminal find(Stream<? extends Terminal> terminals) {
        Map<Predicate<Terminal>, Terminal> resultMap = new HashMap<>();
        List<Predicate<Terminal>> rulesNotFound = new LinkedList<>(rules);
        terminals.forEach(t -> {
            Iterator<Predicate<Terminal>> iterator = rulesNotFound.iterator();
            while (iterator.hasNext()) {
                Predicate<Terminal> predicate = iterator.next();
                if (predicate.test(t)) {
                    resultMap.put(predicate, t);
                    iterator.remove();
                }
            }
        });
        for (Predicate<Terminal> predicate : rules) {
            if (resultMap.containsKey(predicate)) {
                return resultMap.get(predicate);
            }
        }
        return null;
    }

}
