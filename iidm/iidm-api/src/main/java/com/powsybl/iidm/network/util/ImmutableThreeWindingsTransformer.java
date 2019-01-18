/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableThreeWindingsTransformer extends AbstractImmutableIdentifiable<ThreeWindingsTransformer> implements ThreeWindingsTransformer {

    private static final Map<ThreeWindingsTransformer, ImmutableThreeWindingsTransformer> CACHE = new HashMap<>();

    private ImmutableThreeWindingsTransformer(ThreeWindingsTransformer identifiable) {
        super(identifiable);
    }

    static ThreeWindingsTransformer ofNullable(ThreeWindingsTransformer threeWindingsTransformer) {
        return null == threeWindingsTransformer ? null : CACHE.computeIfAbsent(threeWindingsTransformer, k -> new ImmutableThreeWindingsTransformer(threeWindingsTransformer));
    }

    @Override
    public Terminal getTerminal(Side side) {
        return ImmutableTerminal.ofNullable(identifiable.getTerminal(side));
    }

    @Override
    public Side getSide(Terminal terminal) {
        if (terminal instanceof ImmutableTerminal) {
            return identifiable.getSide(((ImmutableTerminal) terminal).getTerminal());
        } else {
            return identifiable.getSide(terminal);
        }
    }

    @Override
    public Substation getSubstation() {
        return ImmutableSubstation.ofNullable(identifiable.getSubstation());
    }

    @Override
    public Leg1 getLeg1() {
        return new Leg1() {
            @Override
            public double getG() {
                return identifiable.getLeg1().getG();
            }

            @Override
            public Leg1 setG(double g) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public double getB() {
                return identifiable.getLeg1().getB();
            }

            @Override
            public Leg1 setB(double b) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public Terminal getTerminal() {
                return ImmutableTerminal.ofNullable(identifiable.getLeg1().getTerminal());
            }

            @Override
            public double getR() {
                return identifiable.getLeg1().getR();
            }

            @Override
            public Leg1 setR(double r) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public double getX() {
                return identifiable.getLeg1().getX();
            }

            @Override
            public Leg1 setX(double x) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public double getRatedU() {
                return identifiable.getLeg1().getRatedU();
            }

            @Override
            public Leg1 setRatedU(double ratedU) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            @Override
            public CurrentLimits getCurrentLimits() {
                return ImmutableCurrentLimits.ofNullable(identifiable.getLeg1().getCurrentLimits());
            }

            @Override
            public CurrentLimitsAdder newCurrentLimits() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }
        };
    }

    @Override
    public Leg2or3 getLeg2() {
        return ImmutableLeg2or3.ofNullable(identifiable.getLeg2());
    }

    @Override
    public Leg2or3 getLeg3() {
        return ImmutableLeg2or3.ofNullable(identifiable.getLeg3());
    }

    @Override
    public ConnectableType getType() {
        return identifiable.getType();
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return identifiable.getTerminals().stream().map(t -> ImmutableTerminal.ofNullable(t)).collect(Collectors.toList());
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    public static class ImmutableLeg2or3 implements Leg2or3 {

        Leg2or3 leg;

        ImmutableLeg2or3(Leg2or3 leg) {
            this.leg = Objects.requireNonNull(leg);
        }

        static ImmutableLeg2or3 ofNullable(Leg2or3 leg) {
            return null == leg ? null : new ImmutableLeg2or3(leg);
        }

        @Override
        public RatioTapChangerAdder newRatioTapChanger() {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public RatioTapChanger getRatioTapChanger() {
            return ImmutableRatioTapChanger.ofNullable(leg.getRatioTapChanger());
        }

        @Override
        public Terminal getTerminal() {
            return ImmutableTerminal.ofNullable(leg.getTerminal());
        }

        @Override
        public double getR() {
            return leg.getR();
        }

        @Override
        public Leg2or3 setR(double r) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getX() {
            return leg.getX();
        }

        @Override
        public Leg2or3 setX(double x) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getRatedU() {
            return leg.getRatedU();
        }

        @Override
        public Leg2or3 setRatedU(double ratedU) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public CurrentLimits getCurrentLimits() {
            return ImmutableCurrentLimits.ofNullable(leg.getCurrentLimits());
        }

        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }
    }

}
