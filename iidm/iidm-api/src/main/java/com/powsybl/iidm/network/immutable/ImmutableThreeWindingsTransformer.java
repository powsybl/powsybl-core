/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An immutable {@link ThreeWindingsTransformer}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
final class ImmutableThreeWindingsTransformer extends AbstractImmutableIdentifiable<ThreeWindingsTransformer> implements ThreeWindingsTransformer {

    private ImmutableLeg2or3 cacheLeg2;

    private ImmutableLeg2or3 cacheLeg3;

    ImmutableThreeWindingsTransformer(ThreeWindingsTransformer identifiable, ImmutableCacheIndex cache) {
        super(identifiable, cache);
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableTerminal}
     */
    @Override
    public Terminal getTerminal(Side side) {
        return cache.getTerminal(identifiable.getTerminal(side));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Side getSide(Terminal terminal) {
        if (terminal instanceof ImmutableTerminal) {
            return identifiable.getSide(((ImmutableTerminal) terminal).getTerminal());
        } else {
            return identifiable.getSide(terminal);
        }
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableSubstation}
     */
    @Override
    public Substation getSubstation() {
        return cache.getSubstation(identifiable.getSubstation());
    }

    /**
     * {@inheritDoc}
     * @return Returns a new immutable {@link com.powsybl.iidm.network.ThreeWindingsTransformer.Leg1}
     */
    @Override
    public Leg1 getLeg1() {
        return new Leg1() {
            /**
             * {@inheritDoc}
             */
            @Override
            public double getG() {
                return identifiable.getLeg1().getG();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public Leg1 setG(double g) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public double getB() {
                return identifiable.getLeg1().getB();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public Leg1 setB(double b) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * {@inheritDoc}
             * @return Returns an {@link ImmutableTerminal}
             */
            @Override
            public Terminal getTerminal() {
                return cache.getTerminal(identifiable.getLeg1().getTerminal());
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public double getR() {
                return identifiable.getLeg1().getR();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public Leg1 setR(double r) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public double getX() {
                return identifiable.getLeg1().getX();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public Leg1 setX(double x) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public double getRatedU() {
                return identifiable.getLeg1().getRatedU();
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public Leg1 setRatedU(double ratedU) {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }

            /**
             * {@inheritDoc}
             * @return Returns an {@link ImmutableCurrentLimits}
             */
            @Override
            public CurrentLimits getCurrentLimits() {
                return cache.getCurrentLimits(identifiable.getLeg1().getCurrentLimits());
            }

            /**
             * Mutative operation is not allowed. It will throw an exception in runtime.
             * @return
             */
            @Override
            public CurrentLimitsAdder newCurrentLimits() {
                throw ImmutableNetwork.createUnmodifiableNetworkException();
            }
        };
    }

    /**
     * {@inheritDoc}
     * @return Returns a immutable {@link ImmutableLeg2or3}
     */
    @Override
    public Leg2or3 getLeg2() {
        if (cacheLeg2 == null) {
            cacheLeg2 = new ImmutableLeg2or3(identifiable.getLeg2(), cache);
        }
        return cacheLeg2;
    }

    /**
     * {@inheritDoc}
     * @return Returns a immutable {@link ImmutableLeg2or3}
     */
    @Override
    public Leg2or3 getLeg3() {
        if (cacheLeg3 == null) {
            cacheLeg3 = new ImmutableLeg2or3(identifiable.getLeg3(), cache);
        }
        return cacheLeg3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectableType getType() {
        return identifiable.getType();
    }

    /**
     * {@inheritDoc}
     * Terminals are wrapped in {@link ImmutableTerminal}.
     */
    @Override
    public List<? extends Terminal> getTerminals() {
        return identifiable.getTerminals().stream().map(cache::getTerminal).collect(Collectors.toList());
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * An immutable {@link Leg2or3}.
     * It is a read-only object, any modification on it will throw a runtime exception.
     */
    public static class ImmutableLeg2or3 implements Leg2or3 {

        Leg2or3 leg;

        ImmutableCacheIndex cache;

        ImmutableLeg2or3(Leg2or3 leg, ImmutableCacheIndex cache) {
            this.leg = Objects.requireNonNull(leg);
            this.cache = Objects.requireNonNull(cache);
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public RatioTapChangerAdder newRatioTapChanger() {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         * @return an {@link ImmutableRatioTapChanger}
         */
        @Override
        public RatioTapChanger getRatioTapChanger() {
            return cache.getRatioTapChanger(leg.getRatioTapChanger());
        }

        /**
         * {@inheritDoc}
         * @return Returns an {@link ImmutableTerminal}
         */
        @Override
        public Terminal getTerminal() {
            return cache.getTerminal(leg.getTerminal());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getR() {
            return leg.getR();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public Leg2or3 setR(double r) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getX() {
            return leg.getX();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public Leg2or3 setX(double x) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getRatedU() {
            return leg.getRatedU();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public Leg2or3 setRatedU(double ratedU) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         * @return Returns an {@link ImmutableCurrentLimits}
         */
        @Override
        public CurrentLimits getCurrentLimits() {
            return cache.getCurrentLimits(leg.getCurrentLimits());
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public CurrentLimitsAdder newCurrentLimits() {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }
    }

}
