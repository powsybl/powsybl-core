/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Terminal;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A class which allows to choose a terminal, according to given filters, among several terminals
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public final class TerminalChooser {

    private final List<Predicate<Terminal>> rules;

    public static TerminalChooser getDefaultSlackTerminalChooser() {
        List<Predicate<Terminal>> rules = new ArrayList<>();
        rules.add(t -> t.getConnectable().getType() == ConnectableType.BUSBAR_SECTION);
        rules.add(t -> t.getConnectable() instanceof Injection);
        return new TerminalChooser(rules);
    }

    public TerminalChooser(List<Predicate<Terminal>> rules) {
        Objects.requireNonNull(rules);
        this.rules = rules;
    }

    /**
     * @param terminals the terminals among which a terminal has to be chosen
     * @return the first terminal satisfying a rule (rules are checked in ascending order)
     */
    public Terminal choose(Iterable<? extends Terminal> terminals) {
        for (Predicate<Terminal> predicate : rules) {
            Optional<? extends Terminal> terminal = StreamSupport.stream(terminals.spliterator(), false)
                .filter(predicate)
                .findFirst();
            if (terminal.isPresent()) {
                return terminal.get();
            }
        }
        Iterator<? extends Terminal> it = terminals.iterator();
        return it.hasNext() ? it.next() : null;
    }

    /**
     * @param terminals the terminals among which a terminal has to be chosen
     * @return the first terminal satisfying a rule (rules are checked in ascending order)
     */
    public Terminal choose(Stream<? extends Terminal> terminals) {
        return choose(terminals.collect(Collectors.toList()));
    }

}
