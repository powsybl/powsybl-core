/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.criterion;

import com.powsybl.iidm.network.*;

import java.util.Objects;

import static com.powsybl.iidm.network.IdentifiableType.TWO_WINDINGS_TRANSFORMER;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class TwoNominalVoltageCriterion implements Criterion {

    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval1;
    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval2;

    public TwoNominalVoltageCriterion(SingleNominalVoltageCriterion.VoltageInterval voltageInterval1,
                                      SingleNominalVoltageCriterion.VoltageInterval voltageInterval2) {
        this.voltageInterval1 = Objects.requireNonNull(voltageInterval1);
        this.voltageInterval2 = Objects.requireNonNull(voltageInterval2);
    }

    @Override
    public CriterionType getType() {
        return CriterionType.TWO_NOMINAL_VOLTAGE;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        if (type == TWO_WINDINGS_TRANSFORMER) {
            return filterTwoWindingsTransformer(((TwoWindingsTransformer) identifiable).getTerminal1(), ((TwoWindingsTransformer) identifiable).getTerminal2());
        } else {
            return false;
        }
    }

    private boolean filterTwoWindingsTransformer(Terminal terminal1, Terminal terminal2) {
        VoltageLevel voltageLevel1 = terminal1.getVoltageLevel();
        VoltageLevel voltageLevel2 = terminal2.getVoltageLevel();
        double branchNominalVoltage1 = voltageLevel1.getNominalV();
        double branchNominalVoltage2 = voltageLevel2.getNominalV();
        if (voltageInterval1.isNull() && voltageInterval2.isNull()) {
            return true;
        } else if (voltageInterval1.isNull()) {
            return voltageInterval2.checkIsBetweenBound(branchNominalVoltage1) || voltageInterval2.checkIsBetweenBound(branchNominalVoltage2);
        } else if (voltageInterval2.isNull()) {
            return voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) || voltageInterval1.checkIsBetweenBound(branchNominalVoltage2);
        } else {
            return (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage1));
        }
    }

    public SingleNominalVoltageCriterion.VoltageInterval getVoltageInterval1() {
        return voltageInterval1;
    }

    public SingleNominalVoltageCriterion.VoltageInterval getVoltageInterval2() {
        return voltageInterval2;
    }
}
