/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.util.TieLineUtil;

import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class TieLineImpl extends AbstractBranch<Line> implements TieLine {

    @Override
    protected String getTypeDescription() {
        return "AC Line";
    }

    static class MergedDanglingLine extends AbstractIdentifiable<DanglingLine> implements DanglingLine {

        TieLineImpl parent;

        private final double initialP0;

        private final double initialQ0;

        private final double initialR;

        private final double initialX;

        private final double initialG;

        private final double initialB;

        private final String ucteXnodeCode;

        private final Side side;

        private final DanglingLineCharacteristics.GenerationImpl generation;

        private DanglingLineCharacteristics characteristics;

        MergedDanglingLine(String id, String name, boolean fictitious, double p0, double q0, double r, double x, double g, double b, String ucteXnodeCode,
                           DanglingLineCharacteristics.GenerationImpl generation, Branch.Side side) {
            super(id, name, fictitious);
            initialP0 = p0;
            initialQ0 = q0;
            initialR = r;
            initialX = x;
            initialG = g;
            initialB = b;
            this.ucteXnodeCode = ucteXnodeCode;
            this.generation = generation;
            this.side = side;
        }

        private void setParent(TieLineImpl parent) {
            this.parent = parent;
            characteristics = new DanglingLineCharacteristics(parent, new MergedDanglingLineBoundaryImpl(this, side), initialP0, initialQ0, initialR, initialX, initialG, initialB,
                    ucteXnodeCode, generation);
        }

        @Override
        public boolean isMerged() {
            return true;
        }

        @Override
        public double getP0() {
            return characteristics.getP0();
        }

        @Override
        public DanglingLine setP0(double p0) {
            characteristics.setP0(p0, false);
            return this;
        }

        @Override
        public double getQ0() {
            return characteristics.getQ0();
        }

        @Override
        public DanglingLine setQ0(double q0) {
            characteristics.setQ0(q0, false);
            return this;
        }

        @Override
        public double getR() {
            return characteristics.getR();
        }

        @Override
        public MergedDanglingLine setR(double r) {
            characteristics.setR(r);
            return this;
        }

        @Override
        public double getX() {
            return characteristics.getX();
        }

        @Override
        public MergedDanglingLine setX(double x) {
            characteristics.setX(x);
            return this;
        }

        @Override
        public double getG() {
            return characteristics.getG();
        }

        @Override
        public MergedDanglingLine setG(double g) {
            characteristics.setG(g);
            return this;
        }

        @Override
        public double getB() {
            return characteristics.getB();
        }

        @Override
        public MergedDanglingLine setB(double b) {
            characteristics.setB(b);
            return this;
        }

        @Override
        public Generation getGeneration() {
            return DanglingLine.super.getGeneration();
        }

        @Override
        public String getUcteXnodeCode() {
            return null;
        }

        @Override
        public Boundary getBoundary() {
            return characteristics.getBoundary();
        }

        @Override
        public List<? extends Terminal> getTerminals() {
            return Collections.singletonList(parent.getTerminal(side));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Parent tie line " + parent.getId() + " should be removed, not the child dangling line");
        }

        @Override
        public Collection<OperationalLimits> getOperationalLimits() {
            return parent.getLimitsHolder(side).getOperationalLimits();
        }

        @Override
        public Optional<CurrentLimits> getCurrentLimits() {
            return parent.getLimitsHolder(side).getOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
        }

        @Override
        public CurrentLimits getNullableCurrentLimits() {
            return parent.getLimitsHolder(side).getNullableOperationalLimits(LimitType.CURRENT, CurrentLimits.class);
        }

        @Override
        public Optional<ActivePowerLimits> getActivePowerLimits() {
            return parent.getLimitsHolder(side).getOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
        }

        @Override
        public ActivePowerLimits getNullableActivePowerLimits() {
            return parent.getLimitsHolder(side).getNullableOperationalLimits(LimitType.ACTIVE_POWER, ActivePowerLimits.class);
        }

        @Override
        public Optional<ApparentPowerLimits> getApparentPowerLimits() {
            return parent.getLimitsHolder(side).getOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
        }

        @Override
        public ApparentPowerLimits getNullableApparentPowerLimits() {
            return parent.getLimitsHolder(side).getNullableOperationalLimits(LimitType.APPARENT_POWER, ApparentPowerLimits.class);
        }

        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            return parent.getLimitsHolder(side).newCurrentLimits();
        }

        @Override
        public ApparentPowerLimitsAdder newApparentPowerLimits() {
            return parent.getLimitsHolder(side).newApparentPowerLimits();
        }

        @Override
        public ActivePowerLimitsAdder newActivePowerLimits() {
            return parent.getLimitsHolder(side).newActivePowerLimits();
        }

        @Override
        public Terminal getTerminal() {
            return parent.getTerminal(side);
        }

        @Override
        public NetworkImpl getNetwork() {
            return parent.getNetwork();
        }

        @Override
        protected String getTypeDescription() {
            return "TieLine.DanglingLine";
        }
    }

    private final MergedDanglingLine half1;

    private final MergedDanglingLine half2;

    TieLineImpl(Ref<NetworkImpl> network, String id, String name, boolean fictitious, MergedDanglingLine half1, MergedDanglingLine half2) {
        super(network, id, name, fictitious);
        this.half1 = attach(half1);
        this.half2 = attach(half2);
    }

    private MergedDanglingLine attach(MergedDanglingLine half) {
        half.setParent(this);
        return half;
    }

    @Override
    public boolean isTieLine() {
        return true;
    }

    @Override
    public String getUcteXnodeCode() {
        return half1.ucteXnodeCode;
    }

    @Override
    public MergedDanglingLine getHalf1() {
        return half1;
    }

    @Override
    public MergedDanglingLine getHalf2() {
        return half2;
    }

    @Override
    public MergedDanglingLine getHalf(Side side) {
        switch (side) {
            case ONE:
                return half1;
            case TWO:
                return half2;
            default:
                throw new AssertionError("Unknown branch side " + side);
        }
    }

    // Half1 and half2 are lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getR() {
        return TieLineUtil.getR(half1, half2);
    }

    private ValidationException createNotSupportedForTieLines() {
        return new ValidationException(this, "direct modification of characteristics not supported for tie lines");
    }

    @Override
    public LineImpl setR(double r) {
        throw createNotSupportedForTieLines();
    }

    // Half1 and half2 are lines, so the transmission impedance of the equivalent branch is symmetric
    @Override
    public double getX() {
        return TieLineUtil.getX(half1, half2);
    }

    @Override
    public LineImpl setX(double x) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getG1() {
        return TieLineUtil.getG1(half1, half2);
    }

    @Override
    public LineImpl setG1(double g1) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getB1() {
        return TieLineUtil.getB1(half1, half2);
    }

    @Override
    public LineImpl setB1(double b1) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getG2() {
        return TieLineUtil.getG2(half1, half2);
    }

    @Override
    public LineImpl setG2(double g2) {
        throw createNotSupportedForTieLines();
    }

    @Override
    public double getB2() {
        return TieLineUtil.getB2(half1, half2);
    }

    @Override
    public LineImpl setB2(double b2) {
        throw createNotSupportedForTieLines();
    }

    private OperationalLimitsHolderImpl getLimitsHolder(Side side) {
        if (side == Side.ONE) {
            return operationalLimitsHolder1;
        }
        if (side == Side.TWO) {
            return operationalLimitsHolder2;
        }
        throw new AssertionError(); // should not happen
    }
}
