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
import org.apache.commons.lang3.DoubleRange;

import java.util.Objects;
import java.util.Optional;

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
        Double nominalVoltage = getNominalVoltage(identifiable, type);
        return nominalVoltage != null && filterNominalVoltage(nominalVoltage);
    }

    @Override
    public boolean filter(NetworkElement networkElement) {
        Optional<Double> nominalVoltage = networkElement.getNominalVoltage();
        return nominalVoltage.isPresent() && filterNominalVoltage(nominalVoltage.get());
    }

    protected static Double getNominalVoltage(Identifiable<?> identifiable, IdentifiableType type) {
        return switch (type) {
            case LINE, TIE_LINE -> getNominalVoltage(((Branch<?>) identifiable).getTerminal1().getVoltageLevel());
            case HVDC_LINE -> getNominalVoltage(((HvdcLine) identifiable).getConverterStation1().getTerminal().getVoltageLevel());
            case DANGLING_LINE, GENERATOR, LOAD, BATTERY, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, HVDC_CONVERTER_STATION ->
                    getNominalVoltage(((Injection<?>) identifiable).getTerminal().getVoltageLevel());
            case SWITCH -> getNominalVoltage(((Switch) identifiable).getVoltageLevel());
            case BUS -> getNominalVoltage(((Bus) identifiable).getVoltageLevel());
            default -> null;
        };
    }

    private static Double getNominalVoltage(VoltageLevel voltageLevel) {
        return voltageLevel == null ? null : voltageLevel.getNominalV();
    }

    private boolean filterNominalVoltage(Double nominalVoltage) {
        return voltageInterval.isNull() || voltageInterval.checkIsBetweenBound(nominalVoltage);
    }

    public VoltageInterval getVoltageInterval() {
        return voltageInterval;
    }

    public static class VoltageInterval {
        private static final VoltageInterval NULL_INTERVAL = new VoltageInterval();

        double nominalVoltageLowBound;
        double nominalVoltageHighBound;
        boolean lowClosed;
        boolean highClosed;

        private VoltageInterval() {
            this.nominalVoltageLowBound = Double.NaN;
            this.nominalVoltageHighBound = Double.NaN;
        }

        public static VoltageInterval nullInterval() {
            return NULL_INTERVAL;
        }

        public VoltageInterval(double nominalVoltageLowBound, double nominalVoltageHighBound, boolean lowClosed, boolean highClosed) {
            if (Double.isNaN(nominalVoltageLowBound) || Double.isNaN(nominalVoltageHighBound)
                    || Double.isInfinite(nominalVoltageLowBound) || Double.isInfinite(nominalVoltageHighBound)
                    || nominalVoltageLowBound < 0 || nominalVoltageHighBound < 0) {
                throw new IllegalArgumentException("Invalid interval bounds values (must be >= 0 and not infinite).");
            }
            if (nominalVoltageLowBound > nominalVoltageHighBound) {
                throw new IllegalArgumentException("Invalid interval bounds values (nominalVoltageLowBound must be <= nominalVoltageHighBound).");
            }
            if (nominalVoltageLowBound == nominalVoltageHighBound && (!lowClosed || !highClosed)) {
                throw new IllegalArgumentException("Invalid interval: it should not be empty");
            }
            this.nominalVoltageLowBound = nominalVoltageLowBound;
            this.nominalVoltageHighBound = nominalVoltageHighBound;
            this.lowClosed = lowClosed;
            this.highClosed = highClosed;
        }

        @JsonIgnore
        public boolean isNull() {
            return this == NULL_INTERVAL;
        }

        public boolean checkIsBetweenBound(Double value) {
            if (value == null) {
                return false;
            } else if (lowClosed && highClosed) {
                return nominalVoltageLowBound <= value && value <= nominalVoltageHighBound;
            } else if (lowClosed) {
                return nominalVoltageLowBound <= value && value < nominalVoltageHighBound;
            } else if (highClosed) {
                return nominalVoltageLowBound < value && value <= nominalVoltageHighBound;
            } else {
                return nominalVoltageLowBound < value && value < nominalVoltageHighBound;
            }
        }

        /**
         * <p>Return a {@link DoubleRange} representation of the interval.</p>
         * @return the interval as a {@link DoubleRange}
         */
        public DoubleRange asRange() {
            double min = nominalVoltageLowBound;
            if (!lowClosed) {
                min = min + Math.ulp(min);
            }
            double max = nominalVoltageHighBound;
            if (!highClosed) {
                max = max - Math.ulp(max);
            }
            return DoubleRange.of(min, max);
        }

        public double getNominalVoltageLowBound() {
            return nominalVoltageLowBound;
        }

        public double getNominalVoltageHighBound() {
            return nominalVoltageHighBound;
        }

        public boolean getLowClosed() {
            return lowClosed;
        }

        public boolean getHighClosed() {
            return highClosed;
        }
    }
}
