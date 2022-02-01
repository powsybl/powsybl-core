/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.DanglingLineAdder;
import com.powsybl.iidm.network.ValidationUtil;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 *
 */
class DanglingLineAdderImpl extends AbstractInjectionAdder<DanglingLineAdderImpl> implements DanglingLineAdder {

    class GenerationAdderImpl implements GenerationAdder {

        private double minP = Double.NaN;
        private double maxP = Double.NaN;
        private double targetP = Double.NaN;
        private double targetQ = Double.NaN;
        private boolean voltageRegulationOn = false;
        private double targetV = Double.NaN;

        @Override
        public GenerationAdder setTargetP(double targetP) {
            this.targetP = targetP;
            return this;
        }

        @Override
        public GenerationAdder setMaxP(double maxP) {
            this.maxP = maxP;
            return this;
        }

        @Override
        public GenerationAdder setMinP(double minP) {
            this.minP = minP;
            return this;
        }

        @Override
        public GenerationAdder setTargetQ(double targetQ) {
            this.targetQ = targetQ;
            return this;
        }

        @Override
        public GenerationAdder setVoltageRegulationOn(boolean voltageRegulationOn) {
            this.voltageRegulationOn = voltageRegulationOn;
            return this;
        }

        @Override
        public GenerationAdder setTargetV(double targetV) {
            this.targetV = targetV;
            return this;
        }

        @Override
        public DanglingLineAdder add() {
            NetworkImpl network = getNetwork();
            ValidationUtil.checkActivePowerLimits(DanglingLineAdderImpl.this, minP, maxP);
            network.setValidationLevelIfGreaterThan(ValidationUtil.checkActivePowerSetpoint(DanglingLineAdderImpl.this, targetP, network.getMinValidationLevel()));
            network.setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageControl(DanglingLineAdderImpl.this, voltageRegulationOn, targetV, targetQ, network.getMinValidationLevel()));

            generationAdder = this;
            return DanglingLineAdderImpl.this;
        }

        private DanglingLineImpl.GenerationImpl build() {
            return new DanglingLineImpl.GenerationImpl(getNetwork(), minP, maxP, targetP, targetQ, targetV, voltageRegulationOn);
        }
    }

    private final VoltageLevelExt voltageLevel;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = Double.NaN;

    private double b = Double.NaN;

    private String ucteXnodeCode;

    private GenerationAdderImpl generationAdder;

    DanglingLineAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "Dangling line";
    }

    @Override
    public DanglingLineAdderImpl setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public DanglingLineAdder setUcteXnodeCode(String ucteXnodeCode) {
        this.ucteXnodeCode = ucteXnodeCode;
        return this;
    }

    @Override
    public GenerationAdder newGeneration() {
        return new GenerationAdderImpl();
    }

    @Override
    public DanglingLineImpl add() {
        NetworkImpl network = getNetwork();
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();

        network.setValidationLevelIfGreaterThan(ValidationUtil.checkP0(this, p0, network.getMinValidationLevel()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkQ0(this, q0, network.getMinValidationLevel()));
        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG(this, g);
        ValidationUtil.checkB(this, b);

        DanglingLineImpl.GenerationImpl generation = null;
        if (generationAdder != null) {
            generation = generationAdder.build();
        }

        DanglingLineImpl danglingLine = new DanglingLineImpl(network.getRef(), id, getName(), isFictitious(), p0, q0, r, x, g, b, ucteXnodeCode, generation);
        danglingLine.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        network.getIndex().checkAndAdd(danglingLine);
        network.getListeners().notifyCreation(danglingLine);
        return danglingLine;
    }

}
