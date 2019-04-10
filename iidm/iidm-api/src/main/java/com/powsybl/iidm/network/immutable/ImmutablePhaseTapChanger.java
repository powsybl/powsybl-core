/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

/**
 * An immutable {@link PhaseTapChanger}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
final class ImmutablePhaseTapChanger extends ImmutableTapChanger implements PhaseTapChanger {

    private final PhaseTapChanger phaseTapChanger;

    ImmutablePhaseTapChanger(PhaseTapChanger phaseTapChanger, ImmutableCacheIndex cache) {
        super(phaseTapChanger, cache);
        this.phaseTapChanger = phaseTapChanger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegulationMode getRegulationMode() {
        return phaseTapChanger.getRegulationMode();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getRegulationValue() {
        return phaseTapChanger.getRegulationValue();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public PhaseTapChanger setRegulationValue(double regulationValue) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public PhaseTapChanger setTapPosition(int tapPosition) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * @return Returns a new instance of {@link ImmutablePhaseTapChangerStep}
     */
    @Override
    public PhaseTapChangerStep getStep(int tapPosition) {
        return new ImmutablePhaseTapChangerStep(phaseTapChanger.getStep(tapPosition));
    }

    /**
     * {@inheritDoc}
     * @return Returns a new instance of {@link ImmutablePhaseTapChangerStep}
     */
    @Override
    public PhaseTapChangerStep getCurrentStep() {
        return new ImmutablePhaseTapChangerStep(phaseTapChanger.getCurrentStep());
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public PhaseTapChanger setRegulating(boolean regulating) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public PhaseTapChanger setRegulationTerminal(Terminal regulationTerminal) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * An immutable {@link PhaseTapChangerStep}.
     * It is a read-only object, any modification on it will throw a runtime exception.
     */
    public static final class ImmutablePhaseTapChangerStep implements PhaseTapChangerStep {

        PhaseTapChangerStep tap;

        ImmutablePhaseTapChangerStep(PhaseTapChangerStep tap) {
            this.tap = Objects.requireNonNull(tap);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getAlpha() {
            return tap.getAlpha();
        }

        /**
         * Mutative operation is not allowed. It will throw an exception in runtime.
         * @return
         */
        @Override
        public PhaseTapChangerStep setAlpha(double alpha) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
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
        public PhaseTapChangerStep setRho(double rho) {
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
        public PhaseTapChangerStep setR(double r) {
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
        public PhaseTapChangerStep setX(double x) {
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
        public PhaseTapChangerStep setB(double b) {
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
        public PhaseTapChangerStep setG(double g) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }
    }
}
