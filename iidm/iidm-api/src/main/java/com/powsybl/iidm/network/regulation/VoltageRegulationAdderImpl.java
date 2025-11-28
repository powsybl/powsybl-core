/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.regulation;

import com.powsybl.iidm.network.Terminal;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public class VoltageRegulationAdderImpl<T extends VoltageRegulationAdder<T>> implements VoltageRegulationAdder<T>, VoltageRegulationBuilder<T> {

    private final VoltageRegulation voltageRegulation;
    private final T parent;

    public VoltageRegulationAdderImpl(T parent) {
        this.parent = parent;
        this.voltageRegulation = new VoltageRegulation();
    }

    @Override
    public VoltageRegulationAdderImpl<T> setTargetValue(Double targetValue) {
        voltageRegulation.setTargetValue(targetValue);
        return this;
    }

    @Override
    public VoltageRegulationAdderImpl<T> setTargetDeadband(Double targetDeadband) {
        return this;
    }

    @Override
    public VoltageRegulationAdderImpl<T> setSlope(Double slope) {
        voltageRegulation.setSlope(slope);
        return this;
    }

    @Override
    public VoltageRegulationAdderImpl<T> setTerminal(Terminal terminal) {
        voltageRegulation.setTerminal(terminal);
        return this;
    }

    @Override
    public VoltageRegulationAdderImpl<T> setMode(RegulationMode mode) {
        voltageRegulation.setMode(mode);
        return this;
    }

    @Override
    public VoltageRegulationAdderImpl<T> setRegulating(boolean regulating) {
        voltageRegulation.setRegulating(regulating);
        return this;
    }

    @Override
    public VoltageRegulationAdderImpl<T> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(parent);
    }

    @Override
    public T addVoltageRegulation() {
        parent.setVoltageRegulation(voltageRegulation);
        return parent;
    }

    @Override
    public void setVoltageRegulation(VoltageRegulation voltageRegulation) {
        throw new UnsupportedOperationException("Cannot set voltage regulation");
    }
}
