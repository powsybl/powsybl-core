/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

/**
 * An immutable {@link RatioTapChanger}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
final class ImmutableRatioTapChanger extends ImmutableTapChanger implements RatioTapChanger {

    private final RatioTapChanger ratioTapChanger;

    ImmutableRatioTapChanger(RatioTapChanger tapChanger, ImmutableCacheIndex cache) {
        super(tapChanger, cache);
        this.ratioTapChanger = tapChanger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTargetV() {
        return ratioTapChanger.getTargetV();
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return ratioTapChanger.hasLoadTapChangingCapabilities();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public RatioTapChanger setTapPosition(int tapPosition) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * @return Returns a new instance of {@link ImmutableRatioTapChangerStep}
     */
    @Override
    public RatioTapChangerStep getStep(int tapPosition) {
        return new ImmutableRatioTapChangerStep(ratioTapChanger.getStep(tapPosition));
    }

    /**
     * {@inheritDoc}
     * @return Returns a new instance of {@link ImmutableRatioTapChangerStep}
     */
    @Override
    public RatioTapChangerStep getCurrentStep() {
        return new ImmutableRatioTapChangerStep(ratioTapChanger.getCurrentStep());
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public RatioTapChanger setRegulating(boolean regulating) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public RatioTapChanger setRegulationTerminal(Terminal regulationTerminal) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * An immutable {@link RatioTapChangerStep}.
     * It is a read-only object, any modification on it will throw a runtime exception.
     */
    public static final class ImmutableRatioTapChangerStep implements RatioTapChangerStep {

        RatioTapChangerStep tap;

        private ImmutableRatioTapChangerStep(RatioTapChangerStep tap) {
            this.tap = Objects.requireNonNull(tap);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getRho() {
            return tap.getRho();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public RatioTapChangerStep setRho(double rho) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getR() {
            return tap.getR();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public RatioTapChangerStep setR(double r) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getX() {
            return tap.getX();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public RatioTapChangerStep setX(double x) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getB() {
            return tap.getB();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public RatioTapChangerStep setB(double b) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getG() {
            return tap.getG();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public RatioTapChangerStep setG(double g) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }
    }
}
