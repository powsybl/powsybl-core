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
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableRatioTapChanger extends ImmutableTapChanger implements RatioTapChanger {

    RatioTapChanger ratioTapChanger;

    ImmutableRatioTapChanger(RatioTapChanger tapChanger, ImmutableCacheIndex cache) {
        super(tapChanger, cache);
        this.ratioTapChanger = tapChanger;
    }

    @Override
    public double getTargetV() {
        return ratioTapChanger.getTargetV();
    }

    @Override
    public RatioTapChanger setTargetV(double targetV) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return ratioTapChanger.hasLoadTapChangingCapabilities();
    }

    @Override
    public RatioTapChanger setTapPosition(int tapPosition) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public RatioTapChangerStep getStep(int tapPosition) {
        return new ImmutableRatioTapChangerStep(ratioTapChanger.getStep(tapPosition));
    }

    @Override
    public RatioTapChangerStep getCurrentStep() {
        return new ImmutableRatioTapChangerStep(ratioTapChanger.getCurrentStep());
    }

    @Override
    public RatioTapChanger setRegulating(boolean regulating) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public RatioTapChanger setRegulationTerminal(Terminal regulationTerminal) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    public static final class ImmutableRatioTapChangerStep implements RatioTapChangerStep {

        RatioTapChangerStep tap;

        private ImmutableRatioTapChangerStep(RatioTapChangerStep tap) {
            this.tap = Objects.requireNonNull(tap);
        }

        @Override
        public double getRho() {
            return tap.getRho();
        }

        @Override
        public RatioTapChangerStep setRho(double rho) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getR() {
            return tap.getR();
        }

        @Override
        public RatioTapChangerStep setR(double r) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getX() {
            return tap.getX();
        }

        @Override
        public RatioTapChangerStep setX(double x) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getB() {
            return tap.getB();
        }

        @Override
        public RatioTapChangerStep setB(double b) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getG() {
            return tap.getG();
        }

        @Override
        public RatioTapChangerStep setG(double g) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }
    }
}
