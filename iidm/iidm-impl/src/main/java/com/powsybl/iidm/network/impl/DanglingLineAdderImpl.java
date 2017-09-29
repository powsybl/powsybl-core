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

    private float p0 = Float.NaN;

    private float q0 = Float.NaN;

    private float r = Float.NaN;

    private float x = Float.NaN;

    private float g = Float.NaN;

    private float b = Float.NaN;

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
    public DanglingLineAdderImpl setP0(float p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setQ0(float q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setR(float r) {
        this.r = r;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setX(float x) {
        this.x = x;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setG(float g) {
        this.g = g;
        return this;
    }

    @Override
    public DanglingLineAdderImpl setB(float b) {
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
        TerminalExt terminal = checkAndGetTerminal(id);

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
