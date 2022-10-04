/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.criterion;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ThreeNominalVoltageCriterion implements Criterion {

    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval1;
    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval2;
    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval3;

    public ThreeNominalVoltageCriterion(SingleNominalVoltageCriterion.VoltageInterval voltageInterval1,
                                        SingleNominalVoltageCriterion.VoltageInterval voltageInterval2,
                                        SingleNominalVoltageCriterion.VoltageInterval voltageInterval3) {
        this.voltageInterval1 = voltageInterval1;
        this.voltageInterval2 = voltageInterval2;
        this.voltageInterval3 = voltageInterval3;
    }

    @Override
    public CriterionType getType() {
        return CriterionType.THREE_NOMINAL_VOLTAGE;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        return Criterion.super.filter(identifiable, type);
    }

    private boolean filterThreeWindingsTransformer(Terminal terminal1, Terminal terminal2, Terminal terminal3) {
        VoltageLevel voltageLevel1 = terminal1.getVoltageLevel();
        VoltageLevel voltageLevel2 = terminal2.getVoltageLevel();
        VoltageLevel voltageLevel3 = terminal3.getVoltageLevel();
        double branchNominalVoltage1 = voltageLevel1.getNominalV();
        double branchNominalVoltage2 = voltageLevel2.getNominalV();
        double branchNominalVoltage3 = voltageLevel3.getNominalV();
        if (voltageInterval1.isNull() && voltageInterval2.isNull() && voltageInterval3.isNull()) {
            return true;
        } else if (voltageInterval1.isNull() && voltageInterval2.isNull()) {
            return voltageInterval3.checkIsBetweenBound(branchNominalVoltage1) || voltageInterval3.checkIsBetweenBound(branchNominalVoltage2) ||
                    voltageInterval3.checkIsBetweenBound(branchNominalVoltage3);
        } else if (voltageInterval1.isNull() && voltageInterval3.isNull()) {
            return voltageInterval2.checkIsBetweenBound(branchNominalVoltage1) || voltageInterval2.checkIsBetweenBound(branchNominalVoltage2) ||
                    voltageInterval2.checkIsBetweenBound(branchNominalVoltage3);
        } else if (voltageInterval2.isNull() && voltageInterval3.isNull()) {
            return voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) || voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) ||
                    voltageInterval1.checkIsBetweenBound(branchNominalVoltage3);
        } else if (voltageInterval1.isNull()) {
            return (voltageInterval2.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval2.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval2.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval2.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1)) ||
                    (voltageInterval2.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval2.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1));
        } else if (voltageInterval2.isNull()) {
            return (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1));
        } else if (voltageInterval3.isNull()) {
            return (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage1)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage1)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage2));
        } else {
            return (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage2)
                    && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage3)
                            && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage1)
                            && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage3)
                            && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage1)
                            && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage2)
                            && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1));
        }
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
