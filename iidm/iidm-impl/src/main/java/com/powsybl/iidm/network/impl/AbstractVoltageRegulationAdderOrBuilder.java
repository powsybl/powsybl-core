/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdderOrBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import org.jspecify.annotations.NonNull;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public abstract class AbstractVoltageRegulationAdderOrBuilder<T extends VoltageRegulationAdderOrBuilder<T>> implements VoltageRegulationAdderOrBuilder<T> {

    protected final Class<? extends VoltageRegulationHolder<?>> classHolder;
    protected final Validable validable;
    protected final VoltageRegulationHolder<?> holder;
    protected final Ref<NetworkImpl> network;
    protected double targetValue = Double.NaN;
    protected double targetDeadband = Double.NaN;
    protected double slope = Double.NaN;
    protected Terminal terminal = null;
    protected RegulationMode mode = null;
    protected boolean regulating = true;

    protected AbstractVoltageRegulationAdderOrBuilder(Class<? extends VoltageRegulationHolder<?>> classHolder,
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

    /**
     * Validates and creates a new instance of VoltageRegulation
     */
    protected @NonNull VoltageRegulationExt checkAndCreateVoltageRegulation() {
        // VALIDATION
        if (validable != null) {
            checkPreAttributes(validable);
            // MODE
            checkRegulationMode(validable);
            // SLOPE
            checkSlopeValue(validable);
            // DEADBAND
            checkDeadbandValue(validable);
            // TERMINAL
            checkTerminal(validable);
            // TARGET VALUE (check after Terminal and mode)
            checkTargetValue(validable);
            //
            return new VoltageRegulationImpl(validable, holder, classHolder, network, targetValue, targetDeadband, slope, terminal, mode, regulating);
        }
        throw new PowsyblException("VoltageRegulation cannot be validated because its parent is not a Validable class");
    }

    private void checkPreAttributes(Validable validable) {
        if (holder instanceof RatioTapChanger ratioTapChanger) {
            boolean loadTapChangingCapabilities = ratioTapChanger.hasLoadTapChangingCapabilities();
            network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkRTCLoadTapChangingCapabilities(validable,
                loadTapChangingCapabilities,
                regulating,
                network.get().getMinValidationLevel(),
                network.get().getReportNodeContext().getReportNode()));
        }
    }

    private void checkTargetValue(Validable validable) {
        network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulationTargetValue(validable,
            targetValue, mode, regulating, isWithTerminal(),
            network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode()));
    }

    private void checkTerminal(Validable validable) {
        ValidationUtil.checkRegulatingTerminal(validable, terminal, network.get());
        network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulationTerminal(validable,
            terminal, regulating,
            network.get(),
            network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode()));
    }

    private void checkDeadbandValue(Validable validable) {
        network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulationDeadband(validable,
            targetDeadband, regulating,
            classHolder,
            network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode()));
    }

    private void checkSlopeValue(Validable validable) {
        network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulationSlope(validable,
            slope, mode, regulating,
            network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode()));
    }

    private void checkRegulationMode(Validable validable) {
        network.get().setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageRegulationMode(validable,
            mode, regulating, isWithTerminal(),
            classHolder, network.get().getMinValidationLevel(), network.get().getReportNodeContext().getReportNode()));
    }

    private boolean isWithTerminal() {
        return terminal != null;
    }

    protected abstract T self();
}
