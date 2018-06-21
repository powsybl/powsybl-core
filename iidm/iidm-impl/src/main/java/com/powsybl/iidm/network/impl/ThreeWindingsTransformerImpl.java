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
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ThreeWindingsTransformerImpl extends AbstractConnectable<ThreeWindingsTransformer> implements ThreeWindingsTransformer {

    abstract static class AbstractLegBase<T extends AbstractLegBase<T>> implements Validable, CurrentLimitsOwner<Void> {

        protected ThreeWindingsTransformerImpl transformer;

        private double r;

        private double x;

        private double ratedU;

        private CurrentLimits limits;

        AbstractLegBase(double r, double x, double ratedU) {
            this.r = r;
            this.x = x;
            this.ratedU = ratedU;
        }

        void setTransformer(ThreeWindingsTransformerImpl transformer) {
            this.transformer = transformer;
        }

        public TerminalExt getTerminal() {
            return transformer.terminals.get(0);
        }

        public double getR() {
            return r;
        }

        public T setR(double r) {
            if (Double.isNaN(r)) {
                throw new ValidationException(this, "r is invalid");
            }
            this.r = r;
            return (T) this;
        }

        public double getX() {
            return x;
        }

        public T setX(double x) {
            if (Double.isNaN(x)) {
                throw new ValidationException(this, "x is invalid");
            }
            this.x = x;
            return (T) this;
        }

        public double getRatedU() {
            return ratedU;
        }

        public T setRatedU(double ratedU) {
            if (Double.isNaN(ratedU)) {
                throw new ValidationException(this, "rated U is invalid");
            }
            this.ratedU = ratedU;
            return (T) this;
        }

        @Override
        public void setCurrentLimits(Void side, CurrentLimitsImpl limits) {
            this.limits = limits;
        }

        public CurrentLimits getCurrentLimits() {
            return limits;
        }

        public CurrentLimitsAdder newCurrentLimits() {
            return new CurrentLimitsAdderImpl<>(null, this);
        }

        protected abstract String getTypeDescription();

        public Identifiable getTransformer() {
            return transformer;
        }

        @Override
        public String getMessageHeader() {
            return getTypeDescription() + " '" + transformer.getId() + "': ";
        }

    }

    static class Leg1Impl extends AbstractLegBase<Leg1Impl> implements Leg1 {

        private double g;

        private double b;

        Leg1Impl(double r, double x, double g, double b, double ratedU) {
            super(r, x, ratedU);
            this.g = g;
            this.b = b;
        }

        @Override
        public double getG() {
            return g;
        }

        @Override
        public Leg1Impl setG(double g) {
            if (Double.isNaN(g)) {
                throw new ValidationException(this, "g is invalid");
            }
            this.g = g;
            return this;
        }

        @Override
        public double getB() {
            return b;
        }

        @Override
        public Leg1Impl setB(double b) {
            if (Double.isNaN(b)) {
                throw new ValidationException(this, "b is invalid");
            }
            this.b = b;
            return this;
        }

        @Override
        public TerminalExt getTerminal() {
            return transformer.terminals.get(0);
        }

        @Override
        public String getTypeDescription() {
            return "3 windings transformer leg 1";
        }

        @Override
        public String toString() {
            return transformer.getId() + " leg 1";
        }

    }

    private abstract static class AbstractLeg2or3<T extends AbstractLeg2or3<T>> extends AbstractLegBase<T> implements RatioTapChangerParent {

        private RatioTapChangerImpl ratioTapChanger;

        AbstractLeg2or3(double r, double x, double ratedU) {
            super(r, x, ratedU);
        }

        public RatioTapChangerAdderImpl newRatioTapChanger() {
            return new RatioTapChangerAdderImpl(this);
        }

        public RatioTapChangerImpl getRatioTapChanger() {
            return ratioTapChanger;
        }

        @Override
        public NetworkImpl getNetwork() {
            return transformer.getSubstation().getNetwork();
        }

        @Override
        public void setRatioTapChanger(RatioTapChangerImpl ratioTapChanger) {
            this.ratioTapChanger = ratioTapChanger;
        }

    }

    static class Leg2Impl extends AbstractLeg2or3<Leg2Impl> implements Leg2or3 {

        Leg2Impl(double r, double x, double ratedU) {
            super(r, x, ratedU);
        }

        @Override
        public TerminalExt getTerminal() {
            return transformer.terminals.get(1);
        }

        @Override
        public String getTapChangerAttribute() {
            return "ratioTapChanger2";
        }

        @Override
        public String getTypeDescription() {
            return "3 windings transformer leg 2";
        }

        @Override
        public String toString() {
            return transformer.getId() + " leg 2";
        }

    }

    static class Leg3Impl extends AbstractLeg2or3<Leg3Impl> implements Leg2or3 {

        Leg3Impl(double r, double x, double ratedU) {
            super(r, x, ratedU);
        }

        @Override
        public TerminalExt getTerminal() {
            return transformer.terminals.get(2);
        }

        @Override
        public String getTapChangerAttribute() {
            return "ratioTapChanger3";
        }

        @Override
        public String getTypeDescription() {
            return "3 windings transformer leg 3";
        }

        @Override
        public String toString() {
            return transformer.getId() + " leg 3";
        }

    }

    private final Leg1Impl leg1;

    private final Leg2Impl leg2;

    private final Leg3Impl leg3;

    ThreeWindingsTransformerImpl(String id, String name, Leg1Impl leg1, Leg2Impl leg2, Leg3Impl leg3) {
        super(id, name);
        this.leg1 = Objects.requireNonNull(leg1);
        this.leg2 = Objects.requireNonNull(leg2);
        this.leg3 = Objects.requireNonNull(leg3);
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.THREE_WINDINGS_TRANSFORMER;
    }

    @Override
    public SubstationImpl getSubstation() {
        return leg1.getTerminal().getVoltageLevel().getSubstation();
    }

    @Override
    public Leg1Impl getLeg1() {
        return leg1;
    }

    @Override
    public Leg2Impl getLeg2() {
        return leg2;
    }

    @Override
    public Leg3Impl getLeg3() {
        return leg3;
    }

    @Override
    public Terminal getTerminal(Side side) {
        switch (side) {
            case ONE:
                return getLeg1().getTerminal();

            case TWO:
                return getLeg2().getTerminal();

            case THREE:
                return getLeg3().getTerminal();

            default:
                throw new AssertionError();
        }
    }

    @Override
    public Side getSide(Terminal terminal) {
        Objects.requireNonNull(terminal);

        if (getLeg1().getTerminal() == terminal) {
            return Side.ONE;
        } else if (getLeg2().getTerminal() == terminal) {
            return Side.TWO;
        } else if (getLeg3().getTerminal() == terminal) {
            return Side.THREE;
        } else {
            throw new AssertionError("The terminal is not connected to this three windings transformer");
        }
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        if (leg2.getRatioTapChanger() != null) {
            leg2.getRatioTapChanger().extendStateArraySize(initStateArraySize, number, sourceIndex);
        }
        if (leg3.getRatioTapChanger() != null) {
            leg3.getRatioTapChanger().extendStateArraySize(initStateArraySize, number, sourceIndex);
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        if (leg2.getRatioTapChanger() != null) {
            leg2.getRatioTapChanger().reduceStateArraySize(number);
        }
        if (leg3.getRatioTapChanger() != null) {
            leg3.getRatioTapChanger().reduceStateArraySize(number);
        }
    }

    @Override
    public void deleteStateArrayElement(int index) {
        super.deleteStateArrayElement(index);
        if (leg2.getRatioTapChanger() != null) {
            leg2.getRatioTapChanger().deleteStateArrayElement(index);
        }
        if (leg3.getRatioTapChanger() != null) {
            leg3.getRatioTapChanger().deleteStateArrayElement(index);
        }
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        super.allocateStateArrayElement(indexes, sourceIndex);
        if (leg2.getRatioTapChanger() != null) {
            leg2.getRatioTapChanger().allocateStateArrayElement(indexes, sourceIndex);
        }
        if (leg3.getRatioTapChanger() != null) {
            leg3.getRatioTapChanger().allocateStateArrayElement(indexes, sourceIndex);
        }
    }

    @Override
    protected String getTypeDescription() {
        return "3 windings transformer";
    }

}
