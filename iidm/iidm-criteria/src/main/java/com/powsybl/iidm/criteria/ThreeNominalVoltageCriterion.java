/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.criteria;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.criteria.translation.NetworkElement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ThreeNominalVoltageCriterion implements Criterion {

    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval1;
    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval2;
    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval3;
    @JsonIgnore
    private final List<SingleNominalVoltageCriterion.VoltageInterval> voltageIntervals = new ArrayList<>();

    public ThreeNominalVoltageCriterion(SingleNominalVoltageCriterion.VoltageInterval voltageInterval1,
                                        SingleNominalVoltageCriterion.VoltageInterval voltageInterval2,
                                        SingleNominalVoltageCriterion.VoltageInterval voltageInterval3) {
        this.voltageInterval1 = voltageInterval1 == null ?
                new SingleNominalVoltageCriterion.VoltageInterval(null, null,
                        null, null) : voltageInterval1;
        this.voltageInterval2 = voltageInterval2 == null ?
                new SingleNominalVoltageCriterion.VoltageInterval(null, null,
                        null, null) : voltageInterval2;
        this.voltageInterval3 = voltageInterval3 == null ?
                new SingleNominalVoltageCriterion.VoltageInterval(null, null,
                        null, null) : voltageInterval3;
        if (!this.voltageInterval1.isNull()) {
            voltageIntervals.add(voltageInterval1);
        }
        if (!this.voltageInterval2.isNull()) {
            voltageIntervals.add(voltageInterval2);
        }
        if (!this.voltageInterval3.isNull()) {
            voltageIntervals.add(voltageInterval3);
        }
    }

    @Override
    public CriterionType getType() {
        return CriterionType.THREE_NOMINAL_VOLTAGE;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        if (type == IdentifiableType.THREE_WINDINGS_TRANSFORMER) {
            ThreeWindingsTransformer transformer = (ThreeWindingsTransformer) identifiable;
            return filterThreeWindingsTransformer(transformer.getLeg1().getTerminal(),
                    transformer.getLeg2().getTerminal(),
                    transformer.getLeg3().getTerminal());
        } else {
            return false;
        }
    }

    private boolean filterThreeWindingsTransformer(Terminal terminal1, Terminal terminal2, Terminal terminal3) {
        double voltageLevel1 = terminal1.getVoltageLevel().getNominalV();
        double voltageLevel2 = terminal2.getVoltageLevel().getNominalV();
        double voltageLevel3 = terminal3.getVoltageLevel().getNominalV();
        return filterWithNominalVoltages(voltageLevel1, voltageLevel2, voltageLevel3);
    }

    @Override
    public boolean filter(NetworkElement networkElement) {
        Double nominalVoltage1 = networkElement.getNominalVoltage1();
        Double nominalVoltage2 = networkElement.getNominalVoltage2();
        Double nominalVoltage3 = networkElement.getNominalVoltage3();
        return nominalVoltage1 != null && nominalVoltage2 != null && nominalVoltage3 != null
                && filterWithNominalVoltages(nominalVoltage1, nominalVoltage2, nominalVoltage3);
    }

    private boolean filterWithNominalVoltages(double nominalVoltage1, double nominalVoltage2, double nominalVoltage3) {
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

    public SingleNominalVoltageCriterion.VoltageInterval getVoltageInterval1() {
        return voltageInterval1;
    }

    public SingleNominalVoltageCriterion.VoltageInterval getVoltageInterval2() {
        return voltageInterval2;
    }

    public SingleNominalVoltageCriterion.VoltageInterval getVoltageInterval3() {
        return voltageInterval3;
    }
}
