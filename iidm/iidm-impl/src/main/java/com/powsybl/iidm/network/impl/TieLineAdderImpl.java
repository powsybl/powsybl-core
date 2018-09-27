/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.TieLineAdder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineAdderImpl extends AbstractBranchAdder<TieLineAdderImpl> implements TieLineAdder {

    private final NetworkImpl network;

    private String ucteXnodeCode;

    private TieLineImpl.HalfLineImpl half1 = new TieLineImpl.HalfLineImpl();

    private TieLineImpl.HalfLineImpl half2 = new TieLineImpl.HalfLineImpl();

    private TieLineImpl.HalfLineImpl activeHalf;

    TieLineAdderImpl(NetworkImpl network) {
        this.network = network;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return network;
    }

    @Override
    protected String getTypeDescription() {
        return "AC tie Line";
    }

    @Override
    public TieLineAdderImpl setUcteXnodeCode(String ucteXnodeCode) {
        this.ucteXnodeCode = ucteXnodeCode;
        return this;
    }

    @Override
    public TieLineAdderImpl line1() {
        activeHalf = half1;
        return this;
    }

    @Override
    public TieLineAdderImpl line2() {
        activeHalf = half2;
        return this;
    }

    private TieLineImpl.HalfLineImpl getActiveHalf() {
        if (activeHalf == null) {
            throw new ValidationException(this, "No active half of the line");
        }
        return activeHalf;
    }

    @Override
    public TieLineAdderImpl setId(String id) {
        if (activeHalf == null) {
            return super.setId(id);
        } else {
            getActiveHalf().setId(id);
        }
        return this;
    }

    @Override
    public TieLineAdderImpl setName(String name) {
        if (activeHalf == null) {
            return super.setName(name);
        } else {
            getActiveHalf().setName(name);
        }
        return this;
    }

    @Override
    public TieLineAdderImpl setR(double r) {
        getActiveHalf().setR(r);
        return this;
    }

    @Override
    public TieLineAdderImpl setX(double x) {
        getActiveHalf().setX(x);
        return this;
    }

    @Override
    public TieLineAdderImpl setG1(double g1) {
        getActiveHalf().setG1(g1);
        return this;
    }

    @Override
    public TieLineAdderImpl setG2(double g2) {
        getActiveHalf().setG2(g2);
        return this;
    }

    @Override
    public TieLineAdderImpl setB1(double b1) {
        getActiveHalf().setB1(b1);
        return this;
    }

    @Override
    public TieLineAdderImpl setB2(double b2) {
        getActiveHalf().setB2(b2);
        return this;
    }

    @Override
    public TieLineAdderImpl setXnodeP(double xnodeP) {
        getActiveHalf().setXnodeP(xnodeP);
        return this;
    }

    @Override
    public TieLineAdderImpl setXnodeQ(double xnodeQ) {
        getActiveHalf().setXnodeQ(xnodeQ);
        return this;
    }

    private void checkHalf(TieLineImpl.HalfLineImpl half, int num) {
        if (half.id == null) {
            throw new ValidationException(this, "id is not set for half line " + num);
        }
        if (Double.isNaN(half.r)) {
            throw new ValidationException(this, "r is not set for half line " + num);
        }
        if (Double.isNaN(half.x)) {
            throw new ValidationException(this, "x is not set for half line " + num);
        }
        if (Double.isNaN(half.g1)) {
            throw new ValidationException(this, "g1 is not set for half line " + num);
        }
        if (Double.isNaN(half.b1)) {
            throw new ValidationException(this, "b1 is not set for half line " + num);
        }
        if (Double.isNaN(half.g2)) {
            throw new ValidationException(this, "g2 is not set for half line " + num);
        }
        if (Double.isNaN(half.b2)) {
            throw new ValidationException(this, "b2 is not set for half line " + num);
        }
        if (Double.isNaN(half.xnodeP)) {
            throw new ValidationException(this, "xnodeP is not set for half line " + num);
        }
        if (Double.isNaN(half.xnodeQ)) {
            throw new ValidationException(this, "xnodeQ is not set for half line " + num);
        }
    }

    @Override
    public TieLineImpl add() {
        String id = checkAndGetUniqueId();
        VoltageLevelExt voltageLevel1 = checkAndGetVoltageLevel1();
        VoltageLevelExt voltageLevel2 = checkAndGetVoltageLevel2();
        TerminalExt terminal1 = checkAndGetTerminal1();
        TerminalExt terminal2 = checkAndGetTerminal2();

        if (ucteXnodeCode == null) {
            throw new ValidationException(this, "ucteXnodeCode is not set");
        }
        checkHalf(half1, 1);
        checkHalf(half2, 2);

        // check that the line is attachable on both side
        voltageLevel1.attach(terminal1, true);
        voltageLevel2.attach(terminal2, true);

        TieLineImpl line = new TieLineImpl(id, getName(), ucteXnodeCode, half1, half2);
        terminal1.setNum(1);
        terminal2.setNum(2);
        line.addTerminal(terminal1);
        line.addTerminal(terminal2);
        voltageLevel1.attach(terminal1, false);
        voltageLevel2.attach(terminal2, false);
        network.getObjectStore().checkAndAdd(line);
        return line;
    }

}
