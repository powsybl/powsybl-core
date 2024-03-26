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
        return filterNominalVoltage(nominalVoltage);
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
        return voltageInterval.checkIsBetweenBound(nominalVoltage);
    }

    public VoltageInterval getVoltageInterval() {
        return voltageInterval;
    }

}
