/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public abstract class AbstractVoltageRegulationAdderOrBuilder<T extends VoltageRegulationAdderOrBuilder<T>, P extends VoltageRegulationHolder> implements VoltageRegulationAdderOrBuilder<T> {

    protected final P parent;
    protected final Consumer<VoltageRegulationImpl> setVoltageRegulation;
    protected final Ref<NetworkImpl> network;
    protected double targetValue = Double.NaN;
    protected double targetDeadband = Double.NaN;
    protected double slope = Double.NaN;
    protected Terminal terminal = null;
    protected RegulationMode mode = null;
    protected boolean regulating = false;

    protected AbstractVoltageRegulationAdderOrBuilder(P parent, Ref<NetworkImpl> network, Consumer<VoltageRegulationImpl> setVoltageRegulation) {
        this.parent = parent;
        this.setVoltageRegulation = setVoltageRegulation;
        this.network = network;
    }

    @Override
    public T withTargetValue(double targetValue) {
        this.targetValue = targetValue;
        return self();
    }

    @Override
    public T withTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return self();
    }

    @Override
    public T withSlope(double slope) {
        this.slope = slope;
        return self();
    }

    @Override
    public T withTerminal(Terminal terminal) {
        this.terminal = terminal;
        return self();
    }

    @Override
    public T withMode(RegulationMode mode) {
        this.mode = mode;
        return self();
    }

    @Override
    public T withRegulating(boolean regulating) {
        this.regulating = regulating;
        return self();
    }

    protected @NonNull VoltageRegulationImpl createVoltageRegulation() {
        VoltageRegulationImpl voltageRegulation = new VoltageRegulationImpl(network, targetValue, targetDeadband, slope, terminal, mode, regulating);
        if (parent instanceof Validable validable) {
            network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulation(validable,
                voltageRegulation,
                network.get().getMinValidationLevel(),
                network.get().getReportNodeContext().getReportNode()));
        }
        return voltageRegulation;
    }

    protected abstract T self();
}
