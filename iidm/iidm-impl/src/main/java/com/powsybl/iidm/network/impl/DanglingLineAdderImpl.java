/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.DanglingLineAdder;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DanglingLineAdderImpl extends AbstractInjectionAdder<DanglingLineAdderImpl> implements DanglingLineAdder {

    private final VoltageLevelExt voltageLevel;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = Double.NaN;

    private double b = Double.NaN;

    private String ucteXnodeCode;

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
    public DanglingLineImpl add() {
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();

        ValidationUtil.checkP0(this, p0);
        ValidationUtil.checkQ0(this, q0);
        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG(this, g);
        ValidationUtil.checkB(this, b);

        DanglingLineImpl danglingLine = new DanglingLineImpl(getNetwork().getRef(), id, getName(), p0, q0, r, x, g, b, ucteXnodeCode);
        danglingLine.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getObjectStore().checkAndAdd(danglingLine);
        getNetwork().getListeners().notifyCreation(danglingLine);
        return danglingLine;
    }

}
