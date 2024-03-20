/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>Criterion checking that one of the nominal voltages of the network element, <b>on whichever side</b>,
 * is inside a {@link com.powsybl.iidm.criteria.SingleNominalVoltageCriterion.VoltageInterval}.</p>
 * @author Olivier Perrin {@literal <olivier.perrin@rte-france.com>}
 */
public class AtLeastOneNominalVoltageCriterion implements Criterion {

    private final SingleNominalVoltageCriterion.VoltageInterval voltageInterval;

    public AtLeastOneNominalVoltageCriterion(SingleNominalVoltageCriterion.VoltageInterval voltageInterval) {
        this.voltageInterval = Objects.requireNonNull(voltageInterval);
    }

    @JsonIgnore
    @Override
    public CriterionType getType() {
        return CriterionType.AT_LEAST_ONE_NOMINAL_VOLTAGE;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        return filterNominalVoltages(getNominalVoltagesToCheck(identifiable, type));
    }

    @Override
    public boolean filter(NetworkElement networkElement) {
        return filterNominalVoltages(getNominalVoltagesToCheck(networkElement));
    }

    private List<Double> getNominalVoltagesToCheck(Identifiable<?> identifiable, IdentifiableType type) {
        return switch (type) {
            case DANGLING_LINE, GENERATOR, LOAD, BATTERY, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, SWITCH, HVDC_CONVERTER_STATION, BUS ->
                    Collections.singletonList(SingleNominalVoltageCriterion.getNominalVoltage(identifiable, type));
            case LINE, HVDC_LINE, TIE_LINE, TWO_WINDINGS_TRANSFORMER -> TwoNominalVoltageCriterion.getNominalVoltages(identifiable, type);
            case THREE_WINDINGS_TRANSFORMER -> ThreeNominalVoltageCriterion.getNominalVoltages(identifiable, type);
            default -> Collections.emptyList();
        };
    }

    private List<Double> getNominalVoltagesToCheck(NetworkElement networkElement) {
        return Arrays.asList(networkElement.getNominalVoltage1().orElse(null),
                networkElement.getNominalVoltage2().orElse(null),
                networkElement.getNominalVoltage3().orElse(null));
    }

    private boolean filterNominalVoltages(List<Double> nominalVoltagesToCheck) {
        return voltageInterval.isNull() || nominalVoltagesToCheck.stream().filter(Objects::nonNull).anyMatch(voltageInterval::checkIsBetweenBound);
    }

    public SingleNominalVoltageCriterion.VoltageInterval getVoltageInterval() {
        return voltageInterval;
    }
}
