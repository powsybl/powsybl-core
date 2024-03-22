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
import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.powsybl.iidm.criteria.util.NominalVoltageUtils.getNominalVoltage;
import static com.powsybl.iidm.network.IdentifiableType.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class TwoNominalVoltageCriterion implements Criterion {

    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval1;
    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval2;
    @JsonIgnore
    private final List<SingleNominalVoltageCriterion.VoltageInterval> voltageIntervals = new ArrayList<>();

    public TwoNominalVoltageCriterion(SingleNominalVoltageCriterion.VoltageInterval voltageInterval1,
                                      SingleNominalVoltageCriterion.VoltageInterval voltageInterval2) {
        this.voltageInterval1 = voltageInterval1 != null ? voltageInterval1 :
                SingleNominalVoltageCriterion.VoltageInterval.nullInterval();
        this.voltageInterval2 = voltageInterval2 != null ? voltageInterval2 :
                SingleNominalVoltageCriterion.VoltageInterval.nullInterval();
        if (!this.voltageInterval1.isNull()) {
            voltageIntervals.add(voltageInterval1);
        }
        if (!this.voltageInterval2.isNull()) {
            voltageIntervals.add(voltageInterval2);
        }
    }

    @Override
    public CriterionType getType() {
        return CriterionType.TWO_NOMINAL_VOLTAGE;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        List<Double> nominalVoltages = getNominalVoltages(identifiable, type);
        return nominalVoltages.size() == 2 && filterWithNominalVoltages(nominalVoltages.get(0), nominalVoltages.get(1));
    }

    protected static List<Double> getNominalVoltages(Identifiable<?> identifiable, IdentifiableType type) {
        if (type == TWO_WINDINGS_TRANSFORMER || type == LINE || type == TIE_LINE) {
            Branch<?> branch = (Branch<?>) identifiable;
            return Arrays.asList(getNominalVoltage(branch.getTerminal1()), getNominalVoltage(branch.getTerminal2()));
        } else if (type == HVDC_LINE) {
            HvdcLine hvdcLine = (HvdcLine) identifiable;
            return Arrays.asList(getNominalVoltage(hvdcLine.getConverterStation1().getTerminal()),
                    getNominalVoltage(hvdcLine.getConverterStation2().getTerminal()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean filter(NetworkElement networkElement) {
        Double nominalVoltage1 = networkElement.getNominalVoltage1().orElse(null);
        Double nominalVoltage2 = networkElement.getNominalVoltage2().orElse(null);
        return filterWithNominalVoltages(nominalVoltage1, nominalVoltage2);
    }

    private boolean filterWithNominalVoltages(Double nominalVoltage1, Double nominalVoltage2) {
        AtomicBoolean filter = new AtomicBoolean(true);
        voltageIntervals.forEach(voltageInterval -> {
            if (!voltageInterval.checkIsBetweenBound(nominalVoltage1) &&
                    !voltageInterval.checkIsBetweenBound(nominalVoltage2)) {
                filter.set(false);
            }
        });
        return filter.get();
    }

    public SingleNominalVoltageCriterion.VoltageInterval getVoltageInterval1() {
        return voltageInterval1;
    }

    public SingleNominalVoltageCriterion.VoltageInterval getVoltageInterval2() {
        return voltageInterval2;
    }
}
