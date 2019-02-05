/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Terminal;

import java.util.*;
import java.util.function.Function;

import static com.powsybl.iidm.network.util.TemporaryLimitsUtils.getTmpLimitName;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CurrentLimitsMapping {

    private final Map<Terminal, CurrentLimitsAdder> adders;
    private final Map<CurrentLimitsAdder, Set<Double>> permanentLimits;
    private final Map<CurrentLimitsAdder, List<TemporaryLimit>> temporaryLimits;

    public CurrentLimitsMapping() {
        adders = new HashMap<>();
        permanentLimits = new HashMap<>();
        temporaryLimits = new HashMap<>();
    }

    public CurrentLimitsAdder computeAdderIfAbsent(Terminal terminal,
                                                   Function<? super Terminal, ? extends CurrentLimitsAdder> mappingFunction) {
        return adders.computeIfAbsent(terminal, mappingFunction);
    }

    public void addPermanentLimit(double value, CurrentLimitsAdder adder) { // Only keep the lowest permanent limit (cf CVG) To keep ??
        Set<Double> values = permanentLimits.computeIfAbsent(adder, a -> new HashSet<>());
        values.add(value);
    }

    public void addTemporaryLimit(int acceptableDuration, double value, CurrentLimitsAdder adder) {
        List<TemporaryLimit> limits = temporaryLimits.computeIfAbsent(adder, a -> new ArrayList<>());
        limits.add(new TemporaryLimit(acceptableDuration, value));
    }

    public void addAll() {
        fillPermanentLimits();
        fillTemporaryLimits();
        for (Map.Entry<Terminal, CurrentLimitsAdder> entry : adders.entrySet()) {
            entry.getValue().add();
        }
    }

    private void fillPermanentLimits() {
        for (Map.Entry<CurrentLimitsAdder, Set<Double>> entry : permanentLimits.entrySet()) {
            entry.getKey().setPermanentLimit(Collections.min(entry.getValue()));
        }
    }

    private void fillTemporaryLimits() {
        for (Map.Entry<CurrentLimitsAdder, List<TemporaryLimit>> entry : temporaryLimits.entrySet()) {
            // Sort to be sure to have the lowest acceptation value for max limit value (cf. CVG)
            entry.getValue().sort(Comparator.comparing(TemporaryLimit::getAcceptableDuration));
            int firstAcceptableDuration = entry.getValue().get(0).getAcceptableDuration();
            entry.getKey().beginTemporaryLimit() // first temporary limit has max value (cf. CVG)
                    .setName(getTmpLimitName(firstAcceptableDuration))
                    .setAcceptableDuration(firstAcceptableDuration)
                    .setValue(Double.MAX_VALUE)
                    .endTemporaryLimit();
            for (int i = 1; i < entry.getValue().size(); i++) {
                TemporaryLimit temporaryLimit = entry.getValue().get(i);
                entry.getKey().beginTemporaryLimit()
                        .setName(getTmpLimitName(temporaryLimit.getAcceptableDuration()))
                        .setAcceptableDuration(temporaryLimit.getAcceptableDuration())
                        .setValue(temporaryLimit.getValue())
                        .endTemporaryLimit();
            }
        }
    }

    private class TemporaryLimit {
        private final Integer acceptableDuration;
        private final double value;

        TemporaryLimit(int acceptableDuration, double value) {
            this.acceptableDuration = acceptableDuration;
            this.value = value;
        }

        Integer getAcceptableDuration() {
            return acceptableDuration;
        }

        double getValue() {
            return value;
        }
    }
}
