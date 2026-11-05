/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.BoundaryLineAdder;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class GenerationAdderImpl implements BoundaryLineAdder.GenerationAdder {

    private final BoundaryLineAdderImpl parent;

    private double minP = Double.NaN;
    private double maxP = Double.NaN;
    private double targetP = Double.NaN;
    private double localTargetV = Double.NaN;
    private double localTargetQ = Double.NaN;
    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes;
    private Boolean voltageRegulationOn;

    GenerationAdderImpl(BoundaryLineAdderImpl parent) {
        this.parent = parent;
    }

    @Override
    public GenerationAdderImpl setTargetP(double targetP) {
        this.targetP = targetP;
        return this;
    }

    @Override
    public GenerationAdderImpl setMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    @Override
    public GenerationAdderImpl setMinP(double minP) {
        this.minP = minP;
        return this;
    }

    @Override
    public GenerationAdderImpl setTargetQ(double targetQ) {
        return setLocalTargetQ(targetQ);
    }

    @Override
    public GenerationAdderImpl setVoltageRegulationOn(boolean voltageRegulationOn) {
        this.voltageRegulationOn = voltageRegulationOn;
        return this;
    }

    @Override
    public GenerationAdderImpl setTargetV(double targetV) {
        return setLocalTargetV(targetV);
    }

    @Override
    public GenerationAdderImpl setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    @Override
    public GenerationAdderImpl setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return localTargetQ;
    }

    @Override
    public VoltageRegulationAdder<BoundaryLineAdder.GenerationAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(BoundaryLine.Generation.class, parent, this, parent.getNetworkRef(), this::setVoltageRegulationAttributes);
    }

    private void setVoltageRegulationAttributes(VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes) {
        this.voltageRegulationAttributes = voltageRegulationAttributes;
        this.voltageRegulationOn = null;
    }

    private VoltageRegulation.VoltageRegulationAttributes buildVoltageRegulationAttributes() {
        if (!Boolean.TRUE.equals(voltageRegulationOn)) {
            return null;
        }
        return BoundaryLineImpl.defaultLocalVoltageRegulationAttributes();
    }

    @Override
    public BoundaryLineAdderImpl add() {
        NetworkImpl network = parent.getNetwork();
        ValidationUtil.checkActivePowerLimits(parent, minP, maxP);
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkActivePowerSetpoint(parent, targetP, network.getMinValidationLevel(),
                network.getReportNodeContext().getReportNode()));

        // Backward compatibility
        if (voltageRegulationAttributes == null) {
            voltageRegulationAttributes = buildVoltageRegulationAttributes();
        }

        network.setValidationLevelIfGreaterThan(ValidationUtil.checkLocalTargetQandV(parent, BoundaryLine.Generation.class,
                localTargetV, localTargetQ, voltageRegulationAttributes,
                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));

        parent.setGenerationAdder(this);
        return parent;
    }

    BoundaryLineImpl.GenerationImpl build() {
        return new BoundaryLineImpl.GenerationImpl(parent.getNetwork(), minP, maxP, targetP,
            localTargetV, localTargetQ, voltageRegulationAttributes);
    }
}
