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
final class ImmutableVscConverterStation extends AbstractImmutableIdentifiable<VscConverterStation> implements VscConverterStation {

    ImmutableVscConverterStation(VscConverterStation identifiable, ImmutableCacheIndex cache) {
        super(identifiable, cache);
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return identifiable.isVoltageRegulatorOn();
    }

    @Override
    public HvdcConverterStation setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getVoltageSetpoint() {
        return identifiable.getVoltageSetpoint();
    }

    @Override
    public HvdcConverterStation setVoltageSetpoint(double voltageSetpoint) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getReactivePowerSetpoint() {
        return identifiable.getReactivePowerSetpoint();
    }

    @Override
    public HvdcConverterStation setReactivePowerSetpoint(double reactivePowerSetpoint) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public HvdcType getHvdcType() {
        return identifiable.getHvdcType();
    }

    @Override
    public float getLossFactor() {
        return identifiable.getLossFactor();
    }

    @Override
    public VscConverterStation setLossFactor(float lossFactor) {
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
