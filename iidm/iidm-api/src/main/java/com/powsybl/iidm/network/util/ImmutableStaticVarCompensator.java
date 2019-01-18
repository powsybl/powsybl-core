/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableStaticVarCompensator extends AbstractImmutableIdentifiable<StaticVarCompensator> implements StaticVarCompensator {

    private static final Map<StaticVarCompensator, ImmutableStaticVarCompensator> CACHE = new HashMap<>();

    private ImmutableStaticVarCompensator(StaticVarCompensator identifiable) {
        super(identifiable);
    }

    static ImmutableStaticVarCompensator ofNullable(StaticVarCompensator svc) {
        return null == svc ? null : CACHE.computeIfAbsent(svc, k -> new ImmutableStaticVarCompensator(svc));
    }

    @Override
    public double getBmin() {
        return identifiable.getBmin();
    }

    @Override
    public StaticVarCompensator setBmin(double bMin) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getBmax() {
        return identifiable.getBmax();
    }

    @Override
    public StaticVarCompensator setBmax(double bMax) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getVoltageSetPoint() {
        return identifiable.getVoltageSetPoint();
    }

    @Override
    public StaticVarCompensator setVoltageSetPoint(double voltageSetPoint) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getReactivePowerSetPoint() {
        return identifiable.getReactivePowerSetPoint();
    }

    @Override
    public StaticVarCompensator setReactivePowerSetPoint(double reactivePowerSetPoint) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public RegulationMode getRegulationMode() {
        return identifiable.getRegulationMode();
    }

    @Override
    public StaticVarCompensator setRegulationMode(RegulationMode regulationMode) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Terminal getTerminal() {
        return ImmutableTerminal.ofNullable(identifiable.getTerminal());
    }

    @Override
    public ConnectableType getType() {
        return identifiable.getType();
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return identifiable.getTerminals().stream().map(ImmutableTerminal::ofNullable).collect(Collectors.toList());
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
