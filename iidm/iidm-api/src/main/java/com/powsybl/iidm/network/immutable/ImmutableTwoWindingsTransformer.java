/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
final class ImmutableTwoWindingsTransformer extends AbstractImmutableIdentifiable<TwoWindingsTransformer> implements TwoWindingsTransformer {

    ImmutableTwoWindingsTransformer(TwoWindingsTransformer twt, ImmutableCacheIndex cache) {
        super(twt, cache);
    }

    @Override
    public Substation getSubstation() {
        return cache.getSubstation(identifiable.getSubstation());
    }

    @Override
    public double getR() {
        return identifiable.getR();
    }

    @Override
    public TwoWindingsTransformer setR(double r) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getX() {
        return identifiable.getX();
    }

    @Override
    public TwoWindingsTransformer setX(double x) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getG() {
        return identifiable.getG();
    }

    @Override
    public TwoWindingsTransformer setG(double g) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getB() {
        return identifiable.getB();
    }

    @Override
    public TwoWindingsTransformer setB(double b) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getRatedU1() {
        return identifiable.getRatedU1();
    }

    @Override
    public TwoWindingsTransformer setRatedU1(double ratedU1) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getRatedU2() {
        return identifiable.getRatedU2();
    }

    @Override
    public TwoWindingsTransformer setRatedU2(double ratedU2) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public PhaseTapChangerAdder newPhaseTapChanger() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public PhaseTapChanger getPhaseTapChanger() {
        return cache.getPhaseTapChanger(identifiable.getPhaseTapChanger());
    }

    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
        return cache.getRatioTapChanger(identifiable.getRatioTapChanger());
    }

    // branch interfaces
    @Override
    public Terminal getTerminal1() {
        return cache.getTerminal(identifiable.getTerminal1());
    }

    @Override
    public Terminal getTerminal2() {
        return cache.getTerminal(identifiable.getTerminal2());
    }

    @Override
    public Terminal getTerminal(Side side) {
        return cache.getTerminal(identifiable.getTerminal(side));
    }

    @Override
    public Terminal getTerminal(String voltageLevelId) {
        return cache.getTerminal(identifiable.getTerminal(voltageLevelId));
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
    public CurrentLimits getCurrentLimits(Side side) {
        return cache.getCurrentLimits(identifiable.getCurrentLimits(side));
    }

    @Override
    public CurrentLimits getCurrentLimits1() {
        return cache.getCurrentLimits(identifiable.getCurrentLimits1());
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public CurrentLimits getCurrentLimits2() {
        return cache.getCurrentLimits(identifiable.getCurrentLimits2());
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public boolean isOverloaded() {
        return identifiable.isOverloaded();
    }

    @Override
    public boolean isOverloaded(float limitReduction) {
        return identifiable.isOverloaded(limitReduction);
    }

    @Override
    public int getOverloadDuration() {
        return identifiable.getOverloadDuration();
    }

    @Override
    public boolean checkPermanentLimit(Side side, float limitReduction) {
        return identifiable.checkPermanentLimit(side, limitReduction);
    }

    @Override
    public boolean checkPermanentLimit(Side side) {
        return identifiable.checkPermanentLimit(side);
    }

    @Override
    public boolean checkPermanentLimit1(float limitReduction) {
        return identifiable.checkPermanentLimit1(limitReduction);
    }

    @Override
    public boolean checkPermanentLimit1() {
        return identifiable.checkPermanentLimit1();
    }

    @Override
    public boolean checkPermanentLimit2(float limitReduction) {
        return identifiable.checkPermanentLimit2(limitReduction);
    }

    @Override
    public boolean checkPermanentLimit2() {
        return identifiable.checkPermanentLimit2();
    }

    @Override
    public Overload checkTemporaryLimits(Side side, float limitReduction) {
        return identifiable.checkTemporaryLimits(side, limitReduction);
    }

    @Override
    public Overload checkTemporaryLimits(Side side) {
        return identifiable.checkTemporaryLimits(side);
    }

    @Override
    public Overload checkTemporaryLimits1(float limitReduction) {
        return identifiable.checkTemporaryLimits1(limitReduction);
    }

    @Override
    public Overload checkTemporaryLimits1() {
        return identifiable.checkTemporaryLimits1();
    }

    @Override
    public Overload checkTemporaryLimits2(float limitReduction) {
        return identifiable.checkTemporaryLimits2(limitReduction);
    }

    @Override
    public Overload checkTemporaryLimits2() {
        return identifiable.checkTemporaryLimits2();
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
}
