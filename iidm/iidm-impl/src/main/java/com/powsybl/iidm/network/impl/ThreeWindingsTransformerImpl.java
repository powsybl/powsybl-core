/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
            double oldValue = this.r;
            this.r = r;
            transformer.notifyUpdate(() -> getLegAttribute() + ".r", oldValue, r);
            return (T) this;
        }

        public double getX() {
            return x;
        }

        public T setX(double x) {
            if (Double.isNaN(x)) {
                throw new ValidationException(this, "x is invalid");
            }
            double oldValue = this.x;
            this.x = x;
            transformer.notifyUpdate(() -> getLegAttribute() + ".x", oldValue, x);
            return (T) this;
        }

        public double getRatedU() {
            return ratedU;
        }

        public T setRatedU(double ratedU) {
            if (Double.isNaN(ratedU)) {
                throw new ValidationException(this, "rated U is invalid");
            }
            double oldValue = this.ratedU;
            this.ratedU = ratedU;
            transformer.notifyUpdate(() -> getLegAttribute() + ".ratedU", oldValue, x);
            return (T) this;
        }

        @Override
        public void setCurrentLimits(Void side, CurrentLimitsImpl limits) {
            CurrentLimits oldValue = this.limits;
            this.limits = limits;
            transformer.notifyUpdate(() -> getLegAttribute() + ".currentLimits", oldValue, x);
        }

        public CurrentLimits getCurrentLimits() {
            return limits;
        }

        public CurrentLimitsAdder newCurrentLimits() {
            return new CurrentLimitsAdderImpl<>(null, this);
        }

        protected String getTypeDescription() {
            return "3 windings transformer " + getLegAttribute();
        }

        @Override
        public String toString() {
            return transformer.getId() + " " + getLegAttribute();
        }

        public Identifiable getTransformer() {
            return transformer;
        }

        @Override
        public String getMessageHeader() {
            return getTypeDescription() + " '" + transformer.getId() + "': ";
        }

        protected abstract String getLegAttribute();
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
            double oldValue = this.g;
            this.g = g;
            transformer.notifyUpdate(() -> getLegAttribute() + ".g", oldValue, g);
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
            double oldValue = this.b;
            this.b = b;
            transformer.notifyUpdate(() -> getLegAttribute() + ".b", oldValue, b);
            return this;
        }

        @Override
        protected String getLegAttribute() {
            return "leg1";
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
        public Set<TapChanger> getAllTapChangers() {
            Set<TapChanger> tapChangers = new HashSet<TapChanger>();
            if (transformer.getLeg2().getRatioTapChanger() != null) {
                tapChangers.add(transformer.getLeg2().getRatioTapChanger());
            }
            if (transformer.getLeg3().getRatioTapChanger() != null) {
                tapChangers.add(transformer.getLeg3().getRatioTapChanger());
            }
            return tapChangers;
        }

        @Override
        public void setRatioTapChanger(RatioTapChangerImpl ratioTapChanger) {
            RatioTapChangerImpl oldValue = this.ratioTapChanger;
            this.ratioTapChanger = ratioTapChanger;
            transformer.notifyUpdate(() -> getLegAttribute() + "." + getTapChangerAttribute(), oldValue, ratioTapChanger);
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
            return "TapChanger2";
        }

        @Override
        protected String getLegAttribute() {
            return "leg2";
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
            return "TapChanger3";
        }

        @Override
        protected String getLegAttribute() {
            return "leg3";
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
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        if (leg2.getRatioTapChanger() != null) {
            leg2.getRatioTapChanger().extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        }
        if (leg3.getRatioTapChanger() != null) {
            leg3.getRatioTapChanger().extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        if (leg2.getRatioTapChanger() != null) {
            leg2.getRatioTapChanger().reduceVariantArraySize(number);
        }
        if (leg3.getRatioTapChanger() != null) {
            leg3.getRatioTapChanger().reduceVariantArraySize(number);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        if (leg2.getRatioTapChanger() != null) {
            leg2.getRatioTapChanger().deleteVariantArrayElement(index);
        }
        if (leg3.getRatioTapChanger() != null) {
            leg3.getRatioTapChanger().deleteVariantArrayElement(index);
        }
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        if (leg2.getRatioTapChanger() != null) {
            leg2.getRatioTapChanger().allocateVariantArrayElement(indexes, sourceIndex);
        }
        if (leg3.getRatioTapChanger() != null) {
            leg3.getRatioTapChanger().allocateVariantArrayElement(indexes, sourceIndex);
        }
    }

    @Override
    protected String getTypeDescription() {
        return "3 windings transformer";
    }

}
