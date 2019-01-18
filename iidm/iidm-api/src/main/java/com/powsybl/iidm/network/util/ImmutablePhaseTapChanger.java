/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.iidm.network.Terminal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutablePhaseTapChanger extends ImmutableTapChanger implements PhaseTapChanger {

    private static final Map<PhaseTapChanger, ImmutablePhaseTapChanger> CACHE = new HashMap<>();

    PhaseTapChanger phaseTapChanger;

    private ImmutablePhaseTapChanger(PhaseTapChanger phaseTapChanger) {
        super(phaseTapChanger);
        this.phaseTapChanger = phaseTapChanger;
    }

    static ImmutablePhaseTapChanger ofNullable(PhaseTapChanger tapChanger) {
        return tapChanger == null ? null : CACHE.computeIfAbsent(tapChanger, k -> new ImmutablePhaseTapChanger(tapChanger));
    }

    @Override
    public RegulationMode getRegulationMode() {
        return phaseTapChanger.getRegulationMode();
    }

    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getRegulationValue() {
        return phaseTapChanger.getRegulationValue();
    }

    @Override
    public PhaseTapChanger setRegulationValue(double regulationValue) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public PhaseTapChanger setTapPosition(int tapPosition) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public PhaseTapChangerStep getStep(int tapPosition) {
        return new ImmutablePhaseTapChangerStep(phaseTapChanger.getStep(tapPosition));
    }

    @Override
    public PhaseTapChangerStep getCurrentStep() {
        return new ImmutablePhaseTapChangerStep(phaseTapChanger.getCurrentStep());
    }

    @Override
    public PhaseTapChanger setRegulating(boolean regulating) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public PhaseTapChanger setRegulationTerminal(Terminal regulationTerminal) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    public static final class ImmutablePhaseTapChangerStep implements PhaseTapChangerStep {

        PhaseTapChangerStep tap;

        ImmutablePhaseTapChangerStep(PhaseTapChangerStep tap) {
            this.tap = Objects.requireNonNull(tap);
        }

        @Override
        public double getAlpha() {
            return tap.getAlpha();
        }

        @Override
        public PhaseTapChangerStep setAlpha(double alpha) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getRho() {
            return tap.getRho();
        }

        @Override
        public PhaseTapChangerStep setRho(double rho) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getR() {
            return tap.getR();
        }

        @Override
        public PhaseTapChangerStep setR(double r) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getX() {
            return tap.getX();
        }

        @Override
        public PhaseTapChangerStep setX(double x) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getB() {
            return tap.getB();
        }

        @Override
        public PhaseTapChangerStep setB(double b) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }

        @Override
        public double getG() {
            return tap.getG();
        }

        @Override
        public PhaseTapChangerStep setG(double g) {
            throw ImmutableNetwork.createUnmodifiableNetworkException();
        }
    }
}
