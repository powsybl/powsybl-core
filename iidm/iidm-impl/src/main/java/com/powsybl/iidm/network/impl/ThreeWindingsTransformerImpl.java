/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ThreeWindingsTransformerImpl extends AbstractConnectable<ThreeWindingsTransformer>
    implements ThreeWindingsTransformer {

    static class LegImpl
        implements Validable, CurrentLimitsOwner<Void>, Leg, RatioTapChangerParent, PhaseTapChangerParent {

        protected ThreeWindingsTransformerImpl transformer;

        private double r;

        private double x;

        private double g;

        private double b;

        private double ratedU;

        private double ratedS;

        private CurrentLimits limits;

        private RatioTapChangerImpl ratioTapChanger;

        private PhaseTapChangerImpl phaseTapChanger;

        private int legNumber = 0;

        LegImpl(double r, double x, double g, double b, double ratedU, double ratedS, int legNumber) {
            this.r = r;
            this.x = x;
            this.g = g;
            this.b = b;
            this.ratedU = ratedU;
            this.legNumber = legNumber;
            this.ratedS = ratedS;
        }

        void setTransformer(ThreeWindingsTransformerImpl transformer) {
            this.transformer = transformer;
        }

        public TerminalExt getTerminal() {
            return transformer.terminals.get(legNumber - 1);
        }

        public double getR() {
            return r;
        }

        public Leg setR(double r) {
            if (Double.isNaN(r)) {
                throw new ValidationException(this, "r is invalid");
            }
            double oldValue = this.r;
            this.r = r;
            transformer.notifyUpdate(() -> getLegAttribute() + ".r", oldValue, r);
            return this;
        }

        public double getX() {
            return x;
        }

        public Leg setX(double x) {
            if (Double.isNaN(x)) {
                throw new ValidationException(this, "x is invalid");
            }
            double oldValue = this.x;
            this.x = x;
            transformer.notifyUpdate(() -> getLegAttribute() + ".x", oldValue, x);
            return this;
        }

        public double getG() {
            return g;
        }

        public Leg setG(double g) {
            if (Double.isNaN(g)) {
                throw new ValidationException(this, "g is invalid");
            }
            double oldValue = this.g;
            this.g = g;
            transformer.notifyUpdate(() -> getLegAttribute() + ".g", oldValue, g);
            return this;
        }

        public double getB() {
            return b;
        }

        public Leg setB(double b) {
            if (Double.isNaN(b)) {
                throw new ValidationException(this, "b is invalid");
            }
            double oldValue = this.b;
            this.b = b;
            transformer.notifyUpdate(() -> getLegAttribute() + ".b", oldValue, b);
            return this;
        }

        public double getRatedU() {
            return ratedU;
        }

        public Leg setRatedU(double ratedU) {
            ValidationUtil.checkRatedU(this, ratedU, "");
            double oldValue = this.ratedU;
            this.ratedU = ratedU;
            transformer.notifyUpdate(() -> getLegAttribute() + ".ratedU", oldValue, ratedU);
            return this;
        }

        public RatioTapChangerAdderImpl newRatioTapChanger() {
            return new RatioTapChangerAdderImpl(this);
        }

        public RatioTapChangerImpl getRatioTapChanger() {
            return ratioTapChanger;
        }

        @Override
        public Optional<RatioTapChanger> getOptionalRatioTapChanger() {
            return Optional.ofNullable(ratioTapChanger);
        }

        public PhaseTapChangerAdderImpl newPhaseTapChanger() {
            return new PhaseTapChangerAdderImpl(this);
        }

        public PhaseTapChangerImpl getPhaseTapChanger() {
            return phaseTapChanger;
        }

        @Override
        public Optional<PhaseTapChanger> getOptionalPhaseTapChanger() {
            return Optional.ofNullable(phaseTapChanger);
        }

        @Override
        public NetworkImpl getNetwork() {
            return transformer.getNetwork();
        }

        @Override
        public void setRatioTapChanger(RatioTapChangerImpl ratioTapChanger) {
            RatioTapChangerImpl oldValue = this.ratioTapChanger;
            this.ratioTapChanger = ratioTapChanger;
            transformer.notifyUpdate(() -> getLegAttribute() + "." + getTapChangerAttribute(), oldValue,
                ratioTapChanger);
        }

        @Override
        public void setPhaseTapChanger(PhaseTapChangerImpl phaseTapChanger) {
            PhaseTapChangerImpl oldValue = this.phaseTapChanger;
            this.phaseTapChanger = phaseTapChanger;
            transformer.notifyUpdate(() -> getLegAttribute() + "." + getTapChangerAttribute(), oldValue,
                phaseTapChanger);
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

        public String getTapChangerAttribute() {
            return String.format("TapChanger%d", legNumber);
        }

        protected String getLegAttribute() {
            return String.format("leg%d", legNumber);
        }

        @Override
        public Set<TapChanger> getAllTapChangers() {
            Set<TapChanger> tapChangers = new HashSet<>();
            transformer.leg1.getOptionalRatioTapChanger().ifPresent(tapChangers::add);
            transformer.leg1.getOptionalPhaseTapChanger().ifPresent(tapChangers::add);
            transformer.leg2.getOptionalRatioTapChanger().ifPresent(tapChangers::add);
            transformer.leg2.getOptionalPhaseTapChanger().ifPresent(tapChangers::add);
            transformer.leg3.getOptionalRatioTapChanger().ifPresent(tapChangers::add);
            transformer.leg3.getOptionalPhaseTapChanger().ifPresent(tapChangers::add);
            return tapChangers;
        }

        @Override
        public boolean hasRatioTapChanger() {
            return ratioTapChanger != null;
        }

        @Override
        public boolean hasPhaseTapChanger() {
            return phaseTapChanger != null;
        }

        @Override
        public double getRatedS() {
            return ratedS;
        }

        @Override
        public LegImpl setRatedS(double ratedS) {
            ValidationUtil.checkRatedS(this, ratedS);
            double oldValue = this.ratedS;
            this.ratedS = ratedS;
            transformer.notifyUpdate(() -> getLegAttribute() + ".ratedS", oldValue, ratedS);
            return this;
        }
    }

    private final LegImpl leg1;

    private final LegImpl leg2;

    private final LegImpl leg3;

    private double ratedU0;

    ThreeWindingsTransformerImpl(String id, String name, boolean fictitious, LegImpl leg1, LegImpl leg2, LegImpl leg3, double ratedU0) {
        super(id, name, fictitious);
        this.leg1 = Objects.requireNonNull(leg1);
        this.leg2 = Objects.requireNonNull(leg2);
        this.leg3 = Objects.requireNonNull(leg3);
        this.ratedU0 = ratedU0;
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
    public LegImpl getLeg1() {
        return leg1;
    }

    @Override
    public LegImpl getLeg2() {
        return leg2;
    }

    @Override
    public LegImpl getLeg3() {
        return leg3;
    }

    @Override
    public Stream<Leg> getLegStream() {
        return Stream.of(leg1, leg2, leg3);
    }

    @Override
    public List<Leg> getLegs() {
        return Arrays.asList(leg1, leg2, leg3);
    }

    @Override
    public double getRatedU0() {
        return ratedU0;
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
        leg1.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).extendVariantArraySize(initVariantArraySize, number, sourceIndex));
        leg1.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).extendVariantArraySize(initVariantArraySize, number, sourceIndex));
        leg2.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).extendVariantArraySize(initVariantArraySize, number, sourceIndex));
        leg2.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).extendVariantArraySize(initVariantArraySize, number, sourceIndex));
        leg3.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).extendVariantArraySize(initVariantArraySize, number, sourceIndex));
        leg3.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).extendVariantArraySize(initVariantArraySize, number, sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        leg1.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).reduceVariantArraySize(number));
        leg1.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).reduceVariantArraySize(number));
        leg2.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).reduceVariantArraySize(number));
        leg2.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).reduceVariantArraySize(number));
        leg3.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).reduceVariantArraySize(number));
        leg3.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).reduceVariantArraySize(number));
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        leg1.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).deleteVariantArrayElement(index));
        leg1.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).deleteVariantArrayElement(index));
        leg2.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).deleteVariantArrayElement(index));
        leg2.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).deleteVariantArrayElement(index));
        leg3.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).deleteVariantArrayElement(index));
        leg3.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).deleteVariantArrayElement(index));
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        leg1.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).allocateVariantArrayElement(indexes, sourceIndex));
        leg1.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).allocateVariantArrayElement(indexes, sourceIndex));
        leg2.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).allocateVariantArrayElement(indexes, sourceIndex));
        leg2.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).allocateVariantArrayElement(indexes, sourceIndex));
        leg3.getOptionalRatioTapChanger().ifPresent(rtc -> ((RatioTapChangerImpl) rtc).allocateVariantArrayElement(indexes, sourceIndex));
        leg3.getOptionalPhaseTapChanger().ifPresent(ptc -> ((PhaseTapChangerImpl) ptc).allocateVariantArrayElement(indexes, sourceIndex));
    }

    @Override
    protected String getTypeDescription() {
        return "3 windings transformer";
    }
}
