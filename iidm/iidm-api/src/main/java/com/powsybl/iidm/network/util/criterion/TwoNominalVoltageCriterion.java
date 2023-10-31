/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util.criterion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
        this.voltageInterval1 = voltageInterval1 == null ?
                new SingleNominalVoltageCriterion.VoltageInterval(null, null,
                        null, null) : voltageInterval1;
        this.voltageInterval2 = voltageInterval2 == null ?
                new SingleNominalVoltageCriterion.VoltageInterval(null, null,
                        null, null) : voltageInterval2;
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
        if (type == TWO_WINDINGS_TRANSFORMER || type == LINE) {
            return filter(((Branch<?>) identifiable).getTerminal1(), ((Branch<?>) identifiable).getTerminal2());
        } else if (type == HVDC_LINE) {
            return filter(((HvdcLine) identifiable).getConverterStation1().getTerminal(),
                    ((HvdcLine) identifiable).getConverterStation2().getTerminal());
        } else {
            return false;
        }
    }

    private boolean filter(Terminal terminal1, Terminal terminal2) {
        AtomicBoolean filter = new AtomicBoolean(true);
        voltageIntervals.forEach(voltageInterval -> {
            if (!voltageInterval.checkIsBetweenBound(terminal1.getVoltageLevel().getNominalV()) &&
                    !voltageInterval.checkIsBetweenBound(terminal2.getVoltageLevel().getNominalV())) {
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
