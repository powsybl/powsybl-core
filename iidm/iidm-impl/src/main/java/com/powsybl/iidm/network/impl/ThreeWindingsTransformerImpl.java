/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;

import static com.powsybl.iidm.network.TapChanger.Kind.PHASE_TAP_CHANGER;
import static com.powsybl.iidm.network.TapChanger.Kind.RATIO_TAP_CHANGER;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ThreeWindingsTransformerImpl extends AbstractConnectable<ThreeWindingsTransformer> implements ThreeWindingsTransformer {

    private static final String UNEXPECTED_TAP_CHANGER = "Unexpected type of tap changer for transformer ";

    abstract static class AbstractLeg<T extends AbstractLeg<T>> implements Validable, CurrentLimitsOwner<Void>, TapChangerOwner, RatioTapChangerParent, PhaseTapChangerParent {

        protected ThreeWindingsTransformerImpl transformer;

        private int leg;

        private double r;

        private double x;

        private double g;

        private double b;

        private double ratedU;

        private CurrentLimits limits;

        private TapChangerHolderImpl tapChanger;

        AbstractLeg(int leg, double r, double x, double g, double b, double ratedU) {
            this.leg = leg;
            this.r = r;
            this.x = x;
            this.g = g;
            this.b = b;
            this.ratedU = ratedU;
            tapChanger = new TapChangerHolderImpl(this);
        }

        void setTransformer(ThreeWindingsTransformerImpl transformer) {
            this.transformer = transformer;
        }

        public TerminalExt getTerminal() {
            return transformer.terminals.get(leg - 1);
        }

        @Override
        public String getRatioTapChangerAttribute() {
            return "ratioTapChanger" + leg;
        }

        @Override
        public String getPhaseTapChangerAttribute() {
            return "phaseTapChanger" + leg;
        }

        public String getTypeDescription() {
            return "3 windings transformer leg " + leg;
        }

        @Override
        public String toString() {
            return transformer.getId() + " leg " + leg;
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

        public double getG() {
            return g;
        }

        public T setG(double g) {
            if (Double.isNaN(g)) {
                throw new ValidationException(this, "g is invalid");
            }
            this.g = g;
            return (T) this;
        }

        public double getB() {
            return b;
        }

        public T setB(double b) {
            if (Double.isNaN(b)) {
                throw new ValidationException(this, "b is invalid");
            }
            this.b = b;
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

        @Override
        public void setTapChanger(TapChanger tapChanger) {
            this.tapChanger.setTapChanger(tapChanger);
        }

        @Override
        public void setRatioTapChanger(RatioTapChangerImpl ratioTapChanger) {
            setTapChanger(ratioTapChanger);
        }

        @Override
        public void setPhaseTapChanger(PhaseTapChangerImpl phaseTapChanger) {
            setTapChanger(phaseTapChanger);
        }

        public TapChanger getTapChanger() {
            return tapChanger.getTapChanger();
        }

        public <T extends TapChanger> T getTapChanger(Class<T> type) {
            return tapChanger.getTapChanger(type);
        }

        public RatioTapChangerAdderImpl newRatioTapChanger() {
            return new RatioTapChangerAdderImpl(this);
        }

        public PhaseTapChangerAdderImpl newPhaseTapChanger() {
            return new PhaseTapChangerAdderImpl(this);
        }

        @Override
        public NetworkImpl getNetwork() {
            return transformer.getSubstation().getNetwork();
        }

        public Identifiable getTransformer() {
            return transformer;
        }

        @Override
        public String getMessageHeader() {
            return getTypeDescription() + " '" + transformer.getId() + "': ";
        }

    }

    static class Leg1Impl extends AbstractLeg<Leg1Impl> implements Leg {

        Leg1Impl(double r, double x, double g, double b, double ratedU) {
            super(1, r, x, g, b, ratedU);
        }
    }

    static class Leg2Impl extends AbstractLeg<Leg2Impl> implements Leg {

        Leg2Impl(double r, double x, double g, double b, double ratedU) {
            super(2, r, x, g, b, ratedU);
        }
    }

    static class Leg3Impl extends AbstractLeg<Leg3Impl> implements Leg {

        Leg3Impl(double r, double x, double g, double b, double ratedU) {
            super(3, r, x, g, b, ratedU);
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

    private static <L extends Leg> void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex, L leg, String transformerId) {
        if (leg.getTapChanger() != null) {
            switch (leg.getTapChanger().getKind()) {
                case RATIO_TAP_CHANGER:
                    leg.getTapChanger(RatioTapChangerImpl.class).extendVariantArraySize(initVariantArraySize, number, sourceIndex);
                    break;
                case PHASE_TAP_CHANGER:
                    leg.getTapChanger(PhaseTapChangerImpl.class).extendVariantArraySize(initVariantArraySize, number, sourceIndex);
                    break;
                default:
                    throw new PowsyblException(UNEXPECTED_TAP_CHANGER + transformerId);
            }
        }
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        extendVariantArraySize(initVariantArraySize, number, sourceIndex, leg1, id);
        extendVariantArraySize(initVariantArraySize, number, sourceIndex, leg2, id);
        extendVariantArraySize(initVariantArraySize, number, sourceIndex, leg3, id);
    }

    private static <L extends Leg> void reduceVariantArraySize(int number, L leg, String transformerId) {
        if (leg.getTapChanger() != null) {
            switch (leg.getTapChanger().getKind()) {
                case RATIO_TAP_CHANGER:
                    leg.getTapChanger(RatioTapChangerImpl.class).reduceVariantArraySize(number);
                    break;
                case PHASE_TAP_CHANGER:
                    leg.getTapChanger(PhaseTapChangerImpl.class).reduceVariantArraySize(number);
                    break;
                default:
                    throw new PowsyblException(UNEXPECTED_TAP_CHANGER + transformerId);
            }
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        reduceVariantArraySize(number, leg1, id);
        reduceVariantArraySize(number, leg2, id);
        reduceVariantArraySize(number, leg3, id);
    }

    private static <L extends Leg> void deleteVariantArrayElement(int index, L leg, String transformerId) {
        if (leg.getTapChanger() != null) {
            switch (leg.getTapChanger().getKind()) {
                case RATIO_TAP_CHANGER:
                    leg.getTapChanger(RatioTapChangerImpl.class).deleteVariantArrayElement(index);
                    break;
                case PHASE_TAP_CHANGER:
                    leg.getTapChanger(PhaseTapChangerImpl.class).deleteVariantArrayElement(index);
                    break;
                default:
                    throw new PowsyblException(UNEXPECTED_TAP_CHANGER + transformerId);
            }
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        deleteVariantArrayElement(index, leg1, id);
        deleteVariantArrayElement(index, leg2, id);
        deleteVariantArrayElement(index, leg3, id);
    }

    private static <L extends Leg> void allocateVariantArrayElement(int[] indexes, int sourceIndex, L leg, String transformerId) {
        if (leg.getTapChanger() != null) {
            switch (leg.getTapChanger().getKind()) {
                case RATIO_TAP_CHANGER:
                    leg.getTapChanger(RatioTapChangerImpl.class).allocateVariantArrayElement(indexes, sourceIndex);
                    break;
                case PHASE_TAP_CHANGER:
                    leg.getTapChanger(PhaseTapChangerImpl.class).allocateVariantArrayElement(indexes, sourceIndex);
                    break;
                default:
                    throw new PowsyblException(UNEXPECTED_TAP_CHANGER + transformerId);
            }
        }
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        allocateVariantArrayElement(indexes, sourceIndex, leg1, id);
        allocateVariantArrayElement(indexes, sourceIndex, leg2, id);
        allocateVariantArrayElement(indexes, sourceIndex, leg3, id);
    }

    @Override
    protected String getTypeDescription() {
        return "3 windings transformer";
    }

}
