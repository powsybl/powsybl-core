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
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
class ImmutableLine extends AbstractImmutableIdentifiable<Line> implements Line {

    private final ImmutableCacheIndex cache;

    protected ImmutableLine(Line identifiable, ImmutableCacheIndex cache) {
        super(identifiable);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public boolean isTieLine() {
        return identifiable.isTieLine();
    }

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
        return identifiable.getSide(terminal);
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

    @Override
    public double getR() {
        return identifiable.getR();
    }

    @Override
    public Line setR(double r) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getX() {
        return identifiable.getX();
    }

    @Override
    public Line setX(double x) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getG1() {
        return identifiable.getG1();
    }

    @Override
    public Line setG1(double g1) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getG2() {
        return identifiable.getG2();
    }

    @Override
    public Line setG2(double g2) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getB1() {
        return identifiable.getB1();
    }

    @Override
    public Line setB1(double b1) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getB2() {
        return identifiable.getB2();
    }

    @Override
    public Line setB2(double b2) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

}
