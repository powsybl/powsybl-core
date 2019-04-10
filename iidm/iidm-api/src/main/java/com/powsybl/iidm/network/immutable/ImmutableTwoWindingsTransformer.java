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
 * An immutable {@link TwoWindingsTransformer}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
final class ImmutableTwoWindingsTransformer extends AbstractImmutableIdentifiable<TwoWindingsTransformer> implements TwoWindingsTransformer {

    ImmutableTwoWindingsTransformer(TwoWindingsTransformer twt, ImmutableCacheIndex cache) {
        super(twt, cache);
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
     */
    @Override
    public double getR() {
        return identifiable.getR();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public TwoWindingsTransformer setR(double r) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getX() {
        return identifiable.getX();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public TwoWindingsTransformer setX(double x) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getG() {
        return identifiable.getG();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public TwoWindingsTransformer setG(double g) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getB() {
        return identifiable.getB();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public TwoWindingsTransformer setB(double b) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getRatedU1() {
        return identifiable.getRatedU1();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public TwoWindingsTransformer setRatedU1(double ratedU1) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getRatedU2() {
        return identifiable.getRatedU2();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public TwoWindingsTransformer setRatedU2(double ratedU2) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public PhaseTapChangerAdder newPhaseTapChanger() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public PhaseTapChanger getPhaseTapChanger() {
        return cache.getPhaseTapChanger(identifiable.getPhaseTapChanger());
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
        return cache.getRatioTapChanger(identifiable.getRatioTapChanger());
    }

    // branch interfaces
    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableTerminal}
     */
    @Override
    public Terminal getTerminal1() {
        return cache.getTerminal(identifiable.getTerminal1());
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableTerminal}
     */
    @Override
    public Terminal getTerminal2() {
        return cache.getTerminal(identifiable.getTerminal2());
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
     * @return Returns an {@link ImmutableTerminal}
     */
    @Override
    public Terminal getTerminal(String voltageLevelId) {
        return cache.getTerminal(identifiable.getTerminal(voltageLevelId));
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
     * @return Returns an {@link ImmutableCurrentLimits}
     */
    @Override
    public CurrentLimits getCurrentLimits(Side side) {
        return cache.getCurrentLimits(identifiable.getCurrentLimits(side));
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableCurrentLimits}
     */
    @Override
    public CurrentLimits getCurrentLimits1() {
        return cache.getCurrentLimits(identifiable.getCurrentLimits1());
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public CurrentLimitsAdder newCurrentLimits1() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableCurrentLimits}
     */
    @Override
    public CurrentLimits getCurrentLimits2() {
        return cache.getCurrentLimits(identifiable.getCurrentLimits2());
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public CurrentLimitsAdder newCurrentLimits2() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOverloaded() {
        return identifiable.isOverloaded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOverloaded(float limitReduction) {
        return identifiable.isOverloaded(limitReduction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOverloadDuration() {
        return identifiable.getOverloadDuration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkPermanentLimit(Side side, float limitReduction) {
        return identifiable.checkPermanentLimit(side, limitReduction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkPermanentLimit(Side side) {
        return identifiable.checkPermanentLimit(side);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkPermanentLimit1(float limitReduction) {
        return identifiable.checkPermanentLimit1(limitReduction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkPermanentLimit1() {
        return identifiable.checkPermanentLimit1();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkPermanentLimit2(float limitReduction) {
        return identifiable.checkPermanentLimit2(limitReduction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkPermanentLimit2() {
        return identifiable.checkPermanentLimit2();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Overload checkTemporaryLimits(Side side, float limitReduction) {
        return identifiable.checkTemporaryLimits(side, limitReduction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Overload checkTemporaryLimits(Side side) {
        return identifiable.checkTemporaryLimits(side);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Overload checkTemporaryLimits1(float limitReduction) {
        return identifiable.checkTemporaryLimits1(limitReduction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Overload checkTemporaryLimits1() {
        return identifiable.checkTemporaryLimits1();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Overload checkTemporaryLimits2(float limitReduction) {
        return identifiable.checkTemporaryLimits2(limitReduction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Overload checkTemporaryLimits2() {
        return identifiable.checkTemporaryLimits2();
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
}
