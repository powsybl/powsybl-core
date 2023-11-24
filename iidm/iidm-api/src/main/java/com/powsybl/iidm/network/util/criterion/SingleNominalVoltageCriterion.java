/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util.criterion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class SingleNominalVoltageCriterion implements Criterion {

    private final VoltageInterval voltageInterval;

    public SingleNominalVoltageCriterion(VoltageInterval voltageInterval) {
        this.voltageInterval = Objects.requireNonNull(voltageInterval);
    }

    @JsonIgnore
    @Override
    public CriterionType getType() {
        return CriterionType.SINGLE_NOMINAL_VOLTAGE;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        switch (type) {
            case LINE:
                return filterInjection(((Line) identifiable).getTerminal1().getVoltageLevel());
            case DANGLING_LINE:
            case GENERATOR:
            case LOAD:
            case BATTERY:
            case SHUNT_COMPENSATOR:
            case STATIC_VAR_COMPENSATOR:
            case BUSBAR_SECTION:
                return filterInjection(((Injection) identifiable).getTerminal().getVoltageLevel());
            case SWITCH:
                return filterInjection(((Switch) identifiable).getVoltageLevel());
            default:
                return false;
        }
    }

    private boolean filterInjection(VoltageLevel voltageLevel) {
        if (voltageLevel == null) {
            return false;
        }
        double injectionNominalVoltage = voltageLevel.getNominalV();
        return voltageInterval.isNull() || voltageInterval.checkIsBetweenBound(injectionNominalVoltage);
    }

    public VoltageInterval getVoltageInterval() {
        return voltageInterval;
    }

    public static class VoltageInterval {
        Double nominalVoltageLowBound;
        Double nominalVoltageHighBound;
        Boolean lowClosed;
        Boolean highClosed;

        public VoltageInterval() {
        }

        public VoltageInterval(Double nominalVoltageLowBound, Double nominalVoltageHighBound, Boolean lowClosed, Boolean highClosed) {
            this.nominalVoltageLowBound = nominalVoltageLowBound;
            this.nominalVoltageHighBound = nominalVoltageHighBound;
            this.lowClosed = lowClosed;
            this.highClosed = highClosed;
        }

        @JsonIgnore
        public boolean isNull() {
            return nominalVoltageLowBound == null || nominalVoltageHighBound == null || lowClosed == null || highClosed == null;
        }

        public boolean checkIsBetweenBound(double value) {
            if (lowClosed && highClosed) {
                return nominalVoltageLowBound <= value && value <= nominalVoltageHighBound;
            } else if (lowClosed) {
                return nominalVoltageLowBound <= value && value < nominalVoltageHighBound;
            } else if (highClosed) {
                return nominalVoltageLowBound < value && value <= nominalVoltageHighBound;
            } else {
                return nominalVoltageLowBound < value && value < nominalVoltageHighBound;
            }
        }

        public Double getNominalVoltageLowBound() {
            return nominalVoltageLowBound;
        }

        public Double getNominalVoltageHighBound() {
            return nominalVoltageHighBound;
        }

        public Boolean getLowClosed() {
            return lowClosed;
        }

        public Boolean getHighClosed() {
            return highClosed;
        }
    }
}
