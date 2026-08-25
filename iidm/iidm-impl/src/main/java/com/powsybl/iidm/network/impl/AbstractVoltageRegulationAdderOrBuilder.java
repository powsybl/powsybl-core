/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
abstract class AbstractVoltageRegulationAdderOrBuilder<T extends VoltageRegulationAdderOrBuilder<T>> implements VoltageRegulationAdderOrBuilder<T> {

    // Context
    final Class<? extends VoltageRegulationHolder<?>> classHolder;
    final Validable validable;
    final VoltageRegulationHolder<?> holder;
    final Ref<NetworkImpl> network;
    // VoltageRegulation attributes
    RegulationMode mode = null;
    boolean regulating = true;
    Terminal terminal = null;
    double targetValue = Double.NaN;
    double targetDeadband = Double.NaN;
    double slope = Double.NaN;

    AbstractVoltageRegulationAdderOrBuilder(Class<? extends VoltageRegulationHolder<?>> classHolder,
                                                      Validable validable,
                                                      VoltageRegulationHolder<?> holder,
                                                      Ref<NetworkImpl> network) {
        this.classHolder = classHolder;
        this.holder = holder;
        this.validable = validable;
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

    VoltageRegulation.AttributesWithTerminal checkAndGetVoltageRegulationAttributes() {
        // VALIDATION
        checkVoltageRegulationAttributes();
        return new VoltageRegulation.AttributesWithTerminal(targetValue, targetDeadband, slope, mode, regulating, terminal);
    }

    private void checkVoltageRegulationAttributes() {
        checkAttributesNotFromVoltageRegulation();
        // MODE
        checkRegulationMode();
        // SLOPE
        checkSlopeValue();
        // DEADBAND
        checkDeadbandValue();
        // TERMINAL
        checkTerminal();
        // TARGET VALUE (check after Terminal and mode)
        checkTargetValue();
    }

    private void checkAttributesNotFromVoltageRegulation() {
        if (holder instanceof RatioTapChanger ratioTapChanger) {
            boolean loadTapChangingCapabilities = ratioTapChanger.hasLoadTapChangingCapabilities();
            network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkRTCLoadTapChangingCapabilities(validable,
                loadTapChangingCapabilities,
                regulating,
                network.get().getMinValidationLevel(),
                network.get().getReportNodeContext().getReportNode()));
        }
    }

    private void checkTargetValue() {
        network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulationTargetValue(validable,
            targetValue, mode, regulating, isWithTerminal(),
            network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode()));
    }

    private void checkTerminal() {
        ValidationUtil.checkRegulatingTerminal(validable, terminal, network.get());
        network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulationTerminal(validable,
            terminal, regulating,
            network.get(),
            classHolder,
            network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode()));
    }

    private void checkDeadbandValue() {
        network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulationDeadband(validable,
            targetDeadband, regulating,
            classHolder,
            network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode()));
    }

    private void checkSlopeValue() {
        network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulationSlope(validable,
            slope, mode, regulating,
            network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode()));
    }

    private void checkRegulationMode() {
        network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulationMode(validable,
            mode, regulating, isWithTerminal(),
            classHolder, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode()));
    }

    private boolean isWithTerminal() {
        return terminal != null;
    }

    protected abstract T self();
}
