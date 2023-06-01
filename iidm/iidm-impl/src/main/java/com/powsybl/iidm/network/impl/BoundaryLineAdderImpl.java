/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BoundaryLineAdder;
import com.powsybl.iidm.network.ValidationUtil;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 *
 */
class BoundaryLineAdderImpl extends AbstractInjectionAdder<BoundaryLineAdderImpl> implements BoundaryLineAdder {

    private final VoltageLevelExt voltageLevel;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = 0.0;

    private double b = 0.0;

    private String ucteXnodeCode;

    private GenerationAdderImpl generationAdder;

    BoundaryLineAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    public NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    void setGenerationAdder(GenerationAdderImpl adder) {
        generationAdder = adder;
    }

    @Override
    protected String getTypeDescription() {
        return "Boundary line";
    }

    @Override
    public BoundaryLineAdderImpl setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public BoundaryLineAdderImpl setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public BoundaryLineAdderImpl setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public BoundaryLineAdderImpl setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public BoundaryLineAdderImpl setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public BoundaryLineAdderImpl setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public BoundaryLineAdder setUcteXnodeCode(String ucteXnodeCode) {
        this.ucteXnodeCode = ucteXnodeCode;
        return this;
    }

    @Override
    public GenerationAdder newGeneration() {
        return new GenerationAdderImpl(this);
    }

    @Override
    public BoundaryLineImpl add() {
        NetworkImpl network = getNetwork();
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();

        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG(this, g);
        ValidationUtil.checkB(this, b);

        BoundaryLineImpl.GenerationImpl generation = null;
        if (generationAdder != null) {
            generation = generationAdder.build();
        }

        BoundaryLineImpl boundaryLine = new BoundaryLineImpl(network.getRef(), id, getName(), isFictitious(), p0, q0, r, x, g, b, ucteXnodeCode, generation);
        boundaryLine.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        network.getIndex().checkAndAdd(boundaryLine);
        network.getListeners().notifyCreation(boundaryLine);
        return boundaryLine;
    }

}
