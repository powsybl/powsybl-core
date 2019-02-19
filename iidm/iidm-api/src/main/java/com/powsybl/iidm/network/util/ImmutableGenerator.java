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
public final class ImmutableGenerator extends AbstractImmutableIdentifiable<Generator> implements Generator {

    private final ImmutableCacheIndex cache;

    ImmutableGenerator(Generator identifiable, ImmutableCacheIndex cache) {
        super(identifiable);
        this.cache = Objects.requireNonNull(cache);
    }

    @Override
    public EnergySource getEnergySource() {
        return identifiable.getEnergySource();
    }

    @Override
    public Generator setEnergySource(EnergySource energySource) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getMaxP() {
        return identifiable.getMaxP();
    }

    @Override
    public Generator setMaxP(double maxP) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getMinP() {
        return identifiable.getMinP();
    }

    @Override
    public Generator setMinP(double minP) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return identifiable.isVoltageRegulatorOn();
    }

    @Override
    public Generator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return cache.getTerminal(identifiable.getRegulatingTerminal());
    }

    @Override
    public Generator setRegulatingTerminal(Terminal regulatingTerminal) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getTargetV() {
        return identifiable.getTargetV();
    }

    @Override
    public Generator setTargetV(double targetV) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getTargetP() {
        return identifiable.getTargetP();
    }

    @Override
    public Generator setTargetP(double targetP) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getTargetQ() {
        return identifiable.getTargetQ();
    }

    @Override
    public Generator setTargetQ(double targetQ) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getRatedS() {
        return identifiable.getRatedS();
    }

    @Override
    public Generator setRatedS(double ratedS) {
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

    @Override
    public ReactiveLimits getReactiveLimits() {
        return identifiable.getReactiveLimits();
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        return identifiable.getReactiveLimits(type);
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

}
