/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.criteria.translation.NetworkElement;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.powsybl.iidm.criteria.util.NominalVoltageUtils.getNominalVoltage;
import static com.powsybl.iidm.network.IdentifiableType.THREE_WINDINGS_TRANSFORMER;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ThreeNominalVoltageCriterion implements Criterion {

    private final VoltageInterval voltageInterval1;
    private final VoltageInterval voltageInterval2;
    private final VoltageInterval voltageInterval3;
    @JsonIgnore
    private final List<VoltageInterval> voltageIntervals = new ArrayList<>();

    public ThreeNominalVoltageCriterion(VoltageInterval voltageInterval1,
                                        VoltageInterval voltageInterval2,
                                        VoltageInterval voltageInterval3) {
        this.voltageInterval1 = voltageInterval1;
        this.voltageInterval2 = voltageInterval2;
        this.voltageInterval3 = voltageInterval3;
        if (this.voltageInterval1 != null) {
            voltageIntervals.add(voltageInterval1);
        }
        if (this.voltageInterval2 != null) {
            voltageIntervals.add(voltageInterval2);
        }
        if (this.voltageInterval3 != null) {
            voltageIntervals.add(voltageInterval3);
        }
    }

    @Override
    public CriterionType getType() {
        return CriterionType.THREE_NOMINAL_VOLTAGE;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        List<Double> nominalVoltages = getNominalVoltages(identifiable, type);
        return nominalVoltages.size() == 3 && filterWithNominalVoltages(nominalVoltages.get(0),
                nominalVoltages.get(1), nominalVoltages.get(2));
    }

    protected static List<Double> getNominalVoltages(Identifiable<?> identifiable, IdentifiableType type) {
        if (type == THREE_WINDINGS_TRANSFORMER) {
            ThreeWindingsTransformer transformer = (ThreeWindingsTransformer) identifiable;
            return Arrays.asList(getNominalVoltage(transformer.getLeg1().getTerminal()),
                    getNominalVoltage(transformer.getLeg2().getTerminal()),
                    getNominalVoltage(transformer.getLeg3().getTerminal()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean filter(NetworkElement networkElement) {
        Double nominalVoltage1 = networkElement.getNominalVoltage1().orElse(null);
        Double nominalVoltage2 = networkElement.getNominalVoltage2().orElse(null);
        Double nominalVoltage3 = networkElement.getNominalVoltage3().orElse(null);
        return filterWithNominalVoltages(nominalVoltage1, nominalVoltage2, nominalVoltage3);
    }

    private boolean filterWithNominalVoltages(Double nominalVoltage1, Double nominalVoltage2, Double nominalVoltage3) {
        AtomicBoolean filter = new AtomicBoolean(true);
        voltageIntervals.forEach(voltageInterval -> {
            if (!voltageInterval.checkIsBetweenBound(nominalVoltage1) &&
                    !voltageInterval.checkIsBetweenBound(nominalVoltage2) &&
                    !voltageInterval.checkIsBetweenBound(nominalVoltage3)) {
                filter.set(false);
            }
        });
        return filter.get();
    }

    public Optional<VoltageInterval> getVoltageInterval1() {
        return Optional.ofNullable(voltageInterval1);
    }

    public Optional<VoltageInterval> getVoltageInterval2() {
        return Optional.ofNullable(voltageInterval2);
    }

    public Optional<VoltageInterval> getVoltageInterval3() {
        return Optional.ofNullable(voltageInterval3);
    }
}
