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
public final class ImmutableDanglingLine extends AbstractImmutableIdentifiable<DanglingLine> implements DanglingLine {

    private final ImmutableCacheIndex cache;

    ImmutableDanglingLine(DanglingLine identifiable, ImmutableCacheIndex cache) {
        super(identifiable);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public double getP0() {
        return identifiable.getP0();
    }

    @Override
    public DanglingLine setP0(double p0) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getQ0() {
        return identifiable.getQ0();
    }

    @Override
    public DanglingLine setQ0(double q0) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getR() {
        return identifiable.getR();
    }

    @Override
    public DanglingLine setR(double r) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getX() {
        return identifiable.getX();
    }

    @Override
    public DanglingLine setX(double x) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getG() {
        return identifiable.getG();
    }

    @Override
    public DanglingLine setG(double g) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getB() {
        return identifiable.getB();
    }

    @Override
    public DanglingLine setB(double b) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public String getUcteXnodeCode() {
        return identifiable.getUcteXnodeCode();
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        return cache.getCurrentLimits(identifiable.getCurrentLimits());
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Terminal getTerminal() {
        return cache.getTerminal(identifiable.getTerminal());
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
