/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineAdderImpl extends AbstractBranchAdder<TieLineAdderImpl> implements TieLineAdder {

    class HalfLineAdderImpl implements Validable, TieLineAdder.HalfLineAdder {

        private final int num;

        protected String id;

        protected String name;

        protected boolean fictitious = false;

        protected Branch.Side originalBoundarySide;

        protected double r = Double.NaN;

        protected double x = Double.NaN;

        protected double g1 = Double.NaN;

        protected double g2 = Double.NaN;

        protected double b1 = Double.NaN;

        protected double b2 = Double.NaN;

        HalfLineAdderImpl(int num) {
            this.num = num;
        }

        @Override
        public HalfLineAdderImpl setId(String id) {
            this.id = id;
            return this;
        }

        @Override
        public HalfLineAdderImpl setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public HalfLineAdderImpl setFictitious(boolean fictitious) {
            this.fictitious = fictitious;
            return this;
        }

        @Override
        public HalfLineAdderImpl setOriginalBoundarySide(Branch.Side originalBoundarySide) {
            this.originalBoundarySide = originalBoundarySide;
            return this;
        }

        @Override
        public HalfLineAdderImpl setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public HalfLineAdderImpl setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public HalfLineAdderImpl setG1(double g1) {
            this.g1 = g1;
            return this;
        }

        @Override
        public HalfLineAdderImpl setG2(double g2) {
            this.g2 = g2;
            return this;
        }

        @Override
        public HalfLineAdderImpl setB1(double b1) {
            this.b1 = b1;
            return this;
        }

        @Override
        public HalfLineAdderImpl setB2(double b2) {
            this.b2 = b2;
            return this;
        }

        public TieLineAdderImpl add() {
            if (id == null || id.isEmpty()) {
                throw new ValidationException(this, String.format("id is not set for half line %d", num));
            }
            if (Double.isNaN(r)) {
                throw new ValidationException(this, String.format("r is not set for half line %d", num));
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(this, String.format("x is not set for half line %d", num));
            }
            if (Double.isNaN(g1)) {
                throw new ValidationException(this, String.format("g1 is not set for half line %d", num));
            }
            if (Double.isNaN(b1)) {
                throw new ValidationException(this, String.format("b1 is not set for half line %d", num));
            }
            if (Double.isNaN(g2)) {
                throw new ValidationException(this, String.format("g2 is not set for half line %d", num));
            }
            if (Double.isNaN(b2)) {
                throw new ValidationException(this, String.format("b2 is not set for half line %d", num));
            }
            if (num == 1) {
                TieLineAdderImpl.this.halfLineAdder1 = this;
            }
            if (num == 2) {
                TieLineAdderImpl.this.halfLineAdder2 = this;
            }
            return TieLineAdderImpl.this;
        }

        private TieLineImpl.HalfLineImpl build() {
            Branch.Side side = (num == 1) ? Branch.Side.ONE : Branch.Side.TWO;
            return new TieLineImpl.HalfLineImpl(id, name, fictitious, r, x, g1, b1, g2, b2, side, originalBoundarySide);
        }

        @Override
        public String getMessageHeader() {
            return String.format("TieLine.halfLine%d", num);
        }
    }

    private final NetworkImpl network;

    private String ucteXnodeCode;

    private HalfLineAdderImpl halfLineAdder1;

    private HalfLineAdderImpl halfLineAdder2;

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
    public HalfLineAdderImpl newHalfLine1() {
        return new HalfLineAdderImpl(1);
    }

    @Override
    public HalfLineAdderImpl newHalfLine2() {
        return new HalfLineAdderImpl(2);
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

        if (halfLineAdder1 == null) {
            throw new ValidationException(this, "half line 1 is not set");
        }

        if (halfLineAdder2 == null) {
            throw new ValidationException(this, "half line 2 is not set");
        }

        if (halfLineAdder1.originalBoundarySide == null) {
            throw new ValidationException(this, "originalBoundarySide of half line 1 is not set");
        }

        if (halfLineAdder2.originalBoundarySide == null) {
            throw new ValidationException(this, "originalBoundarySide of half line 2 is not set");
        }

        TieLineImpl.HalfLineImpl half1 = halfLineAdder1.build();
        TieLineImpl.HalfLineImpl half2 = halfLineAdder2.build();

        // check that the line is attachable on both side
        voltageLevel1.attach(terminal1, true);
        voltageLevel2.attach(terminal2, true);

        TieLineImpl line = new TieLineImpl(network.getRef(), id, getName(), isFictitious(), ucteXnodeCode, half1, half2);
        terminal1.setNum(1);
        terminal2.setNum(2);
        line.addTerminal(terminal1);
        line.addTerminal(terminal2);
        voltageLevel1.attach(terminal1, false);
        voltageLevel2.attach(terminal2, false);
        network.getIndex().checkAndAdd(line);
        getNetwork().getListeners().notifyCreation(line);
        return line;
    }

}
