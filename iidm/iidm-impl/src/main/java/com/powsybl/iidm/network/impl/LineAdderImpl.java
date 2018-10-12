/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LineAdder;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LineAdderImpl extends AbstractBranchAdder<LineAdderImpl> implements LineAdder {

    private final NetworkImpl network;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g1 = Double.NaN;

    private double b1 = Double.NaN;

    private double g2 = Double.NaN;

    private double b2 = Double.NaN;

    LineAdderImpl(NetworkImpl network) {
        this.network = network;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return network;
    }

    @Override
    protected String getTypeDescription() {
        return "AC Line";
    }

    @Override
    public LineAdderImpl setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public LineAdderImpl setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public LineAdderImpl setG1(double g1) {
        this.g1 = g1;
        return this;
    }

    @Override
    public LineAdderImpl setB1(double b1) {
        this.b1 = b1;
        return this;
    }

    @Override
    public LineAdderImpl setG2(double g2) {
        this.g2 = g2;
        return this;
    }

    @Override
    public LineAdderImpl setB2(double b2) {
        this.b2 = b2;
        return this;
    }

    @Override
    public LineImpl add() {
        String id = checkAndGetUniqueId();
        VoltageLevelExt voltageLevel1 = checkAndGetVoltageLevel1();
        VoltageLevelExt voltageLevel2 = checkAndGetVoltageLevel2();
        TerminalExt terminal1 = checkAndGetTerminal1();
        TerminalExt terminal2 = checkAndGetTerminal2();

        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG1(this, g1);
        ValidationUtil.checkG2(this, g2);
        ValidationUtil.checkB1(this, b1);
        ValidationUtil.checkB2(this, b2);

        // check that the line is attachable on both side
        voltageLevel1.attach(terminal1, true);
        voltageLevel2.attach(terminal2, true);

        LineImpl line = new LineImpl(id, getName(), r, x, g1, b1, g2, b2);
        terminal1.setNum(1);
        terminal2.setNum(2);
        line.addTerminal(terminal1);
        line.addTerminal(terminal2);
        voltageLevel1.attach(terminal1, false);
        voltageLevel2.attach(terminal2, false);
        network.getObjectStore().checkAndAdd(line);
        getNetwork().getListeners().notifyCreation(line);
        return line;
    }

}
