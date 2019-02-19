/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableThreeWindingsTransformer extends AbstractImmutableIdentifiable<ThreeWindingsTransformer> implements ThreeWindingsTransformer {

    private final ImmutableCacheIndex cache;

    private ImmutableLeg2or3 cacheLeg2;
    private ImmutableLeg2or3 cacheLeg3;

    ImmutableThreeWindingsTransformer(ThreeWindingsTransformer identifiable, ImmutableCacheIndex cache) {
        super(identifiable);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public Terminal getTerminal(Side side) {
        return cache.getTerminal(identifiable.getTerminal(side));
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
        return cache.getSubstation(identifiable.getSubstation());
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
                return cache.getTerminal(identifiable.getLeg1().getTerminal());
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
                return cache.getCurrentLimits(identifiable.getLeg1().getCurrentLimits());
            }

            @Override
            public CurrentLimitsAdder newCurrentLimits() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }
        };
    }

    @Override
    public Leg2or3 getLeg2() {
        if (cacheLeg2 == null) {
            cacheLeg2 = new ImmutableLeg2or3(identifiable.getLeg2(), cache);
        }
        return cacheLeg2;
    }

    @Override
    public Leg2or3 getLeg3() {
        if (cacheLeg3 == null) {
            cacheLeg3 = new ImmutableLeg2or3(identifiable.getLeg3(), cache);
        }
        return cacheLeg3;
    }

    @Override
    public ConnectableType getType() {
        return identifiable.getType();
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return identifiable.getTerminals().stream().map(cache::getTerminal).collect(Collectors.toList());
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    public static class ImmutableLeg2or3 implements Leg2or3 {

        Leg2or3 leg;

        ImmutableCacheIndex cache;

        ImmutableLeg2or3(Leg2or3 leg, ImmutableCacheIndex cache) {
            this.leg = Objects.requireNonNull(leg);
            this.cache = Objects.requireNonNull(cache);
        }

        @Override
        public RatioTapChangerAdder newRatioTapChanger() {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public RatioTapChanger getRatioTapChanger() {
            return cache.getRatioTapChanger(leg.getRatioTapChanger());
        }

        @Override
        public Terminal getTerminal() {
            return cache.getTerminal(leg.getTerminal());
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
            return cache.getCurrentLimits(leg.getCurrentLimits());
        }

        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }
    }

}
