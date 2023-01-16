/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TieLineAdderImpl extends AbstractBranchAdder<TieLineAdderImpl> implements TieLineAdder {

    class HalfLineAdderImpl extends AbstractIdentifiableAdder<HalfLineAdderImpl> implements Validable, MergedDanglingLineAdder, GenerationAdderHolder<MergedDanglingLineAdder> {

        private final int num;

        private double p0 = Double.NaN;

        private double q0 = Double.NaN;

        private double r = Double.NaN;

        private double x = Double.NaN;

        private double g = 0.0;

        private double b = 0.0;

        private Integer node;
        private String connectableBus;
        private String bus;

        private String halfUcteXnodeCode = null;

        private GenerationAdderImpl<MergedDanglingLineAdder> generationAdder;

        HalfLineAdderImpl(int num) {
            this.num = num;
        }

        @Override
        public NetworkImpl getNetwork() {
            return network;
        }

        @Override
        public void setGenerationAdder(GenerationAdderImpl<MergedDanglingLineAdder> adder) {
            this.generationAdder = adder;
        }

        @Override
        protected String getTypeDescription() {
            return "TieLineAdder.DanglingLine" + num;
        }

        @Override
        public HalfLineAdderImpl setP0(double p0) {
            this.p0 = p0;
            return this;
        }

        @Override
        public HalfLineAdderImpl setQ0(double q0) {
            this.q0 = q0;
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
        public HalfLineAdderImpl setG(double g1) {
            this.g = g1;
            return this;
        }

        @Override
        public HalfLineAdderImpl setB(double b1) {
            this.b = b1;
            return this;
        }

        @Override
        public HalfLineAdderImpl setUcteXnodeCode(String ucteXnodeCode) {
            this.halfUcteXnodeCode = ucteXnodeCode;
            return this;
        }

        @Override
        public GenerationAdder<MergedDanglingLineAdder> newGeneration() {
            return new GenerationAdderImpl<>(this);
        }

        @Override
        public TieLineAdderImpl add() {
            if (Double.isNaN(r)) {
                throw new ValidationException(this, String.format("r is not set for half line %d", num));
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(this, String.format("x is not set for half line %d", num));
            }
            if (Double.isNaN(g)) {
                throw new ValidationException(this, String.format("g is not set for half line %d", num));
            }
            if (Double.isNaN(b)) {
                throw new ValidationException(this, String.format("b is not set for half line %d", num));
            }
            if (num == 1) {
                TieLineAdderImpl.this.halfLineAdder1 = this;
                setNullableNode1(node);
                setConnectableBus1(connectableBus);
                setBus1(bus);
            }
            if (num == 2) {
                TieLineAdderImpl.this.halfLineAdder2 = this;
                setNullableNode2(node);
                setConnectableBus2(connectableBus);
                setBus2(bus);
            }
            return TieLineAdderImpl.this;
        }

        private DanglingLineImpl build() {
            return new DanglingLineImpl(getNetwork().getRef(), checkAndGetUniqueId(id -> {
                Identifiable<?> i = getNetwork().getIndex().get(id);
                if (i != null) {
                    // a disconnected dangling line can have the same ID as a merged dangling line
                    return !(i instanceof DanglingLine);
                }
                return false;
            }),
                    getName(), isFictitious(), p0, q0, r, x, g, b,
                    halfUcteXnodeCode, generationAdder != null ? generationAdder.build() : null);
        }

        @Override
        public String getMessageHeader() {
            return String.format("TieLine.halfLine%d", num);
        }

        @Override
        public HalfLineAdderImpl setNode(int node) {
            this.node = node;
            return this;
        }

        @Override
        public HalfLineAdderImpl setBus(String bus) {
            this.bus = bus;
            return this;
        }

        @Override
        public HalfLineAdderImpl setConnectableBus(String connectableBus) {
            this.connectableBus = connectableBus;
            return this;
        }
    }

    private final NetworkImpl network;

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
    public HalfLineAdderImpl newHalf1() {
        return new HalfLineAdderImpl(1);
    }

    @Override
    public HalfLineAdderImpl newHalf2() {
        return new HalfLineAdderImpl(2);
    }

    @Override
    public TieLineImpl add() {
        String id = checkAndGetUniqueId();
        if (halfLineAdder1 == null) {
            throw new ValidationException(this, "half line 1 is not set");
        }

        if (halfLineAdder2 == null) {
            throw new ValidationException(this, "half line 2 is not set");
        }
        checkConnectableBuses();
        VoltageLevelExt voltageLevel1 = checkAndGetVoltageLevel1();
        VoltageLevelExt voltageLevel2 = checkAndGetVoltageLevel2();
        TerminalExt terminal1 = checkAndGetTerminal1();
        TerminalExt terminal2 = checkAndGetTerminal2();

        if (halfLineAdder1.halfUcteXnodeCode == null && halfLineAdder2.halfUcteXnodeCode == null) {
            throw new ValidationException(this, "ucteXnodeCode is not set");
        }
        if (!Objects.equals(halfLineAdder1.halfUcteXnodeCode, halfLineAdder2.halfUcteXnodeCode)) {
            if (halfLineAdder1.halfUcteXnodeCode == null) {
                halfLineAdder1.halfUcteXnodeCode = halfLineAdder2.halfUcteXnodeCode;
            } else if (halfLineAdder2.halfUcteXnodeCode == null) {
                halfLineAdder2.halfUcteXnodeCode = halfLineAdder1.halfUcteXnodeCode;
            } else {
                throw new ValidationException(this, "ucteXnodeCode is not consistent");
            }
        }

        DanglingLineImpl half1 = halfLineAdder1.build();
        DanglingLineImpl half2 = halfLineAdder2.build();

        // check that the line is attachable on both side
        voltageLevel1.attach(terminal1, true);
        voltageLevel2.attach(terminal2, true);

        TieLineImpl line = new TieLineImpl(network.getRef(), id, getName(), isFictitious());
        terminal1.setNum(1);
        terminal2.setNum(2);
        line.addTerminal(terminal1);
        line.addTerminal(terminal2);
        voltageLevel1.attach(terminal1, false);
        voltageLevel2.attach(terminal2, false);
        line.attachDanglingLines(half1, half2);
        network.getIndex().checkAndAdd(line);
        network.getListeners().notifyCreation(line);
        return line;
    }

}
